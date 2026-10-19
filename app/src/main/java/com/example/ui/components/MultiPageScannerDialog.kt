package com.example.ui.components

import android.net.Uri
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiPageScannerDialog(
    onDismiss: () -> Unit,
    onDocumentSaved: (SecureDocument) -> Unit = {},
    viewModel: VaultViewModel
) {
    val context = LocalContext.current
    val theme = LocalAppTheme.current
    val scope = rememberCoroutineScope()

    val defaultTitle = remember {
        "Scanned_Document_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())}"
    }
    var docTitle by remember { mutableStateOf(defaultTitle) }
    var selectedCategory by remember { mutableStateOf("document") }
    var notes by remember { mutableStateOf("") }

    val initialUris by viewModel.multiPageScannerInitialUris.collectAsState()
    var pages by remember(initialUris) { mutableStateOf(initialUris) }
    var isCompiling by remember { mutableStateOf(false) }

    // Security Gate Controls
    var requirePin by remember { mutableStateOf(true) }
    var customPin by remember { mutableStateOf("") }
    var require2fa by remember { mutableStateOf(false) }
    var custom2faKey by remember { mutableStateOf("") }

    val categories = listOf(
        "document" to "📄 Legal & Financial Documents",
        "id" to "🪪 Government ID / Passport",
        "contract" to "📑 Confidential Agreements",
        "receipt" to "🧾 Tax & Business Receipts",
        "medical" to "🏥 Health & Medical Records",
        "insurance" to "🛡️ Insurance Policies"
    )

    // Camera capture temporary file tracking
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCameraUri != null) {
            pages = pages + pendingCameraUri!!
            pendingCameraUri = null
        }
    }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 30)
    ) { uris ->
        if (uris.isNotEmpty()) {
            pages = pages + uris
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            pages = pages + uris
        }
    }

    fun launchCamera() {
        try {
            val scannerDir = File(context.cacheDir, "scanner").apply { mkdirs() }
            val tempFile = File(scannerDir, "cam_page_${System.currentTimeMillis()}_${pages.size + 1}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            viewModel.showToast("⚠️ Could not launch camera: ${e.message}")
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surface),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .border(1.5.dp, Color(0xFF8B5CF6), RoundedCornerShape(24.dp))
                .testTag("multi_page_scanner_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF8B5CF6).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFF8B5CF6), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Multi-Page Document Scanner",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Compile multiple photos into a single encrypted PDF",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(theme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable content body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Feature Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF8B5CF6).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("📑 PDF Compiler", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA78BFA))
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SafeGreenBg)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("🛡️ Zero-Cloud Local", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SafeGreen)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x22FFFFFF))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("🔒 AES-256-GCM", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Document Title
                    OutlinedTextField(
                        value = docTitle,
                        onValueChange = { docTitle = it },
                        label = { Text("Document / PDF Title") },
                        placeholder = { Text("e.g. Insurance Policy Full Terms, Passport & Visa") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("scanner_doc_title_input"),
                        colors = defaultTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Target Folder / Category
                    var catExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = catExpanded,
                        onExpandedChange = { catExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = categories.find { it.first == selectedCategory }?.second ?: "📄 Document",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Save in Folder / Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            colors = defaultTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = catExpanded,
                            onDismissRequest = { catExpanded = false },
                            containerColor = VaultSurfaceVariant
                        ) {
                            categories.forEach { (catId, catLabel) ->
                                DropdownMenuItem(
                                    text = { Text(catLabel, color = TextPrimary) },
                                    onClick = {
                                        selectedCategory = catId
                                        catExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pages Section Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Scanned Document Pages", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF8B5CF6))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${pages.size} ${if (pages.size == 1) "Page" else "Pages"}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        if (pages.isNotEmpty()) {
                            androidx.compose.material3.TextButton(
                                onClick = { pages = emptyList() }
                            ) {
                                Text("Clear All", fontSize = 11.sp, color = UrgentRed)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Pages Carousel / Grid Preview
                    if (pages.isEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF10101C)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("📷 No document pages captured yet", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Take camera photos of physical pages or select multiple images from your photo gallery",
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            itemsIndexed(pages) { index, uri ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141420)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .size(110.dp, 150.dp)
                                        .border(1.5.dp, Color(0xFF8B5CF6), RoundedCornerShape(12.dp))
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = "Page ${index + 1}",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )

                                        // Page Number Badge
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(bottomEnd = 8.dp))
                                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "Page ${index + 1}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFA78BFA)
                                            )
                                        }

                                        // Remove Page Button
                                        IconButton(
                                            onClick = {
                                                pages = pages.filterIndexed { i, _ -> i != index }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(26.dp)
                                                .padding(2.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.7f))
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(14.dp))
                                        }

                                        // Reorder controls (left / right)
                                        if (pages.size > 1) {
                                            Row(
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .fillMaxWidth()
                                                    .background(Color.Black.copy(alpha = 0.65f))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (index > 0) {
                                                    IconButton(
                                                        onClick = {
                                                            val mutable = pages.toMutableList()
                                                            val item = mutable.removeAt(index)
                                                            mutable.add(index - 1, item)
                                                            pages = mutable
                                                        },
                                                        modifier = Modifier.size(20.dp)
                                                    ) {
                                                        Icon(Icons.Default.ArrowBack, contentDescription = "Move Left", tint = Color.White, modifier = Modifier.size(12.dp))
                                                    }
                                                } else {
                                                    Spacer(modifier = Modifier.size(20.dp))
                                                }

                                                if (index < pages.size - 1) {
                                                    IconButton(
                                                        onClick = {
                                                            val mutable = pages.toMutableList()
                                                            val item = mutable.removeAt(index)
                                                            mutable.add(index + 1, item)
                                                            pages = mutable
                                                        },
                                                        modifier = Modifier.size(20.dp)
                                                    ) {
                                                        Icon(Icons.Default.ArrowForward, contentDescription = "Move Right", tint = Color.White, modifier = Modifier.size(12.dp))
                                                    }
                                                } else {
                                                    Spacer(modifier = Modifier.size(20.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Page Capture Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Action 1: Camera Photo Capture
                        Button(
                            onClick = { launchCamera() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("scanner_camera_button")
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Snap Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Action 2: Gallery Multi-Select
                        OutlinedButton(
                            onClick = {
                                galleryPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("scanner_gallery_button")
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFFA78BFA))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add from Gallery", fontSize = 11.sp, color = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action 3: Files / Storage Browser
                    OutlinedButton(
                        onClick = {
                            try {
                                filePickerLauncher.launch(arrayOf("image/*"))
                            } catch (_: Exception) {}
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("scanner_file_picker_button")
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextMuted)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Browse Device Storage for Page Images", fontSize = 11.sp, color = TextMuted)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Security & Encryption Gates
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
                                Text("SECURITY & ENCLAVE ACCESS", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = theme.primaryLight)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (requirePin && require2fa) UrgentRedBg else if (requirePin) theme.primaryContainer else SafeGreenBg)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = when {
                                            requirePin && require2fa -> "🛡️ PIN + 2FA"
                                            requirePin -> "🔒 PIN Locked"
                                            require2fa -> "🔑 2FA Gated"
                                            else -> "🔓 Direct Access"
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

                            // PIN toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("🔒 Require PIN to Open PDF", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text("Enforces decryption PIN before viewing pages", fontSize = 11.sp, color = TextMuted)
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
                                    label = { Text("Custom PDF PIN (Leave blank for Master PIN)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = defaultTextFieldColors()
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 2FA toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("🔑 Require 2FA Token", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text("Enforces two-factor authentication challenge", fontSize = 11.sp, color = TextMuted)
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
                                    label = { Text("Custom 2FA Token (Leave blank for Master '123456')") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = defaultTextFieldColors()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Optional Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes & Metadata (Optional)") },
                        placeholder = { Text("Policy ID, expiration date, remarks") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = defaultTextFieldColors()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = TextMuted)
                    }

                    Button(
                        onClick = {
                            if (pages.isEmpty()) {
                                viewModel.showToast("⚠️ Please add at least 1 document page photo")
                                return@Button
                            }

                            isCompiling = true
                            scope.launch {
                                val created = withContext(Dispatchers.IO) {
                                    viewModel.compileAndSaveScannedPdf(
                                        imageUris = pages,
                                        docName = docTitle,
                                        category = selectedCategory,
                                        notes = notes,
                                        requirePin = requirePin,
                                        require2fa = require2fa,
                                        customPin = customPin,
                                        custom2faKey = custom2faKey
                                    )
                                }
                                isCompiling = false
                                if (created != null) {
                                    onDocumentSaved(created)
                                    onDismiss()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (pages.isNotEmpty()) SafeGreen else Color.Gray,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.8f).testTag("compile_and_save_pdf_btn"),
                        enabled = pages.isNotEmpty() && !isCompiling
                    ) {
                        if (isCompiling) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Compiling PDF...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (pages.isEmpty()) "Add Pages to Compile" else "Compile & Save PDF (${pages.size} P)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
