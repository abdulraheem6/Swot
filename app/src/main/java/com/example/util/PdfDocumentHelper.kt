package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.media.ExifInterface
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.min

object PdfDocumentHelper {

    private const val PAGE_WIDTH_PT = 595 // Standard A4 width points (72 DPI)
    private const val PAGE_HEIGHT_PT = 842 // Standard A4 height points (72 DPI)

    private fun openStream(context: Context, uri: Uri): InputStream? {
        return try {
            context.contentResolver.openInputStream(uri)
        } catch (_: Exception) {
            try {
                val path = uri.path
                if (path != null && File(path).exists()) {
                    File(path).inputStream()
                } else null
            } catch (_: Exception) {
                null
            }
        }
    }

    /**
     * Decodes an image URI with safe sampling and EXIF orientation adjustment to avoid OOM.
     */
    fun decodeSampledBitmap(context: Context, uri: Uri, maxDimension: Int = 1800): Bitmap? {
        return try {
            // First pass: inspect dimensions
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            openStream(context, uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            if (options.outWidth <= 0 || options.outHeight <= 0) return null

            // Calculate sample size
            var inSampleSize = 1
            var w = options.outWidth
            var h = options.outHeight
            while (w > maxDimension || h > maxDimension) {
                w /= 2
                h /= 2
                inSampleSize *= 2
            }

            // Second pass: decode scaled bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val decodedBitmap = openStream(context, uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return null

            // Inspect EXIF rotation if available
            val rotationDegrees = getExifRotation(context, uri)
            if (rotationDegrees != 0) {
                val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                val rotated = Bitmap.createBitmap(
                    decodedBitmap, 0, 0, decodedBitmap.width, decodedBitmap.height, matrix, true
                )
                if (rotated != decodedBitmap) decodedBitmap.recycle()
                rotated
            } else {
                decodedBitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getExifRotation(context: Context, uri: Uri): Int {
        return try {
            openStream(context, uri)?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (_: Exception) {
            0
        }
    }

    /**
     * Compiles multiple image pages into a single, clean A4 PDF document.
     */
    fun compileImagesToPdf(
        context: Context,
        imageUris: List<Uri>,
        outputFile: File,
        docTitle: String = "Scanned Document"
    ): Boolean {
        if (imageUris.isEmpty()) return false

        val pdfDocument = PdfDocument()

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 9f
        }

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 10f
            isFakeBoldText = true
        }

        val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        try {
            outputFile.parentFile?.mkdirs()

            val totalPages = imageUris.size
            for (index in imageUris.indices) {
                val uri = imageUris[index]
                val bitmap = decodeSampledBitmap(context, uri) ?: continue

                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH_PT, PAGE_HEIGHT_PT, index + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                // Background
                canvas.drawColor(Color.WHITE)

                // Header
                val cleanTitle = if (docTitle.isBlank()) "MyVault Scanned Document" else docTitle.take(50)
                canvas.drawText(cleanTitle, 36f, 26f, titlePaint)
                canvas.drawText("🛡️ Zero-Cloud Encrypted", (PAGE_WIDTH_PT - 150).toFloat(), 26f, textPaint)

                // Usable drawing area
                val marginH = 36f
                val marginTop = 36f
                val marginBottom = 36f
                val availableW = PAGE_WIDTH_PT - (marginH * 2)
                val availableH = PAGE_HEIGHT_PT - marginTop - marginBottom

                // Proportional scale
                val scale = min(availableW / bitmap.width.toFloat(), availableH / bitmap.height.toFloat())
                val drawW = bitmap.width * scale
                val drawH = bitmap.height * scale
                val drawLeft = marginH + (availableW - drawW) / 2f
                val drawTop = marginTop + (availableH - drawH) / 2f

                canvas.drawBitmap(bitmap, null, RectF(drawLeft, drawTop, drawLeft + drawW, drawTop + drawH), bitmapPaint)

                // Footer
                val footerText = "Page ${index + 1} of $totalPages  ·  MyVault Secure PDF"
                val textW = textPaint.measureText(footerText)
                canvas.drawText(footerText, (PAGE_WIDTH_PT - textW) / 2f, (PAGE_HEIGHT_PT - 16).toFloat(), textPaint)

                pdfDocument.finishPage(page)
                bitmap.recycle()
            }

            FileOutputStream(outputFile).use { fos ->
                pdfDocument.writeTo(fos)
                fos.flush()
            }
            pdfDocument.close()
            return outputFile.exists() && outputFile.length() > 0
        } catch (e: Exception) {
            e.printStackTrace()
            try { pdfDocument.close() } catch (_: Exception) {}
            return false
        }
    }

    /**
     * Renders a specific page of an unencrypted PDF file on disk into a Bitmap for native display.
     * Returns Pair(Bitmap?, totalPageCount)
     */
    fun renderPdfPage(pdfFile: File, pageIndex: Int, targetWidth: Int = 900): Pair<Bitmap?, Int> {
        if (!pdfFile.exists() || pdfFile.length() == 0L) return Pair(null, 0)
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        var page: PdfRenderer.Page? = null

        return try {
            pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)
            val totalPages = renderer.pageCount
            if (totalPages == 0) return Pair(null, 0)

            val safeIndex = pageIndex.coerceIn(0, totalPages - 1)
            page = renderer.openPage(safeIndex)

            val scale = targetWidth.toFloat() / page.width.toFloat()
            val targetHeight = (page.height * scale).toInt().coerceAtLeast(100)
            val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)

            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            Pair(bitmap, totalPages)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(null, 0)
        } finally {
            try { page?.close() } catch (_: Exception) {}
            try { renderer?.close() } catch (_: Exception) {}
            try { pfd?.close() } catch (_: Exception) {}
        }
    }
}
