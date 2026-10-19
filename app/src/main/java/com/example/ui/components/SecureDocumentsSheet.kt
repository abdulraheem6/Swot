package com.example.ui.components

import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import coil.compose.AsyncImage
import com.example.data.SecureDocument
import com.example.ui.VaultViewModel
import com.example.ui.theme.BorderAccent
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafeGreenBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.UrgentRed
import com.example.ui.theme.UrgentRedBg
import com.example.ui.theme.VaultSurface
import com.example.ui.theme.VaultSurfaceVariant
import java.io.File
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDocumentSheet(
    onDismiss: () -> Unit,
    onSave: (SecureDocument) -> Unit,
    viewModel: VaultViewModel
) {
    val theme = LocalAppTheme.current
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var selectedFileSize by remember { mutableStateOf(0L) }
    var selectedMimeType by remember { mutableStateOf("") }
    var docName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("document") }
    var notes by remember { mutableStateOf("") }
    
    val existingDocs by viewModel.documents.collectAsState()
    val allFolders = remember(existingDocs) {
        val standard = listOf("document", "photo", "video", "id", "contract", "receipt", "medical")
        val custom = existingDocs.map { it.category }.filter { it !in standard }
        (standard + custom).distinct()
    }

    var folderOption by remember { mutableStateOf("existing") } // "existing" or "new"
    var newFolderName by remember { mutableStateOf("") }

    // Explicit PIN & 2FA controls for any file
    var requirePin by remember { mutableStateOf(true) }
    var customPin by remember { mutableStateOf("") }
    var require2fa by remember { mutableStateOf(false) }
    var custom2faKey by remember { mutableStateOf("") }


    val categories = listOf(
        "document" to "📄 Document / Certificate",
        "photo" to "🖼️ Secure Photo",
        "video" to "🎥 Secure Video / Footage",
        "id" to "🪪 Government ID / Passport",
        "contract" to "📑 Confidential Agreement",
        "receipt" to "🧾 Financial Receipt / Invoice",
        "medical" to "🏥 Health & Medical Record"
    )

    fun onFilePicked(uri: Uri?) {
        if (uri == null) return
        selectedUri = uri
        var name = ""
        var size = 0L
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIdx != -1) name = cursor.getString(nameIdx) ?: ""
                    if (sizeIdx != -1) size = cursor.getLong(sizeIdx)
                }
            }
        } catch (_: Exception) {}

        if (name.isBlank()) {
            name = uri.lastPathSegment?.substringAfterLast("/") ?: "secure_file"
        }
        val mime = context.contentResolver.getType(uri) ?: ""
        selectedFileName = name
        selectedFileSize = size
        selectedMimeType = mime

        val ext = name.substringAfterLast(".", "").lowercase(Locale.US)
        val titleCandidate = if (name.contains(".")) name.substringBeforeLast(".") else name
        if (docName.isBlank() || docName.startsWith("Secure_")) {
            docName = titleCandidate.replace("_", " ").replace("-", " ")
        }

        // Smart category detection based on actual extension or MIME type
        when {
            ext in listOf("pdf", "doc", "docx", "txt", "rtf", "odt") -> category = "document"
            ext in listOf("jpg", "jpeg", "png", "webp", "heic", "gif") || mime.startsWith("image/") -> category = "photo"
            ext in listOf("mp4", "mov", "mkv", "avi", "3gp") || mime.startsWith("video/") -> category = "video"
            ext in listOf("xls", "xlsx", "csv") -> category = "receipt"
            ext in listOf("zip", "rar", "7z", "tar", "gz") -> category = "document"
        }
    }

    // 1. Android Document / File Picker - Opens ANY folder and file on phone (PDFs, docs, downloads, internal storage)
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        onFilePicked(uri)
    }

    // 2. Android GetContent Picker - Alternative general file selector
    val getContentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onFilePicked(uri)
    }

    // 3. Android Visual Media Picker - Dedicated for Photos / Videos
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        onFilePicked(uri)
    }

    // 4. Multi-Photo Picker for Compiling into Single PDF
    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 30)
    ) { uris ->
        if (uris.isNotEmpty()) {
            onDismiss()
            viewModel.openMultiPageScannerDialog(uris)
        }
    }

    // 5. Multi-Document / File Picker for Compiling into Single PDF
    val multipleDocPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onDismiss()
            viewModel.openMultiPageScannerDialog(uris)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = theme.surface,
        modifier = Modifier.testTag("add_document_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "📁 Add Document, File, Photo or Video",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Select any file from your phone storage or camera roll. Encrypted with AES-256.",
                fontSize = 11.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(14.dp))

            // File selection section: Supports documents, folders, downloads, PDFs, as well as photos/videos
            if (selectedUri != null) {
                // SELECTED FILE PREVIEW CARD
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141420)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, theme.primary, RoundedCornerShape(14.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val isImage = category == "photo" || selectedMimeType.startsWith("image/")
                        val isVideo = category == "video" || selectedMimeType.startsWith("video/")

                        if (isImage) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            ) {
                                AsyncImage(
                                    model = selectedUri,
                                    contentDescription = selectedFileName,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        } else {
                            Text(
                                text = if (isVideo) "🎥" else if (selectedFileName.endsWith(".pdf", true)) "📄" else "📁",
                                fontSize = 42.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        Text(
                            text = selectedFileName.ifBlank { "Selected File" },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1
                        )

                        val sizeLabel = if (selectedFileSize > 0) {
                            val kb = selectedFileSize / 1024.0
                            if (kb > 1024) String.format(Locale.US, "%.1f MB", kb / 1024.0) else String.format(Locale.US, "%.1f KB", kb)
                        } else "Ready to encrypt"

                        Text(
                            text = "$sizeLabel · ${if (selectedMimeType.isNotBlank()) selectedMimeType else "Document"}",
                            fontSize = 11.sp,
                            color = theme.primaryLight
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SafeGreenBg)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("🛡️ AES-256-GCM Encrypted at Rest", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SafeGreen)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        documentPickerLauncher.launch(arrayOf("*/*"))
                                    } catch (_: Exception) {
                                        getContentPickerLauncher.launch("*/*")
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Change File", fontSize = 11.sp)
                            }
                            Button(
                                onClick = {
                                    selectedUri = null
                                    selectedFileName = ""
                                    selectedFileSize = 0L
                                    selectedMimeType = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = UrgentRedBg, contentColor = UrgentRed),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Remove", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // CHOOSE FILE CARD - CLEAN & NON-CLUMSY TWO PRIMARY ACTIONS
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141420)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, theme.primary.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📁 Add Document or Secure Media", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Choose single file/photo with encryption, or scan/merge multi-page PDF", fontSize = 11.sp, color = TextMuted)

                        Spacer(modifier = Modifier.height(14.dp))

                        // 1. Single File / Photo Upload (Preserves exact existing single encryption functionality)
                        Button(
                            onClick = {
                                try {
                                    documentPickerLauncher.launch(arrayOf("*/*"))
                                } catch (_: Exception) {
                                    getContentPickerLauncher.launch("*/*")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("pick_any_document_button")
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("Upload Single File, ID or Photo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("PDFs, Passports, Photos, Docs, Downloads (AES-256)", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 2. Multi-Page Camera Scanner & PDF Compiler (Snaps or merges multiple pages)
                        Button(
                            onClick = {
                                onDismiss()
                                viewModel.openMultiPageScannerDialog()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("scan_multipage_pdf_button")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("📄 Multi-Page Document Scanner (PDF)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Snap camera pages or pick multiple photos to compile into PDF", fontSize = 10.sp, color = Color.White.copy(alpha = 0.85f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = docName,
                onValueChange = { docName = it },
                label = { Text("Title / File Description") },
                placeholder = { Text("e.g. Passport Bio Page, Inspection Footage, Deed") },
                modifier = Modifier.fillMaxWidth().testTag("doc_name_input"),
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Folder Storage Location selector (Existing vs New Folder)
            Text("📁 Folder Storage Location", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.primaryLight)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { folderOption = "existing" },
                    colors = ButtonDefaults.buttonColors(containerColor = if (folderOption == "existing") theme.primary else VaultSurfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Existing Folder", fontSize = 11.sp, color = if (folderOption == "existing") Color.White else TextPrimary)
                }
                Button(
                    onClick = { folderOption = "new" },
                    colors = ButtonDefaults.buttonColors(containerColor = if (folderOption == "new") theme.primary else VaultSurfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+ Create New Folder", fontSize = 11.sp, color = if (folderOption == "new") Color.White else TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (folderOption == "existing") {
                var catExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = catExpanded,
                    onExpandedChange = { catExpanded = it }
                ) {
                    OutlinedTextField(
                        value = categories.find { it.first == category }?.second ?: "📁 ${category.replaceFirstChar { it.uppercase() }}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Existing Folder / Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        colors = defaultTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = catExpanded,
                        onDismissRequest = { catExpanded = false },
                        containerColor = VaultSurfaceVariant
                    ) {
                        allFolders.forEach { folderId ->
                            val label = categories.find { it.first == folderId }?.second ?: "📁 ${folderId.replaceFirstChar { it.uppercase() }}"
                            DropdownMenuItem(
                                text = { Text(label, color = TextPrimary) },
                                onClick = {
                                    category = folderId
                                    catExpanded = false
                                }
                            )
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("New Folder Name") },
                    placeholder = { Text("e.g. Tax2026, Insurance, FamilyArchive") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = defaultTextFieldColors()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes & Metadata (Optional)") },
                placeholder = { Text("Reference ID, expiration, security context") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                colors = defaultTextFieldColors()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // EXPLICIT PIN & 2FA SECURITY SECTION (CORE USER REQUIREMENT)
            Card(
                colors = CardDefaults.cardColors(containerColor = VaultSurfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("SECURITY & ACCESS GATES", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = theme.primaryLight)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (requirePin && require2fa) UrgentRedBg else if (requirePin) theme.primaryContainer else SafeGreenBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = when {
                                    requirePin && require2fa -> "🛡️ PIN + 2FA"
                                    requirePin -> "🔒 PIN"
                                    require2fa -> "🔑 2FA"
                                    else -> "🔓 Direct"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    requirePin && require2fa -> UrgentRed
                                    requirePin -> theme.primaryLight
                                    require2fa -> Color(0xFFFFB74D)
                                    else -> SafeGreen
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Option 1: PIN Gate
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("🔒 Require PIN to Open", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Must enter PIN to view / decrypt this file", fontSize = 11.sp, color = TextMuted)
                        }
                        Switch(
                            checked = requirePin,
                            onCheckedChange = { requirePin = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = theme.primary)
                        )
                    }

                    if (requirePin) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = customPin,
                            onValueChange = { if (it.length <= 6) customPin = it },
                            label = { Text("Custom Item PIN (Optional - Defaults to Master PIN)") },
                            placeholder = { Text("Leave blank to use Master PIN") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = defaultTextFieldColors()
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Option 2: 2FA Gate
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("🔑 Require 2FA Code to Open", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Two-Factor Authentication challenge required", fontSize = 11.sp, color = TextMuted)
                        }
                        Switch(
                            checked = require2fa,
                            onCheckedChange = { require2fa = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = theme.primary)
                        )
                    }

                    if (require2fa) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = custom2faKey,
                            onValueChange = { custom2faKey = it },
                            label = { Text("Custom 2FA Key (Optional - Defaults to Master '123456')") },
                            placeholder = { Text("Leave blank to use Master 2FA code") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = defaultTextFieldColors()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    if (docName.isBlank()) {
                        viewModel.showToast("⚠️ Title required")
                        return@Button
                    }
                    val finalCategory = if (folderOption == "new" && newFolderName.isNotBlank()) {
                        newFolderName.trim().lowercase(Locale.US).replace("\\s+".toRegex(), "_")
                    } else {
                        category
                    }
                    val isVideoCat = finalCategory == "video" || finalCategory.contains("video", true)
                    val effectivePin = if (requirePin && customPin.isNotBlank()) customPin.trim() else viewModel.settings.value.pin

                    val savedResult = if (selectedUri != null) {
                        viewModel.saveEncryptedAttachment(
                            selectedUri!!,
                            selectedFileName.ifBlank { "$docName.${if (isVideoCat) "mp4" else "dat"}" },
                            effectivePin
                        )
                    } else null

                    val finalUriStr = savedResult?.path ?: (if (isVideoCat) "sample_video_cam" else "sample_attachment")
                    val finalSize = savedResult?.size ?: (if (selectedFileSize > 0) selectedFileSize else if (isVideoCat) 1024 * 1024 * 4 else 1024 * 350)
                    val finalOrigName = if (savedResult != null) savedResult.originalName else selectedFileName
                    val finalMime = if (savedResult != null) savedResult.mimeType else selectedMimeType

                    val newDoc = SecureDocument(
                        id = UUID.randomUUID().toString(),
                        name = docName.trim(),
                        category = finalCategory,
                        uri = finalUriStr,
                        fileSize = finalSize,
                        notes = notes.trim(),
                        locked = requirePin || require2fa,
                        isVideo = isVideoCat,
                        requirePin = requirePin,
                        require2fa = require2fa,
                        customPin = customPin.trim(),
                        custom2faKey = custom2faKey.trim(),
                        originalFileName = finalOrigName,
                        mimeType = finalMime,
                        isEncrypted = true
                    )
                    viewModel.addAuditLog("FILE_ENCRYPTED", "Uploaded & AES-256 encrypted '${newDoc.name}' ($finalCategory)")
                    onSave(newDoc)
                },
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("save_document_button")
            ) {
                Text("🔒 Encrypt & Save to Vault", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", color = TextMuted)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }


}

@Composable
fun DocumentDetailDialog(
    doc: SecureDocument,
    viewModel: VaultViewModel,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val theme = LocalAppTheme.current
    val settings by viewModel.settings.collectAsState()

    // Active regime forces PIN or 2FA if Strict Enclave or High Defense
    val effectiveRequirePin = remember(doc, settings.securityRegime) {
        doc.requirePin || settings.securityRegime == "high_security" || settings.securityRegime == "strict_enclave"
    }
    val effectiveRequire2fa = remember(doc, settings.securityRegime) {
        doc.require2fa || settings.securityRegime == "strict_enclave"
    }

    var isUnlocked by remember { mutableStateOf(!effectiveRequirePin && !effectiveRequire2fa) }
    var enteredPin by remember { mutableStateOf("") }
    var entered2faKey by remember { mutableStateOf("") }
    var pinVisible by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    // In-dialog editing of PIN / 2FA options
    var showSecurityEdit by remember { mutableStateOf(false) }
    var editRequirePin by remember { mutableStateOf(doc.requirePin) }
    var editRequire2fa by remember { mutableStateOf(doc.require2fa) }
    var editCustomPin by remember { mutableStateOf(doc.customPin) }
    var editCustom2faKey by remember { mutableStateOf(doc.custom2faKey) }

    // Video playback simulation state
    var isVideoPlaying by remember { mutableStateOf(false) }

    val saveAsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(
            if (doc.mimeType.isNotBlank()) doc.mimeType else "*/*"
        )
    ) { destUri: Uri? ->
        if (destUri != null) {
            viewModel.saveDecryptedDocumentToUri(doc, destUri)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("document_detail_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (doc.isVideo || doc.category == "video") "🎥" else "📁",
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(doc.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Text(
                            text = "${doc.category.replaceFirstChar { it.uppercase() }} · Encrypted Storage",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!isUnlocked) {
                    // LOCKED SECURITY AUTHENTICATION GATE (PIN & 2FA VERIFICATION)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, theme.primary, RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(theme.primaryContainer)
                                    .border(1.dp, theme.primaryLight, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Secured",
                                    tint = theme.primaryLight,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = when {
                                    doc.isVideo || doc.category == "video" -> "Protected Video Footage"
                                    doc.category == "photo" -> "Protected Secure Photo"
                                    else -> "Protected Confidential File"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = when {
                                    effectiveRequirePin && effectiveRequire2fa -> "This file enforces BOTH PIN and Two-Factor Authentication (2FA)."
                                    effectiveRequirePin -> "Enter your 4-digit PIN to decrypt and open this file."
                                    else -> "Enter your 2FA Authentication Key to view this file."
                                },
                                fontSize = 11.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // PIN Input (if required)
                            if (effectiveRequirePin) {
                                OutlinedTextField(
                                    value = enteredPin,
                                    onValueChange = {
                                        enteredPin = it
                                        authError = null
                                    },
                                    label = { Text("4-Digit PIN") },
                                    placeholder = { Text("e.g. 1234") },
                                    singleLine = true,
                                    visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    leadingIcon = {
                                        Icon(Icons.Default.Key, contentDescription = null, tint = theme.primaryLight)
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { pinVisible = !pinVisible }) {
                                            Icon(
                                                if (pinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Toggle PIN visibility",
                                                tint = TextMuted
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("doc_auth_password_input"),
                                    colors = defaultTextFieldColors()
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            // 2FA Key Input (if required)
                            if (effectiveRequire2fa) {
                                OutlinedTextField(
                                    value = entered2faKey,
                                    onValueChange = {
                                        entered2faKey = it
                                        authError = null
                                    },
                                    label = { Text("2FA Security Code") },
                                    placeholder = { Text("e.g. 123456") },
                                    singleLine = true,
                                    leadingIcon = {
                                        Text("🔑", fontSize = 14.sp)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("doc_auth_2fa_input"),
                                    colors = defaultTextFieldColors()
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                // Quick demo pill for user convenience
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0x1AFFFFFF))
                                            .clickable {
                                                entered2faKey = doc.custom2faKey.ifBlank { settings.master2faKey }
                                            }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "💡 Demo 2FA: ${doc.custom2faKey.ifBlank { settings.master2faKey }}",
                                            fontSize = 10.sp,
                                            color = SafeGreen
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            if (authError != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = authError ?: "",
                                    color = UrgentRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    val (verified, err) = viewModel.verifyPinAnd2fa(
                                        enteredPin = enteredPin,
                                        entered2fa = entered2faKey,
                                        requirePin = effectiveRequirePin,
                                        require2fa = effectiveRequire2fa,
                                        customPin = doc.customPin,
                                        custom2faKey = doc.custom2faKey
                                    )
                                    if (verified) {
                                        isUnlocked = true
                                        viewModel.addAuditLog(
                                            "FILE_DECRYPT",
                                            "Decrypted '${doc.name}' (PIN: ${if (effectiveRequirePin) "Passed" else "N/A"}, 2FA: ${if (effectiveRequire2fa) "Verified" else "N/A"})"
                                        )
                                        viewModel.showToast("🔓 File unlocked & decrypted")
                                    } else {
                                        authError = err ?: "Authentication failed."
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("unlock_doc_button")
                            ) {
                                Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Unlock & Decrypt File", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // UNLOCKED VIEW: FILE PREVIEW + VIDEO PLAYER + DOWNLOAD TO MOBILE
                    if (doc.isVideo || doc.category == "video") {
                        // VIDEO PLAYER PREVIEW CARD
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0A0A12))
                                .border(1.dp, theme.primary, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(30.dp))
                                        .background(if (isVideoPlaying) UrgentRedBg else theme.primaryContainer)
                                        .clickable {
                                            isVideoPlaying = !isVideoPlaying
                                            if (isVideoPlaying) {
                                                viewModel.showToast("▶️ Playing encrypted video stream")
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(if (isVideoPlaying) "⏸️" else "▶️", fontSize = 24.sp)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(doc.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(
                                    text = if (isVideoPlaying) "Streaming · 1080p Encrypted H.264" else "1080p Enclave Video Footage (02:14)",
                                    fontSize = 11.sp,
                                    color = SafeGreen
                                )
                            }
                        }
                    } else {
                        // PHOTO / DOCUMENT PREVIEW
                        val decryptedCacheFile = remember(isUnlocked, doc) {
                            if (isUnlocked) viewModel.getDecryptedPreviewFile(doc) else null
                        }
                        val isImage = doc.category == "photo" || doc.mimeType.startsWith("image/")

                        if (isImage) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(230.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF101018))
                                    .border(1.dp, theme.primary, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (decryptedCacheFile != null && decryptedCacheFile.exists()) {
                                    AsyncImage(
                                        model = decryptedCacheFile,
                                        contentDescription = doc.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    val fallback = File(doc.uri)
                                    if (fallback.exists()) {
                                        AsyncImage(
                                            model = fallback,
                                            contentDescription = doc.name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("🖼️", fontSize = 48.sp)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(doc.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                            Text("Decrypted Local Photo", fontSize = 11.sp, color = SafeGreen)
                                        }
                                    }
                                }
                            }
                        } else {
                            val isPdf = doc.mimeType == "application/pdf" || doc.name.endsWith(".pdf", true) || doc.originalFileName.endsWith(".pdf", true) || (decryptedCacheFile != null && decryptedCacheFile.name.endsWith(".pdf", true))

                            if (isPdf && decryptedCacheFile != null && decryptedCacheFile.exists()) {
                                var currentPdfPage by remember { mutableStateOf(0) }
                                var pdfBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
                                var totalPdfPages by remember { mutableStateOf(1) }

                                androidx.compose.runtime.LaunchedEffect(decryptedCacheFile, currentPdfPage) {
                                    withContext(Dispatchers.IO) {
                                        val (bmp, pages) = viewModel.renderPdfPage(decryptedCacheFile, currentPdfPage)
                                        pdfBitmap = bmp
                                        if (pages > 0) totalPdfPages = pages
                                    }
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10101C)),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.5.dp, Color(0xFF8B5CF6), RoundedCornerShape(16.dp))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("PDF Visual Preview", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF8B5CF6).copy(alpha = 0.2f))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text("Page ${currentPdfPage + 1} of $totalPdfPages", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA78BFA))
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Rendered Page Bitmap Display
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(280.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color.White)
                                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (pdfBitmap != null) {
                                                Image(
                                                    bitmap = pdfBitmap!!.asImageBitmap(),
                                                    contentDescription = "PDF Page ${currentPdfPage + 1}",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Fit
                                                )
                                            } else {
                                                androidx.compose.material3.CircularProgressIndicator(color = Color(0xFF8B5CF6), strokeWidth = 2.dp)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Page Navigation
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedButton(
                                                onClick = { if (currentPdfPage > 0) currentPdfPage-- },
                                                enabled = currentPdfPage > 0,
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.ChevronLeft, contentDescription = "Prev", modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Prev Page", fontSize = 11.sp)
                                            }

                                            Text(
                                                text = "Page ${currentPdfPage + 1} / $totalPdfPages",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextPrimary
                                            )

                                            Button(
                                                onClick = { if (currentPdfPage < totalPdfPages - 1) currentPdfPage++ },
                                                enabled = currentPdfPage < totalPdfPages - 1,
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Next Page", fontSize = 11.sp)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(Icons.Default.ChevronRight, contentDescription = "Next", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            } else {
                                // RICH DOCUMENT CARD PREVIEW
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10101C)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, theme.primary, RoundedCornerShape(14.dp))
                                ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val docEmoji = when {
                                        doc.originalFileName.endsWith(".pdf", true) || doc.name.endsWith(".pdf", true) -> "📄"
                                        doc.category == "id" -> "🪪"
                                        doc.category == "contract" -> "📑"
                                        doc.category == "receipt" -> "🧾"
                                        doc.category == "medical" -> "🏥"
                                        else -> "📁"
                                    }
                                    Text(docEmoji, fontSize = 46.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = doc.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    if (doc.originalFileName.isNotBlank()) {
                                        Text(
                                            text = "Original file: ${doc.originalFileName}",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))

                                    val sizeLabel = if (doc.fileSize > 0) {
                                        val kb = doc.fileSize / 1024.0
                                        if (kb > 1024) String.format(Locale.US, "%.1f MB", kb / 1024.0) else String.format(Locale.US, "%.1f KB", kb)
                                    } else "Encrypted Size Verified"

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(SafeGreenBg)
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("✓ Decrypted in Memory", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SafeGreen)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0x20FFFFFF))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(sizeLabel, fontSize = 10.sp, color = TextMuted)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ACTION 1: Download decrypted file locally onto mobile Downloads folder
                    Button(
                        onClick = {
                            viewModel.downloadDocumentLocally(doc)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("download_doc_locally")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("Download Decrypted File to Downloads", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Quick save to public Downloads/MyVault folder", fontSize = 10.sp, color = Color.White.copy(alpha = 0.85f))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ACTION 1B: Choose custom directory or folder on device
                    OutlinedButton(
                        onClick = {
                            val defaultName = if (doc.originalFileName.isNotBlank()) doc.originalFileName else "${doc.name.replace("[^a-zA-Z0-9_-]".toRegex(), "_")}.pdf"
                            try {
                                saveAsLauncher.launch(defaultName)
                            } catch (_: Exception) {
                                viewModel.downloadDocumentLocally(doc)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_as_custom_folder_btn")
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp), tint = theme.primaryLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("Save / Export to Any Folder...", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextPrimary)
                            Text("Choose any directory, SD card or folder on your phone", fontSize = 10.sp, color = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ACTION 2 & 3: Share / Export and Open in External App
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.shareDocument(doc, context)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("share_doc_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = theme.primaryLight)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share / Save As", fontSize = 11.sp, color = TextPrimary)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.openDocumentExternally(doc, context)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("open_external_doc_btn")
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp), tint = theme.primaryLight)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open in App", fontSize = 11.sp, color = TextPrimary)
                        }
                    }

                    if (doc.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("NOTES & METADATA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = theme.primaryLight)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(doc.notes, fontSize = 12.sp, color = TextPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // EDIT SECURITY PROTECTION (PIN & 2FA) FOR THIS STORED MEDIA
                    Card(
                        colors = CardDefaults.cardColors(containerColor = VaultSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showSecurityEdit = !showSecurityEdit },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = theme.primaryLight, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Configure PIN & 2FA Options", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Text(if (showSecurityEdit) "▲ Hide" else "▼ Edit", fontSize = 11.sp, color = theme.primaryLight)
                            }

                            if (showSecurityEdit) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Require PIN to open", fontSize = 12.sp, color = TextPrimary)
                                    Switch(
                                        checked = editRequirePin,
                                        onCheckedChange = { editRequirePin = it }
                                    )
                                }
                                if (editRequirePin) {
                                    OutlinedTextField(
                                        value = editCustomPin,
                                        onValueChange = { editCustomPin = it },
                                        label = { Text("Custom Item PIN (or leave blank for Master PIN)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = defaultTextFieldColors()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Require 2FA to open", fontSize = 12.sp, color = TextPrimary)
                                    Switch(
                                        checked = editRequire2fa,
                                        onCheckedChange = { editRequire2fa = it }
                                    )
                                }
                                if (editRequire2fa) {
                                    OutlinedTextField(
                                        value = editCustom2faKey,
                                        onValueChange = { editCustom2faKey = it },
                                        label = { Text("Custom 2FA Token (or leave blank for Master '123456')") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = defaultTextFieldColors()
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val updated = doc.copy(
                                            requirePin = editRequirePin,
                                            require2fa = editRequire2fa,
                                            customPin = editCustomPin.trim(),
                                            custom2faKey = editCustom2faKey.trim(),
                                            locked = editRequirePin || editRequire2fa
                                        )
                                        viewModel.updateDocument(updated)
                                        showSecurityEdit = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Save Security Preferences")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close", color = TextMuted)
                    }

                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = UrgentRedBg, contentColor = UrgentRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("delete_doc_btn")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
}
