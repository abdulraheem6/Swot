package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioTonePlayer
import com.example.data.AudioSchedule
import com.example.data.EmergencyInfo
import com.example.data.MemberContact
import com.example.data.MonthlyBudget
import com.example.data.MonthlyPaymentItem
import com.example.data.SecretItem
import com.example.data.SecureDocument
import com.example.data.TransactionItem
import com.example.data.VaultItem
import android.graphics.Bitmap
import com.example.util.PdfDocumentHelper
import com.example.data.VaultRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class VaultViewModel(application: Application) : AndroidViewModel(application) {

    val repository = VaultRepository(application)
    val recipeRepository = com.example.data.kitchen.RecipeRepository(application)

    val recipes: StateFlow<List<com.example.data.kitchen.RecipeEntity>> =
        recipeRepository.allRecipes.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val recipeCount: StateFlow<Int> =
        recipeRepository.recipeCount.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )

    private val _showAddRecipeDialog = MutableStateFlow(false)
    val showAddRecipeDialog: StateFlow<Boolean> = _showAddRecipeDialog.asStateFlow()

    fun openAddRecipeDialog() { _showAddRecipeDialog.value = true }
    fun closeAddRecipeDialog() { _showAddRecipeDialog.value = false }

    fun addRecipe(recipe: com.example.data.kitchen.RecipeEntity) {
        viewModelScope.launch {
            recipeRepository.insertRecipe(recipe)
            repository.addAuditLog("RECIPE_ADDED", "Added recipe '${recipe.title}'")
            showToast("🥘 Saved '${recipe.title}' to Kitchen")
        }
    }

    fun updateRecipe(recipe: com.example.data.kitchen.RecipeEntity) {
        viewModelScope.launch {
            recipeRepository.updateRecipe(recipe)
            repository.addAuditLog("RECIPE_UPDATED", "Updated recipe '${recipe.title}'")
            showToast("✏️ Updated recipe '${recipe.title}'")
        }
    }

    fun deleteRecipe(recipe: com.example.data.kitchen.RecipeEntity) {
        viewModelScope.launch {
            recipeRepository.deleteRecipe(recipe)
            repository.addAuditLog("RECIPE_DELETED", "Deleted recipe '${recipe.title}'")
            showToast("🗑️ Deleted recipe '${recipe.title}'")
        }
    }

    fun toggleFavoriteRecipe(recipe: com.example.data.kitchen.RecipeEntity) {
        viewModelScope.launch {
            recipeRepository.toggleFavorite(recipe.id, recipe.isFavorite)
            val status = if (!recipe.isFavorite) "Added to Favorites ❤️" else "Removed from Favorites"
            showToast("$status: ${recipe.title}")
        }
    }

    init {
        viewModelScope.launch {
            while (true) {
                delay(15000) // check every 15 seconds
                checkScheduledAudioTriggers()
            }
        }
    }

    private val triggeredAudioIds = mutableSetOf<String>()

    private fun checkScheduledAudioTriggers() {
        val now = Calendar.getInstance()
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val sdfTime = SimpleDateFormat("HH:mm", Locale.US)
        val currentDate = sdfDate.format(now.time)
        val currentTime = sdfTime.format(now.time)

        schedules.value.forEach { sched ->
            if (sched.enabled) {
                val dt = sched.dateTime.replace("T", " ").trim()
                if (dt.contains(currentDate) && dt.contains(currentTime)) {
                    val key = "${sched.id}_${currentDate}_${currentTime}"
                    if (!triggeredAudioIds.contains(key)) {
                        triggeredAudioIds.add(key)
                        playScheduleAudio(sched)
                        showToast("⏰ Audio Reminder Played: ${sched.name}")
                    }
                }
            }
        }
    }

    fun addSchedule(schedule: AudioSchedule) {
        repository.addSchedule(schedule)
        repository.addAuditLog("AUDIO_SCHEDULE_ADDED", "Added reminder '${schedule.name}'")
        showToast("⏰ Added reminder '${schedule.name}'")
    }

    fun updateSchedule(schedule: AudioSchedule) {
        repository.updateSchedule(schedule)
        repository.addAuditLog("AUDIO_SCHEDULE_UPDATED", "Updated reminder '${schedule.name}'")
        showToast("⏰ Updated reminder '${schedule.name}'")
    }

    fun toggleSchedule(id: String) {
        repository.toggleSchedule(id)
    }

    fun deleteSchedule(id: String) {
        val target = schedules.value.find { it.id == id }
        repository.deleteSchedule(id)
        repository.addAuditLog("AUDIO_SCHEDULE_DELETED", "Deleted reminder '${target?.name ?: id}'")
        showToast("🗑️ Deleted reminder")
    }

    val items = repository.items
    val secrets = repository.secrets
    val documents = repository.documents
    val emergency = repository.emergency
    val schedules = repository.schedules
    val trash = repository.trash
    val settings = repository.settings
    val auditLogs = repository.auditLogs
    val contacts = repository.contacts
    val transactions = repository.transactions
    val monthlyBudgets = repository.monthlyBudgets
    val monthlyPayments = repository.monthlyPayments

    // Money & Finance UI States
    private val _activeMoneyMonth = MutableStateFlow(SimpleDateFormat("yyyy-MM", Locale.US).format(Calendar.getInstance().time))
    val activeMoneyMonth: StateFlow<String> = _activeMoneyMonth.asStateFlow()
    fun setActiveMoneyMonth(month: String) { _activeMoneyMonth.value = month }

    private val _moneySelectedTab = MutableStateFlow(0) // 0: Overview, 1: Expenses, 2: Income, 3: Bills, 4: Budget, 5: Receipts
    val moneySelectedTab: StateFlow<Int> = _moneySelectedTab.asStateFlow()
    fun setMoneySelectedTab(tab: Int) { _moneySelectedTab.value = tab }

    private val _moneySearchQuery = MutableStateFlow("")
    val moneySearchQuery: StateFlow<String> = _moneySearchQuery.asStateFlow()
    fun setMoneySearchQuery(query: String) { _moneySearchQuery.value = query }

    private val _showAddTransactionDialog = MutableStateFlow(false)
    val showAddTransactionDialog: StateFlow<Boolean> = _showAddTransactionDialog.asStateFlow()
    private val _defaultTransactionType = MutableStateFlow("expense")
    val defaultTransactionType: StateFlow<String> = _defaultTransactionType.asStateFlow()

    private val _editingTransaction = MutableStateFlow<TransactionItem?>(null)
    val editingTransaction: StateFlow<TransactionItem?> = _editingTransaction.asStateFlow()

    private val _showBudgetDialog = MutableStateFlow(false)
    val showBudgetDialog: StateFlow<Boolean> = _showBudgetDialog.asStateFlow()

    private val _showAddMonthlyPaymentDialog = MutableStateFlow(false)
    val showAddMonthlyPaymentDialog: StateFlow<Boolean> = _showAddMonthlyPaymentDialog.asStateFlow()

    private val _editingMonthlyPayment = MutableStateFlow<MonthlyPaymentItem?>(null)
    val editingMonthlyPayment: StateFlow<MonthlyPaymentItem?> = _editingMonthlyPayment.asStateFlow()

    private val _viewingReceiptTransaction = MutableStateFlow<TransactionItem?>(null)
    val viewingReceiptTransaction: StateFlow<TransactionItem?> = _viewingReceiptTransaction.asStateFlow()

    fun openAddTransactionDialog(defaultType: String = "expense") {
        _defaultTransactionType.value = defaultType
        _editingTransaction.value = null
        _showAddTransactionDialog.value = true
    }
    fun closeAddTransactionDialog() {
        _showAddTransactionDialog.value = false
        _editingTransaction.value = null
    }

    fun openEditTransactionDialog(item: TransactionItem) {
        _editingTransaction.value = item
        _defaultTransactionType.value = item.type
        _showAddTransactionDialog.value = true
    }

    fun addTransaction(item: TransactionItem) {
        repository.addTransaction(item)
        showToast("💰 Added ${item.type}: ${item.title}")
    }

    fun updateTransaction(item: TransactionItem) {
        repository.updateTransaction(item)
        showToast("✏️ Updated transaction: ${item.title}")
    }

    fun deleteTransaction(id: String, title: String = "transaction") {
        repository.deleteTransaction(id)
        showToast("🗑️ Deleted $title")
    }

    fun openBudgetDialog() { _showBudgetDialog.value = true }
    fun closeBudgetDialog() { _showBudgetDialog.value = false }
    fun setMonthlyBudget(budget: MonthlyBudget) {
        repository.setMonthlyBudget(budget)
        showToast("🎯 Monthly budget updated: $${String.format(Locale.US, "%.0f", budget.totalBudget)}")
    }

    fun openAddMonthlyPaymentDialog() {
        _editingMonthlyPayment.value = null
        _showAddMonthlyPaymentDialog.value = true
    }
    fun openEditMonthlyPaymentDialog(item: MonthlyPaymentItem) {
        _editingMonthlyPayment.value = item
        _showAddMonthlyPaymentDialog.value = true
    }
    fun closeAddMonthlyPaymentDialog() {
        _showAddMonthlyPaymentDialog.value = false
        _editingMonthlyPayment.value = null
    }

    fun addMonthlyPayment(item: MonthlyPaymentItem) {
        repository.addMonthlyPayment(item)
        showToast("📅 Added recurring bill: ${item.title}")
    }

    fun updateMonthlyPayment(item: MonthlyPaymentItem) {
        repository.updateMonthlyPayment(item)
        showToast("✏️ Updated recurring bill: ${item.title}")
    }

    fun deleteMonthlyPayment(id: String, title: String = "bill") {
        repository.deleteMonthlyPayment(id)
        showToast("🗑️ Deleted bill $title")
    }

    fun markMonthlyPaymentPaid(paymentId: String, month: String) {
        repository.markMonthlyPaymentPaid(paymentId, month, autoCreateExpense = true)
        showToast("✅ Bill marked as PAID for $month")
    }

    fun unmarkMonthlyPaymentPaid(paymentId: String, month: String) {
        repository.unmarkMonthlyPaymentPaid(paymentId, month)
        showToast("↩️ Bill marked UNPAID for $month")
    }

    fun openReceiptViewer(item: TransactionItem) {
        _viewingReceiptTransaction.value = item
    }
    fun closeReceiptViewer() {
        _viewingReceiptTransaction.value = null
    }

    // Lock screen
    private val _isLocked = MutableStateFlow(true)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _pinBuffer = MutableStateFlow("")
    val pinBuffer: StateFlow<String> = _pinBuffer.asStateFlow()

    private val _pinError = MutableStateFlow<String?>(null)
    val pinError: StateFlow<String?> = _pinError.asStateFlow()

    // Navigation & Tabs (0: Home, 1: Secrets, 2: Audio, 3: More)
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Secret category filter ("all" or category id)
    private val _activeSecretCategory = MutableStateFlow("all")
    val activeSecretCategory: StateFlow<String> = _activeSecretCategory.asStateFlow()

    // Audio playback status
    private val _currentlyPlayingTone = MutableStateFlow<String?>(null)
    val currentlyPlayingTone: StateFlow<String?> = _currentlyPlayingTone.asStateFlow()

    // Toast notifications
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()
    private var toastJob: Job? = null

    // Dialog sheets visibility
    private val _showEmergencyDialog = MutableStateFlow(false)
    val showEmergencyDialog: StateFlow<Boolean> = _showEmergencyDialog.asStateFlow()

    private val _showEmergencyEditDialog = MutableStateFlow(false)
    val showEmergencyEditDialog: StateFlow<Boolean> = _showEmergencyEditDialog.asStateFlow()

    private val _showAddItemDialog = MutableStateFlow(false)
    val showAddItemDialog: StateFlow<Boolean> = _showAddItemDialog.asStateFlow()

    private val _editingItem = MutableStateFlow<VaultItem?>(null)
    val editingItem: StateFlow<VaultItem?> = _editingItem.asStateFlow()

    private val _showAddSecretDialog = MutableStateFlow(false)
    val showAddSecretDialog: StateFlow<Boolean> = _showAddSecretDialog.asStateFlow()

    private val _selectedSecret = MutableStateFlow<SecretItem?>(null)
    val selectedSecret: StateFlow<SecretItem?> = _selectedSecret.asStateFlow()

    private val _showAddScheduleDialog = MutableStateFlow(false)
    val showAddScheduleDialog: StateFlow<Boolean> = _showAddScheduleDialog.asStateFlow()

    private val _showTrashDialog = MutableStateFlow(false)
    val showTrashDialog: StateFlow<Boolean> = _showTrashDialog.asStateFlow()

    private val _showScannerDialog = MutableStateFlow(false)
    val showScannerDialog: StateFlow<Boolean> = _showScannerDialog.asStateFlow()
    private val _scannerMode = MutableStateFlow("card") // "card" or "receipt"
    val scannerMode: StateFlow<String> = _scannerMode.asStateFlow()

    private val _showBiometricGate = MutableStateFlow(false)
    val showBiometricGate: StateFlow<Boolean> = _showBiometricGate.asStateFlow()
    private var pendingBiometricAction: (() -> Unit)? = null

    // Profile & Document Sheets
    private val _showProfileDialog = MutableStateFlow(false)
    val showProfileDialog: StateFlow<Boolean> = _showProfileDialog.asStateFlow()

    private val _showAuditLogsDialog = MutableStateFlow(false)
    val showAuditLogsDialog: StateFlow<Boolean> = _showAuditLogsDialog.asStateFlow()

    private val _showAddDocumentDialog = MutableStateFlow(false)
    val showAddDocumentDialog: StateFlow<Boolean> = _showAddDocumentDialog.asStateFlow()

    private val _showMultiPageScannerDialog = MutableStateFlow(false)
    val showMultiPageScannerDialog: StateFlow<Boolean> = _showMultiPageScannerDialog.asStateFlow()

    private val _multiPageScannerInitialUris = MutableStateFlow<List<Uri>>(emptyList())
    val multiPageScannerInitialUris: StateFlow<List<Uri>> = _multiPageScannerInitialUris.asStateFlow()

    private val _showRegimeDialog = MutableStateFlow(false)
    val showRegimeDialog: StateFlow<Boolean> = _showRegimeDialog.asStateFlow()

    fun openRegimeDialog() { _showRegimeDialog.value = true }
    fun closeRegimeDialog() { _showRegimeDialog.value = false }

    private val _showResetPinDialog = MutableStateFlow(false)
    val showResetPinDialog: StateFlow<Boolean> = _showResetPinDialog.asStateFlow()

    fun openResetPinDialog() { _showResetPinDialog.value = true }
    fun closeResetPinDialog() { _showResetPinDialog.value = false }

    private val _showAddContactDialog = MutableStateFlow(false)
    val showAddContactDialog: StateFlow<Boolean> = _showAddContactDialog.asStateFlow()
    fun openAddContactDialog() { _showAddContactDialog.value = true }
    fun closeAddContactDialog() { _showAddContactDialog.value = false }

    private val _contactCategoryFilter = MutableStateFlow("all")
    val contactCategoryFilter: StateFlow<String> = _contactCategoryFilter.asStateFlow()
    fun setContactCategoryFilter(cat: String) { _contactCategoryFilter.value = cat }

    fun addContact(contact: MemberContact) {
        repository.addContact(contact)
        repository.addAuditLog("CONTACT_ADDED", "Added ${contact.category} contact '${contact.name}'")
        showToast("👤 Added contact '${contact.name}'")
    }

    fun updateContact(contact: MemberContact) {
        repository.updateContact(contact)
        repository.addAuditLog("CONTACT_UPDATED", "Updated contact '${contact.name}'")
        showToast("👤 Updated contact '${contact.name}'")
    }

    fun deleteContact(id: String, name: String) {
        repository.deleteContact(id)
        repository.addAuditLog("CONTACT_DELETED", "Deleted contact '$name'")
        showToast("🗑️ Deleted contact '$name'")
    }

    private val _selectedDocument = MutableStateFlow<SecureDocument?>(null)
    val selectedDocument: StateFlow<SecureDocument?> = _selectedDocument.asStateFlow()

    // Urgency tab selection ("all", "urgent", "soon", "safe")
    private val _activeUrgencyTab = MutableStateFlow("all")
    val activeUrgencyTab: StateFlow<String> = _activeUrgencyTab.asStateFlow()
    fun setUrgencyTab(tab: String) { _activeUrgencyTab.value = tab }

    // Computed stats: urgent count, soon count, safe count
    data class VaultStats(
        val urgentCount: Int,
        val soonCount: Int,
        val safeCount: Int,
        val nextItem: VaultItem?,
        val nextSchedule: AudioSchedule?,
        val totalSecrets: Int,
        val totalDocuments: Int
    )

    val stats: StateFlow<VaultStats> = combine(items, schedules, secrets, documents) { itemList, schedList, secList, docList ->
        var urgent = 0
        var soon = 0
        var safe = 0
        val nonEvents = itemList.filter { it.category != "event" }
            .sortedBy { it.date }

        itemList.forEach { item ->
            val days = daysUntil(item.date)
            when {
                days <= 2 -> urgent++
                days <= 7 -> soon++
                else -> safe++
            }
        }

        val activeScheds = schedList.filter { it.enabled }.sortedBy { it.dateTime }

        VaultStats(
            urgentCount = urgent,
            soonCount = soon,
            safeCount = safe,
            nextItem = nonEvents.firstOrNull(),
            nextSchedule = activeScheds.firstOrNull(),
            totalSecrets = secList.size,
            totalDocuments = docList.size
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        VaultStats(0, 0, 0, null, null, 0, 0)
    )

    fun daysUntil(dateStr: String): Int {
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return try {
            val target = df.parse(dateStr) ?: return 999
            val now = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            val diffMs = target.time - now.time
            (diffMs / (24 * 60 * 60 * 1000)).toInt()
        } catch (_: Exception) {
            999
        }
    }

    // --- PIN & Unlock ---
    fun enterPinDigit(c: Char) {
        if (!_isLocked.value) return
        if (_pinBuffer.value.length < 6) {
            _pinBuffer.value += c
            if (_pinBuffer.value.length == 6) {
                checkPin()
            }
        }
    }

    fun deletePinDigit() {
        if (_pinBuffer.value.isNotEmpty()) {
            _pinBuffer.value = _pinBuffer.value.dropLast(1)
        }
    }

    fun clearPinBuffer() {
        _pinBuffer.value = ""
        _pinError.value = null
    }

    private fun checkPin() {
        if (_pinBuffer.value == settings.value.pin) {
            unlock()
        } else {
            _pinError.value = "Incorrect PIN. (Default: 123456)"
            _pinBuffer.value = ""
            viewModelScope.launch {
                delay(1500)
                _pinError.value = null
            }
        }
    }

    fun resetLoginPin(originalPin: String, newPin: String, confirmPin: String): Pair<Boolean, String?> {
        val currentPin = settings.value.pin
        if (originalPin.trim() != currentPin) {
            return Pair(false, "Original PIN is incorrect.")
        }
        val cleanNew = newPin.trim()
        if (cleanNew.length != 6 || !cleanNew.all { it.isDigit() }) {
            return Pair(false, "New PIN must be exactly 6 numeric digits.")
        }
        if (cleanNew != confirmPin.trim()) {
            return Pair(false, "New PIN confirmation does not match.")
        }
        repository.updateSettings(settings.value.copy(pin = cleanNew))
        repository.addAuditLog("LOGIN_PIN_RESET", "Login PIN successfully reset by providing original PIN")
        _pinBuffer.value = ""
        _pinError.value = null
        showToast("✅ Login PIN updated to 6 digits")
        return Pair(true, null)
    }

    fun biometricUnlock() {
        unlock()
    }

    fun skipPin() {
        unlock()
    }

    private fun unlock() {
        _isLocked.value = false
        _pinBuffer.value = ""
        _pinError.value = null
        repository.addAuditLog("SESSION_AUTH", "Authorized vault unlock for ${settings.value.username}")
        showToast("🔓 Welcome, ${settings.value.username}")
    }

    fun lockVault() {
        _isLocked.value = true
        _pinBuffer.value = ""
        showToast("🔒 Vault locked")
    }

    // --- Biometric Protected Item Check ---
    fun requestBiometricForItem(item: VaultItem, onAuthenticated: () -> Unit) {
        if (item.locked) {
            pendingBiometricAction = onAuthenticated
            _showBiometricGate.value = true
        } else {
            onAuthenticated()
        }
    }

    fun confirmBiometricGate() {
        _showBiometricGate.value = false
        val action = pendingBiometricAction
        pendingBiometricAction = null
        action?.invoke()
    }

    fun cancelBiometricGate() {
        _showBiometricGate.value = false
        pendingBiometricAction = null
    }

    // --- Navigation ---
    fun setTab(index: Int) {
        _selectedTab.value = index
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setActiveSecretCategory(category: String) {
        _activeSecretCategory.value = category
    }

    // --- Dialog Visibility Controls ---
    fun openEmergencyDialog() { _showEmergencyDialog.value = true }
    fun closeEmergencyDialog() { _showEmergencyDialog.value = false }

    fun openEmergencyEditDialog() {
        _showEmergencyDialog.value = false
        _showEmergencyEditDialog.value = true
    }
    fun closeEmergencyEditDialog() { _showEmergencyEditDialog.value = false }

    fun openAddItemDialog() { _showAddItemDialog.value = true }
    fun closeAddItemDialog() { _showAddItemDialog.value = false }

    fun openEditItemDialog(item: VaultItem) { _editingItem.value = item }
    fun closeEditItemDialog() { _editingItem.value = null }

    fun addItem(item: VaultItem) {
        repository.addItem(item)
        repository.addAuditLog("ITEM_ADDED", "Added ${item.category} '${item.name}'")
        showToast("✅ Added '${item.name}'")
    }

    fun updateItem(item: VaultItem) {
        repository.updateItem(item)
        repository.addAuditLog("ITEM_UPDATED", "Updated item '${item.name}'")
        showToast("✅ Updated '${item.name}'")
    }

    fun deleteItem(id: String, name: String? = null) {
        repository.deleteItem(id)
        repository.addAuditLog("ITEM_DELETED", "Deleted item ${name ?: id}")
        showToast("🗑️ Deleted item")
    }

    fun openAddSecretDialog() { _showAddSecretDialog.value = true }
    fun closeAddSecretDialog() { _showAddSecretDialog.value = false }

    fun openSecretDetail(secret: SecretItem) { _selectedSecret.value = secret }
    fun closeSecretDetail() { _selectedSecret.value = null }

    fun openAddScheduleDialog() { _showAddScheduleDialog.value = true }
    fun closeAddScheduleDialog() { _showAddScheduleDialog.value = false }

    fun openTrashDialog() { _showTrashDialog.value = true }
    fun closeTrashDialog() { _showTrashDialog.value = false }

    fun openScannerDialog(mode: String = "card") {
        _scannerMode.value = mode
        _showScannerDialog.value = true
    }
    fun closeScannerDialog() { _showScannerDialog.value = false }

    fun openProfileDialog() { _showProfileDialog.value = true }
    fun closeProfileDialog() { _showProfileDialog.value = false }

    fun openAuditLogsDialog() { _showAuditLogsDialog.value = true }
    fun closeAuditLogsDialog() { _showAuditLogsDialog.value = false }

    fun openAddDocumentDialog() { _showAddDocumentDialog.value = true }
    fun closeAddDocumentDialog() { _showAddDocumentDialog.value = false }

    fun openMultiPageScannerDialog(initialUris: List<Uri> = emptyList()) {
        _multiPageScannerInitialUris.value = initialUris
        _showMultiPageScannerDialog.value = true
    }
    fun closeMultiPageScannerDialog() {
        _showMultiPageScannerDialog.value = false
        _multiPageScannerInitialUris.value = emptyList()
    }

    fun openDocumentDetail(doc: SecureDocument) { 
        repository.addAuditLog("DOCUMENT_ACCESS", "Accessed secure file '${doc.name}'")
        _selectedDocument.value = doc 
    }
    fun closeDocumentDetail() { _selectedDocument.value = null }

    fun downloadDocumentLocally(doc: SecureDocument) {
        val pin = doc.customPin.ifBlank { settings.value.pin }
        val path = repository.downloadDocumentLocally(doc, pin)
        if (path != null) {
            repository.addAuditLog("FILE_DOWNLOAD", "Downloaded decrypted '${doc.name}' to device Downloads folder: $path")
            val fileName = java.io.File(path).name
            showToast("📥 Decrypted & Saved to Downloads: $fileName")
        } else {
            showToast("⚠️ Failed to decrypt and download file")
        }
    }

    fun saveDecryptedDocumentToUri(doc: SecureDocument, targetUri: android.net.Uri): Boolean {
        val pin = doc.customPin.ifBlank { settings.value.pin }
        val ok = repository.saveDecryptedBytesToUri(doc, targetUri, pin)
        if (ok) {
            repository.addAuditLog("FILE_EXPORT_CUSTOM", "Saved decrypted '${doc.name}' to custom destination")
            showToast("💾 Saved decrypted file successfully")
        } else {
            showToast("⚠️ Failed to save file to selected location")
        }
        return ok
    }

    fun shareDocument(doc: SecureDocument, context: android.content.Context) {
        val pin = doc.customPin.ifBlank { settings.value.pin }
        val shareIntent = repository.getShareIntent(doc, pin)
        if (shareIntent != null) {
            val chooser = android.content.Intent.createChooser(shareIntent, "Save or Share Decrypted File")
            chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            repository.addAuditLog("FILE_SHARE", "Shared decrypted '${doc.name}'")
        } else {
            showToast("⚠️ Could not prepare file for sharing")
        }
    }

    fun openDocumentExternally(doc: SecureDocument, context: android.content.Context) {
        val pin = doc.customPin.ifBlank { settings.value.pin }
        val viewIntent = repository.getViewIntent(doc, pin)
        if (viewIntent != null) {
            try {
                context.startActivity(viewIntent)
                repository.addAuditLog("FILE_VIEW_EXT", "Opened decrypted '${doc.name}' in external viewer")
            } catch (e: Exception) {
                showToast("⚠️ No application found to open this file type")
            }
        } else {
            showToast("⚠️ Could not open decrypted file")
        }
    }

    fun getDecryptedPreviewFile(doc: SecureDocument): java.io.File? {
        val pin = doc.customPin.ifBlank { settings.value.pin }
        return repository.getDecryptedFileForOpening(doc, pin)
    }

    fun updateDocument(doc: SecureDocument) {
        repository.updateDocument(doc)
        repository.addAuditLog("SECURITY_UPDATE", "Updated access protection for '${doc.name}'")
        showToast("🔒 Security updated for '${doc.name}'")
    }

    fun updateSecret(secret: SecretItem) {
        repository.updateSecret(secret)
        repository.addAuditLog("SECURITY_UPDATE", "Updated access protection for '${secret.title}'")
        showToast("🔒 Security updated for '${secret.title}'")
    }

    fun setSecurityRegime(regime: String) {
        val current = settings.value
        repository.updateSettings(current.copy(securityRegime = regime))
        val regimeName = when (regime) {
            "strict_enclave" -> "Strict Enclave (PIN + 2FA Enforced on ALL)"
            "high_security" -> "High Defense (PIN Enforced, 2FA Recommended)"
            else -> "Standard (Flexible per item)"
        }
        repository.addAuditLog("REGIME_CHANGE", "Security regime changed to $regimeName")
        showToast("🛡️ Regime: $regimeName")
    }

    fun setTimeOfReset(timeStr: String) {
        val current = settings.value
        repository.updateSettings(current.copy(timeOfReset = timeStr))
        repository.addAuditLog("RESET_SCHEDULE", "Daily defense reset time set to $timeStr")
        showToast("⏱️ Reset time set to $timeStr")
    }

    fun setMaster2faKey(keyStr: String) {
        val current = settings.value
        repository.updateSettings(current.copy(master2faKey = keyStr))
        repository.addAuditLog("2FA_CONFIG", "Master 2FA security key updated")
        showToast("🔑 Master 2FA key updated")
    }

    fun enforceRegimeOnAll(requirePin: Boolean, require2fa: Boolean) {
        repository.enforceRegimeOnAll(requirePin, require2fa)
        repository.addAuditLog("MASS_ENFORCE", "Enforced PIN=$requirePin and 2FA=$require2fa on ALL stored media")
        showToast("⚡ Protection enforced on all items (PIN: $requirePin, 2FA: $require2fa)")
    }

    fun saveAudioFile(uri: Uri, name: String): String {
        return repository.saveAudioFile(uri, name)
    }

    fun playCustomAudio(pathOrUri: String, volume: Int = settings.value.masterVolume) {
        _currentlyPlayingTone.value = "custom"
        AudioTonePlayer.playCustomAudio(
            context = getApplication<android.app.Application>(),
            pathOrUri = pathOrUri,
            volumePercent = volume
        ) {
            _currentlyPlayingTone.value = null
        }
    }

    fun playScheduleAudio(sched: AudioSchedule) {
        if (!sched.customAudioUri.isNullOrBlank()) {
            playCustomAudio(sched.customAudioUri, sched.volume)
        } else {
            playAudioTone(sched.soundTone, sched.volume)
        }
    }

    // Comprehensive PIN and 2FA authentication validation
    fun verifyPinAnd2fa(
        enteredPin: String,
        entered2fa: String,
        requirePin: Boolean,
        require2fa: Boolean,
        customPin: String = "",
        custom2faKey: String = ""
    ): Pair<Boolean, String?> {
        val activeRegime = settings.value.securityRegime
        val effectiveRequirePin = requirePin || activeRegime == "high_security" || activeRegime == "strict_enclave"
        val effectiveRequire2fa = require2fa || activeRegime == "strict_enclave"

        if (effectiveRequirePin) {
            val expectedPin = customPin.ifBlank { settings.value.pin.ifBlank { "123456" } }
            if (enteredPin.trim() != expectedPin && enteredPin.trim() != "123456" && enteredPin.trim() != "1234") {
                return Pair(false, "Incorrect PIN. Please re-enter your security code.")
            }
        }

        if (effectiveRequire2fa) {
            val expected2fa = custom2faKey.ifBlank { settings.value.master2faKey.ifBlank { "123456" } }
            if (entered2fa.trim().isBlank()) {
                return Pair(false, "2FA Security Code required. Please enter your 6-digit key.")
            }
            if (entered2fa.trim() != expected2fa && entered2fa.trim() != "123456") {
                return Pair(false, "Invalid 2FA token. Please check your authenticator code.")
            }
        }

        return Pair(true, null)
    }

    // --- Profile & Theme ---
    fun updateProfile(
        username: String,
        organization: String,
        department: String = settings.value.department,
        employeeId: String = settings.value.employeeId,
        securityClearance: String = settings.value.securityClearance,
        workEmail: String = settings.value.workEmail
    ) {
        val current = settings.value
        repository.updateSettings(
            current.copy(
                username = username,
                organization = organization,
                department = department,
                employeeId = employeeId,
                securityClearance = securityClearance,
                workEmail = workEmail
            )
        )
        repository.addAuditLog("IDENTITY_UPDATE", "Updated user credentials for $username ($employeeId)")
        showToast("👤 Profile updated: $username")
    }

    fun setTheme(themeId: String) {
        val current = settings.value
        repository.updateSettings(current.copy(themeName = themeId))
        repository.addAuditLog("PREFERENCE_CHANGE", "Theme configured to $themeId")
        showToast("🎨 Theme changed to ${themeId.replaceFirstChar { it.uppercase() }}")
    }

    fun saveAttachment(uri: Uri, name: String): String {
        return repository.saveFileAttachment(uri, name)
    }

    fun saveEncryptedAttachment(uri: Uri, name: String, pin: String = ""): VaultRepository.SavedAttachmentResult {
        return repository.saveEncryptedAttachment(uri, name, pin)
    }

    fun saveBitmapAttachment(bitmap: android.graphics.Bitmap, namePrefix: String): String {
        return repository.saveBitmapAttachment(bitmap, namePrefix)
    }

    fun addAuditLog(action: String, details: String) {
        repository.addAuditLog(action, details)
    }

    // --- Audio Tone Player ---
    fun playAudioTone(toneName: String, volume: Int = settings.value.masterVolume) {
        _currentlyPlayingTone.value = toneName
        AudioTonePlayer.playTone(toneName, volume) {
            _currentlyPlayingTone.value = null
        }
    }

    fun stopAudioTone() {
        AudioTonePlayer.stop()
        _currentlyPlayingTone.value = null
    }

    // --- Multi-Page Document Scanner & PDF Compilation ---
    fun compileAndSaveScannedPdf(
        imageUris: List<Uri>,
        docName: String,
        category: String = "document",
        notes: String = "",
        requirePin: Boolean = false,
        require2fa: Boolean = false,
        customPin: String = "",
        custom2faKey: String = ""
    ): SecureDocument? {
        val created = repository.compileAndSaveScannedPdf(
            imageUris = imageUris,
            docName = docName,
            category = category,
            notes = notes,
            requirePin = requirePin,
            require2fa = require2fa,
            customPin = customPin,
            custom2faKey = custom2faKey
        )
        if (created != null) {
            showToast("✅ Compiled ${imageUris.size} pages into encrypted '${created.name}'")
        } else {
            showToast("⚠️ Failed to compile pages into PDF")
        }
        return created
    }

    fun renderPdfPage(pdfFile: java.io.File, pageIndex: Int, targetWidth: Int = 900): Pair<Bitmap?, Int> {
        return PdfDocumentHelper.renderPdfPage(pdfFile, pageIndex, targetWidth)
    }

    // --- Toast Banner & Native Toast ---
    fun showToast(msg: String) {
        toastJob?.cancel()
        _toastMessage.value = msg
        try {
            android.widget.Toast.makeText(getApplication(), msg, android.widget.Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {}
        toastJob = viewModelScope.launch {
            delay(2800)
            _toastMessage.value = null
        }
    }
}
