package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object PdfConverter {

    @Throws(IOException::class)
    fun createPdfFromImages(context: Context, uris: List<Uri>, outputFileName: String): File {
        val pdfDocument = PdfDocument()
        
        for (uri in uris) {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            
            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            
            val canvas = page.canvas
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            
            pdfDocument.finishPage(page)
            bitmap.recycle()
        }
        
        val outputFile = File(context.cacheDir, outputFileName)
        val fileOutputStream = FileOutputStream(outputFile)
        pdfDocument.writeTo(fileOutputStream)
        pdfDocument.close()
        fileOutputStream.close()
        
        return outputFile
    }
}
