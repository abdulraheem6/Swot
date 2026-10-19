package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VaultItem
import com.example.data.SecureDocument
import com.example.data.MonthlyBudget
import com.example.ui.VaultViewModel
import com.example.ui.components.AddContactSheet
import com.example.ui.components.AddDocumentSheet
import com.example.ui.components.AddItemSheet
import com.example.ui.components.AddMonthlyPaymentDialog
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.EditItemSheet
import com.example.ui.components.AuditLogsDialog
import com.example.ui.components.BiometricGateDialog
import com.example.ui.components.DocumentDetailDialog
import com.example.ui.components.EmergencyEditSheet
import com.example.ui.components.EmergencyViewSheet
import com.example.ui.components.LockScreen
import com.example.ui.components.ProfileDialog
import com.example.ui.components.ReceiptViewerDialog
import com.example.ui.components.RegimeDialog
import com.example.ui.components.ResetPinDialog
import com.example.ui.components.ScanResult
import com.example.ui.components.ScannerDialog
import com.example.ui.components.SetBudgetDialog
import com.example.ui.components.TrashSheet
import com.example.ui.screens.AddScheduleSheet
import com.example.ui.screens.AddSecretSheet
import com.example.ui.screens.AudioScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SecretDetailSheet
import com.example.ui.screens.SecretsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.BorderAccent
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VaultBg
import com.example.ui.theme.VaultSurface

class MainActivity : ComponentActivity() {

    private val viewModel: VaultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsState()
            MyApplicationTheme(themeId = settings.themeName) {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: VaultViewModel) {
    val theme = LocalAppTheme.current
    val settings by viewModel.settings.collectAsState()
    val isLocked by viewModel.isLocked.collectAsState()
    val pinBuffer by viewModel.pinBuffer.collectAsState()
    val pinError by viewModel.pinError.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val currentlyPlayingTone by viewModel.currentlyPlayingTone.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    // Dialog sheets states
    val showEmergencyDialog by viewModel.showEmergencyDialog.collectAsState()
    val showEmergencyEditDialog by viewModel.showEmergencyEditDialog.collectAsState()
    val emergencyInfo by viewModel.emergency.collectAsState()
    val showAddItemDialog by viewModel.showAddItemDialog.collectAsState()
    val editingItem by viewModel.editingItem.collectAsState()
    val showAddSecretDialog by viewModel.showAddSecretDialog.collectAsState()
    val selectedSecret by viewModel.selectedSecret.collectAsState()
    val showAddScheduleDialog by viewModel.showAddScheduleDialog.collectAsState()
    val showTrashDialog by viewModel.showTrashDialog.collectAsState()
    val trashItems by viewModel.trash.collectAsState()
    val showScannerDialog by viewModel.showScannerDialog.collectAsState()
    val scannerMode by viewModel.scannerMode.collectAsState()
    val showBiometricGate by viewModel.showBiometricGate.collectAsState()

    // Profile & Document Dialogs
    val showProfileDialog by viewModel.showProfileDialog.collectAsState()
    val showAuditLogsDialog by viewModel.showAuditLogsDialog.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val showAddDocumentDialog by viewModel.showAddDocumentDialog.collectAsState()
    val showMultiPageScannerDialog by viewModel.showMultiPageScannerDialog.collectAsState()
    val selectedDocument by viewModel.selectedDocument.collectAsState()
    val showRegimeDialog by viewModel.showRegimeDialog.collectAsState()
    val showResetPinDialog by viewModel.showResetPinDialog.collectAsState()
    val showAddContactDialog by viewModel.showAddContactDialog.collectAsState()
    val showAddRecipeDialog by viewModel.showAddRecipeDialog.collectAsState()

    // Money Dialog States
    val showAddTransactionDialog by viewModel.showAddTransactionDialog.collectAsState()
    val editingTransaction by viewModel.editingTransaction.collectAsState()
    val defaultTransactionType by viewModel.defaultTransactionType.collectAsState()
    val showBudgetDialog by viewModel.showBudgetDialog.collectAsState()
    val showAddMonthlyPaymentDialog by viewModel.showAddMonthlyPaymentDialog.collectAsState()
    val editingMonthlyPayment by viewModel.editingMonthlyPayment.collectAsState()
    val viewingReceiptTransaction by viewModel.viewingReceiptTransaction.collectAsState()
    val monthlyBudgets by viewModel.monthlyBudgets.collectAsState()
    val activeMoneyMonth by viewModel.activeMoneyMonth.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        if (isLocked) {
            LockScreen(
                viewModel = viewModel,
                pinBuffer = pinBuffer,
                pinError = pinError,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = theme.background,
                bottomBar = {
                    NavigationBar(
                        containerColor = theme.surface,
                        contentColor = TextPrimary,
                        modifier = Modifier
                            .navigationBarsPadding()
                            .testTag("bottom_nav_bar")
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { viewModel.setTab(0) },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = theme.primaryLight,
                                selectedTextColor = theme.primaryLight,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                                indicatorColor = theme.primaryContainer
                            )
                        )

                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { viewModel.setTab(1) },
                            icon = { Icon(Icons.Default.Lock, contentDescription = "Secrets") },
                            label = { Text("Secrets", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = theme.primaryLight,
                                selectedTextColor = theme.primaryLight,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                                indicatorColor = theme.primaryContainer
                            )
                        )

                        NavigationBarItem(
                            selected = false,
                            onClick = { viewModel.openAddItemDialog() },
                            icon = {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(theme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Item", tint = theme.primaryLight)
                                }
                            },
                            label = { Text("Add", fontSize = 10.sp, color = theme.primaryLight) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = theme.primaryLight,
                                selectedTextColor = theme.primaryLight,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                                indicatorColor = theme.primaryContainer
                            )
                        )

                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { viewModel.setTab(2) },
                            icon = { Icon(Icons.Default.VolumeUp, contentDescription = "Audio") },
                            label = { Text("Audio", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = theme.primaryLight,
                                selectedTextColor = theme.primaryLight,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                                indicatorColor = theme.primaryContainer
                            )
                        )

                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { viewModel.setTab(3) },
                            icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "More") },
                            label = { Text("More", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = theme.primaryLight,
                                selectedTextColor = theme.primaryLight,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                                indicatorColor = theme.primaryContainer
                            )
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (selectedTab) {
                        0 -> HomeScreen(viewModel = viewModel)
                        1 -> SecretsScreen(viewModel = viewModel)
                        2 -> AudioScreen(viewModel = viewModel)
                        3 -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }

        // Floating Audio Playing Banner
        AnimatedVisibility(
            visible = currentlyPlayingTone != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 10.dp, start = 16.dp, end = 16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.primary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderAccent, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔊", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "NOW PLAYING TONE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xCCFFFFFF)
                            )
                            Text(
                                text = currentlyPlayingTone?.replaceFirstChar { it.uppercase() } ?: "",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.stopAudioTone() },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x33FFFFFF))
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Floating Toast Message
        AnimatedVisibility(
            visible = toastMessage != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp, start = 24.dp, end = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xE62A2A3A))
                    .border(1.dp, theme.primary, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = toastMessage ?: "",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Emergency Dialogs
        if (showEmergencyDialog) {
            EmergencyViewSheet(
                info = emergencyInfo,
                onDismiss = { viewModel.closeEmergencyDialog() },
                onEditClicked = { viewModel.openEmergencyEditDialog() },
                viewModel = viewModel
            )
        }

        if (showEmergencyEditDialog) {
            EmergencyEditSheet(
                initialInfo = emergencyInfo,
                onDismiss = { viewModel.closeEmergencyEditDialog() },
                onSave = { updated ->
                    viewModel.repository.saveEmergency(updated)
                    viewModel.closeEmergencyEditDialog()
                    viewModel.showToast("✅ Emergency info saved")
                }
            )
        }

        // Add Vault Item Dialog
        if (showAddItemDialog) {
            AddItemSheet(
                onDismiss = { viewModel.closeAddItemDialog() },
                onSave = { newItem ->
                    viewModel.addItem(newItem)
                    viewModel.closeAddItemDialog()
                },
                viewModel = viewModel
            )
        }

        // Edit Vault Item Dialog
        editingItem?.let { itemToEdit ->
            EditItemSheet(
                item = itemToEdit,
                onDismiss = { viewModel.closeEditItemDialog() },
                onSave = { updatedItem ->
                    viewModel.updateItem(updatedItem)
                    viewModel.closeEditItemDialog()
                },
                onDelete = {
                    viewModel.deleteItem(itemToEdit.id, itemToEdit.name)
                    viewModel.closeEditItemDialog()
                },
                viewModel = viewModel
            )
        }

        // Add Secret Dialog
        if (showAddSecretDialog) {
            AddSecretSheet(
                onDismiss = { viewModel.closeAddSecretDialog() },
                onSave = { newSecret ->
                    viewModel.repository.addSecret(newSecret)
                    viewModel.closeAddSecretDialog()
                    viewModel.showToast("✅ Secret encrypted & saved")
                },
                viewModel = viewModel
            )
        }

        // Secret Detail Dialog
        selectedSecret?.let { secret ->
            SecretDetailSheet(
                secret = secret,
                onDismiss = { viewModel.closeSecretDetail() },
                onDelete = {
                    viewModel.repository.deleteSecret(secret.id)
                    viewModel.closeSecretDetail()
                    viewModel.showToast("🗑️ Moved '${secret.title}' to trash")
                },
                viewModel = viewModel
            )
        }

        // Add Audio Schedule Dialog
        if (showAddScheduleDialog) {
            AddScheduleSheet(
                onDismiss = { viewModel.closeAddScheduleDialog() },
                onSave = { newSched ->
                    viewModel.repository.addSchedule(newSched)
                    viewModel.closeAddScheduleDialog()
                    viewModel.showToast("✅ Scheduled reminder '${newSched.name}'")
                },
                viewModel = viewModel
            )
        }

        // Trash Dialog
        if (showTrashDialog) {
            TrashSheet(
                trashItems = trashItems,
                onDismiss = { viewModel.closeTrashDialog() },
                viewModel = viewModel
            )
        }

        // Scanner Dialog
        if (showScannerDialog) {
            ScannerDialog(
                mode = scannerMode,
                onDismiss = { viewModel.closeScannerDialog() },
                viewModel = viewModel,
                onScanned = { result ->
                    viewModel.closeScannerDialog()
                    viewModel.closeAddItemDialog()
                    val scannedItem = VaultItem(
                        id = java.util.UUID.randomUUID().toString(),
                        name = result.name,
                        category = result.category,
                        date = result.date,
                        cardNumber = result.cardNumber,
                        notes = result.notes,
                        attachments = if (result.photoUri != null) listOf(result.photoUri) else emptyList(),
                        tags = result.tags,
                        customFields = result.customFields
                    )
                    viewModel.repository.addItem(scannedItem)

                    // Also add to Secure Documents if an image was attached
                    if (result.photoUri != null) {
                        val doc = SecureDocument(
                            id = java.util.UUID.randomUUID().toString(),
                            name = "${result.name} (Scan)",
                            category = if (result.category == "card") "photo" else "receipt",
                            uri = result.photoUri,
                            fileSize = 1024 * 380,
                            notes = result.notes,
                            locked = false,
                            customFields = result.customFields
                        )
                        viewModel.repository.addDocument(doc)
                    }

                    viewModel.addAuditLog("SCAN_SUCCESS", "Captured & encrypted ${result.category} '${result.name}'")
                    viewModel.showToast("✨ Scanned & saved '${scannedItem.name}'")
                }
            )
        }

        // Biometric Security Gate Dialog
        if (showBiometricGate) {
            BiometricGateDialog(
                onConfirm = { viewModel.confirmBiometricGate() },
                onCancel = { viewModel.cancelBiometricGate() }
            )
        }

        // User Profile & Identity Dialog
        if (showProfileDialog) {
            ProfileDialog(
                settings = settings,
                onDismiss = { viewModel.closeProfileDialog() },
                onSave = { username, org, dept, empId, clearance, email ->
                    viewModel.updateProfile(username, org, dept, empId, clearance, email)
                    viewModel.closeProfileDialog()
                }
            )
        }

        // Security Audit Trail Dialog
        if (showAuditLogsDialog) {
            AuditLogsDialog(
                logs = auditLogs,
                onDismiss = { viewModel.closeAuditLogsDialog() },
                viewModel = viewModel
            )
        }

        // Add Document / Photo Sheet
        if (showAddDocumentDialog) {
            AddDocumentSheet(
                onDismiss = { viewModel.closeAddDocumentDialog() },
                onSave = { newDoc ->
                    viewModel.repository.addDocument(newDoc)
                    viewModel.closeAddDocumentDialog()
                    viewModel.showToast("📁 Saved '${newDoc.name}'")
                },
                viewModel = viewModel
            )
        }

        // Multi-Page Document Scanner & PDF Compiler Dialog
        if (showMultiPageScannerDialog) {
            com.example.ui.components.MultiPageScannerDialog(
                onDismiss = { viewModel.closeMultiPageScannerDialog() },
                viewModel = viewModel
            )
        }

        // Document Detail Dialog (Password & 2FA Protected)
        selectedDocument?.let { doc ->
            DocumentDetailDialog(
                doc = doc,
                viewModel = viewModel,
                onDismiss = { viewModel.closeDocumentDetail() },
                onDelete = {
                    viewModel.repository.deleteDocument(doc.id)
                    viewModel.closeDocumentDetail()
                    viewModel.showToast("🗑️ Moved '${doc.name}' to trash")
                }
            )
        }

        // Security & Defense Regime Dialog
        if (showRegimeDialog) {
            RegimeDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.closeRegimeDialog() }
            )
        }

        // Reset Login PIN (6-Digits) Dialog
        if (showResetPinDialog) {
            ResetPinDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.closeResetPinDialog() }
            )
        }

        // Add Contact Profile Sheet
        if (showAddContactDialog) {
            AddContactSheet(
                onDismiss = { viewModel.closeAddContactDialog() },
                onSave = { newContact ->
                    viewModel.addContact(newContact)
                    viewModel.closeAddContactDialog()
                },
                viewModel = viewModel
            )
        }

        // Add or Edit Transaction Dialog (Expense & Income)
        if (showAddTransactionDialog) {
            AddTransactionDialog(
                initialItem = editingTransaction,
                defaultType = defaultTransactionType,
                onDismiss = { viewModel.closeAddTransactionDialog() },
                onSave = { tx ->
                    if (editingTransaction != null) {
                        viewModel.updateTransaction(tx)
                    } else {
                        viewModel.addTransaction(tx)
                    }
                    viewModel.closeAddTransactionDialog()
                }
            )
        }

        // Monthly Budget Planner Dialog
        if (showBudgetDialog) {
            val currentBudget = monthlyBudgets[activeMoneyMonth] ?: MonthlyBudget(month = activeMoneyMonth)
            SetBudgetDialog(
                currentBudget = currentBudget,
                onDismiss = { viewModel.closeBudgetDialog() },
                onSave = { budget ->
                    viewModel.setMonthlyBudget(budget)
                    viewModel.closeBudgetDialog()
                }
            )
        }

        // Add or Edit Monthly Recurring Payment / Bill Dialog
        if (showAddMonthlyPaymentDialog) {
            AddMonthlyPaymentDialog(
                initialItem = editingMonthlyPayment,
                onDismiss = { viewModel.closeAddMonthlyPaymentDialog() },
                onSave = { bill ->
                    if (editingMonthlyPayment != null) {
                        viewModel.updateMonthlyPayment(bill)
                    } else {
                        viewModel.addMonthlyPayment(bill)
                    }
                    viewModel.closeAddMonthlyPaymentDialog()
                }
            )
        }

        // Receipt Proof Viewer Dialog
        viewingReceiptTransaction?.let { tx ->
            ReceiptViewerDialog(
                item = tx,
                onDismiss = { viewModel.closeReceiptViewer() }
            )
        }

        // Kitchen Add Recipe Dialog (Triggered from Kitchen tile + button)
        if (showAddRecipeDialog) {
            com.example.ui.screens.kitchen.AddEditRecipeDialog(
                onDismiss = { viewModel.closeAddRecipeDialog() },
                onSave = { newRecipe ->
                    viewModel.addRecipe(newRecipe)
                    viewModel.closeAddRecipeDialog()
                }
            )
        }
    }
}
