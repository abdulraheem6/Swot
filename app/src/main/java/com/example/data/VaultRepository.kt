package com.example.data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.example.audio.AlarmAlertActivity
import com.example.audio.AudioAlarmReceiver
import com.example.util.FileCrypto
import com.example.util.PdfDocumentHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class VaultRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("myvault_prefs_v7", Context.MODE_PRIVATE)

    private val _items = MutableStateFlow<List<VaultItem>>(emptyList())
    val items: StateFlow<List<VaultItem>> = _items.asStateFlow()

    private val _secrets = MutableStateFlow<List<SecretItem>>(emptyList())
    val secrets: StateFlow<List<SecretItem>> = _secrets.asStateFlow()

    private val _documents = MutableStateFlow<List<SecureDocument>>(emptyList())
    val documents: StateFlow<List<SecureDocument>> = _documents.asStateFlow()

    private val _emergency = MutableStateFlow(EmergencyInfo())
    val emergency: StateFlow<EmergencyInfo> = _emergency.asStateFlow()

    private val _schedules = MutableStateFlow<List<AudioSchedule>>(emptyList())
    val schedules: StateFlow<List<AudioSchedule>> = _schedules.asStateFlow()

    private val _trash = MutableStateFlow<List<TrashItem>>(emptyList())
    val trash: StateFlow<List<TrashItem>> = _trash.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLogEntry>>(emptyList())
    val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

    private val _contacts = MutableStateFlow<List<MemberContact>>(emptyList())
    val contacts: StateFlow<List<MemberContact>> = _contacts.asStateFlow()

    private val _transactions = MutableStateFlow<List<TransactionItem>>(emptyList())
    val transactions: StateFlow<List<TransactionItem>> = _transactions.asStateFlow()

    private val _monthlyBudgets = MutableStateFlow<Map<String, MonthlyBudget>>(emptyMap())
    val monthlyBudgets: StateFlow<Map<String, MonthlyBudget>> = _monthlyBudgets.asStateFlow()

    private val _monthlyPayments = MutableStateFlow<List<MonthlyPaymentItem>>(emptyList())
    val monthlyPayments: StateFlow<List<MonthlyPaymentItem>> = _monthlyPayments.asStateFlow()

    init {
        loadAll()
    }

    private fun loadAll() {
        val seeded = prefs.getBoolean("seeded_v7", false)
        if (!seeded) {
            seedInitialData()
        } else {
            loadFromDisk()
        }
    }

    private fun seedInitialData() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        fun addDays(days: Int): String {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, days)
            return dateFormat.format(c.time)
        }

        val initialItems = listOf(
            VaultItem(
                id = "item_1",
                name = "Milk",
                category = "product",
                date = addDays(1),
                notes = "2 litres fresh whole milk",
                recurring = true,
                recurInterval = 5,
                locked = false
            ),
            VaultItem(
                id = "item_2",
                name = "Passport",
                category = "document",
                date = addDays(120),
                notes = "Renewal required before international trip",
                recurring = false,
                recurInterval = 0,
                locked = true
            ),
            VaultItem(
                id = "item_3",
                name = "Netflix",
                category = "subscription",
                date = addDays(5),
                notes = "$15.99 monthly renewal",
                recurring = true,
                recurInterval = 30,
                locked = false
            ),
            VaultItem(
                id = "item_4",
                name = "Mom's Birthday",
                category = "event",
                date = addDays(14),
                notes = "Gift idea: spa voucher & flowers",
                recurring = true,
                recurInterval = 365,
                locked = false
            ),
            VaultItem(
                id = "item_5",
                name = "Car Insurance",
                category = "document",
                date = addDays(45),
                notes = "Policy #POL-89423 renewal due",
                recurring = true,
                recurInterval = 365,
                locked = false
            ),
            VaultItem(
                id = "item_6",
                name = "Vitamin D",
                category = "product",
                date = addDays(0),
                notes = "1 tablet daily after breakfast",
                recurring = true,
                recurInterval = 30,
                locked = false
            )
        )

        val initialSecrets = listOf(
            SecretItem(
                id = "sec_1",
                category = "wifi",
                title = "Home WiFi 5G",
                subtitle = "Fiber_SkyNet_5G",
                favorite = true,
                fields = mapOf(
                    "ssid" to "Fiber_SkyNet_5G",
                    "password" to "NeoVault@2026!",
                    "security" to "WPA3 Personal"
                ),
                notes = "Router located in living room cabinet"
            ),
            SecretItem(
                id = "sec_2",
                category = "login",
                title = "Google Account",
                subtitle = "alex.rivera@gmail.com",
                favorite = true,
                fields = mapOf(
                    "username" to "alex.rivera@gmail.com",
                    "password" to "Tr0ub4dor&3_99x",
                    "url" to "https://accounts.google.com",
                    "totpSecret" to "JBSWY3DPEHPK3PXP"
                ),
                notes = "Primary 2FA key stored on YubiKey backup"
            ),
            SecretItem(
                id = "sec_3",
                category = "bank",
                title = "Premier Savings Account",
                subtitle = "Apex International Bank",
                favorite = false,
                fields = mapOf(
                    "bankName" to "Apex International Bank",
                    "accountNumber" to "984729103847",
                    "ifsc" to "APEX0004123",
                    "branch" to "Downtown Metro",
                    "netbankingUser" to "alex_apex_99"
                ),
                notes = "Dedicated high-yield emergency reserve fund"
            ),
            SecretItem(
                id = "sec_4",
                category = "card",
                title = "Sapphire Metal Credit Card",
                subtitle = "•••• 4829",
                favorite = true,
                fields = mapOf(
                    "cardNumber" to "4532 8920 1934 4829",
                    "cardHolder" to "ALEX RIVERA",
                    "expiry" to "09/29",
                    "cvv" to "742",
                    "pin" to "3920"
                ),
                notes = "Travel rewards: 3x points on flights and hotels"
            ),
            SecretItem(
                id = "sec_5",
                category = "note",
                title = "Safe Locker Master Combination",
                subtitle = "Master dial code",
                favorite = false,
                fields = mapOf(
                    "content" to "Left 3x to 42, Right 2x to 18, Left 1x to 99, Pull handle down firmly."
                ),
                notes = "Office fireproof safe in study room"
            )
        )

        val initialDocs = listOf(
            SecureDocument(
                id = "doc_1",
                name = "Executive ID Badge & Photo",
                category = "photo",
                uri = "sample_photo_id",
                fileSize = 1024 * 350,
                notes = "High-resolution secure photo for biometric verification",
                locked = true,
                isVideo = false,
                requirePin = true,
                require2fa = false
            ),
            SecureDocument(
                id = "doc_2",
                name = "Enclave Security Inspection Video",
                category = "video",
                uri = "sample_video_cam",
                fileSize = 1024 * 1024 * 4,
                notes = "Encrypted perimeter and server room surveillance video",
                locked = true,
                isVideo = true,
                requirePin = true,
                require2fa = true
            ),
            SecureDocument(
                id = "doc_3",
                name = "Corporate NDA & Contract",
                category = "contract",
                uri = "sample_contract_doc",
                fileSize = 1024 * 850,
                notes = "Signed employment agreement and IP waiver",
                locked = true,
                isVideo = false,
                requirePin = true,
                require2fa = true
            ),
            SecureDocument(
                id = "doc_4",
                name = "Passport & Visa Copy",
                category = "id",
                uri = "sample_passport_id",
                fileSize = 1024 * 420,
                notes = "Biometric passport verified with department",
                locked = true,
                isVideo = false,
                requirePin = true,
                require2fa = false
            )
        )

        val initialEmergency = EmergencyInfo(
            blood = "O+",
            allergies = "Penicillin, Tree Nuts",
            conditions = "Mild Asthma",
            meds = "Albuterol Inhaler (as needed)",
            contactName = "Sarah Rivera (Spouse)",
            contactPhone = "+1 (555) 234-5678",
            doctor = "Dr. Marcus Chen · City General Hospital",
            organ = "Yes",
            notes = "Carries EpiPen in backpack when traveling."
        )

        val initialSchedules = listOf(
            AudioSchedule(
                id = "sched_1",
                name = "Morning Vitamin Reminder",
                soundTone = "chime",
                dateTime = "${addDays(0)}T09:00",
                repeat = "daily",
                volume = 80,
                enabled = true
            ),
            AudioSchedule(
                id = "sched_2",
                name = "Evening Vault Check",
                soundTone = "gentle",
                dateTime = "${addDays(1)}T20:30",
                repeat = "weekly",
                volume = 70,
                enabled = true
            )
        )

        val initialContacts = listOf(
            MemberContact(
                id = "contact_1",
                name = "David Miller",
                category = "family",
                phone = "+1 (555) 234-5678",
                email = "david.miller@email.com",
                specialty = "Brother · Next of Kin",
                notes = "Emergency contact & spare house keys",
                avatarEmoji = "👨‍👩‍👧"
            ),
            MemberContact(
                id = "contact_2",
                name = "Apex Spark Electric",
                category = "electrician",
                phone = "+1 (555) 876-1234",
                email = "service@apexspark.com",
                specialty = "Licensed Master Electrician",
                notes = "24/7 breaker & wiring emergency",
                avatarEmoji = "⚡"
            ),
            MemberContact(
                id = "contact_3",
                name = "RapidFlow Plumbing",
                category = "plumber",
                phone = "+1 (555) 345-6789",
                email = "service@rapidflow.com",
                specialty = "Residential & Pipe Specialist",
                notes = "Repairs water leaks, boiler, drainage",
                avatarEmoji = "🔧"
            ),
            MemberContact(
                id = "contact_4",
                name = "Marco's Classic Barber",
                category = "barber",
                phone = "+1 (555) 456-7890",
                email = "marco@classiccuts.com",
                specialty = "Master Barber / Stylist",
                notes = "Appointments on Tuesdays & Saturdays",
                avatarEmoji = "💈"
            )
        )

        _items.value = initialItems
        _secrets.value = initialSecrets
        _documents.value = initialDocs
        _emergency.value = initialEmergency
        _schedules.value = initialSchedules
        _contacts.value = initialContacts
        _trash.value = emptyList()
        _settings.value = AppSettings()

        val (initialTx, initialBudgets, initialPayments) = seedMoneyData()
        _transactions.value = initialTx
        _monthlyBudgets.value = initialBudgets
        _monthlyPayments.value = initialPayments

        saveAll()
        prefs.edit().putBoolean("seeded_v7", true).apply()
    }

    private fun seedMoneyData(): Triple<List<TransactionItem>, Map<String, MonthlyBudget>, List<MonthlyPaymentItem>> {
        val monthFormat = SimpleDateFormat("yyyy-MM", Locale.US)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        val currentMonth = monthFormat.format(cal.time)

        fun dateWithOffset(daysAgo: Int): String {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -daysAgo)
            return dateFormat.format(c.time)
        }

        val initialTransactions = listOf(
            TransactionItem(
                id = "tx_1",
                type = "income",
                title = "Primary Tech Salary",
                amount = 4250.00,
                category = "Salary",
                date = dateWithOffset(10),
                paymentMethod = "Bank Transfer",
                notes = "Direct deposit monthly payroll",
                isTaxDeductible = false
            ),
            TransactionItem(
                id = "tx_2",
                type = "income",
                title = "Freelance UI Consulting",
                amount = 850.00,
                category = "Freelance",
                date = dateWithOffset(4),
                paymentMethod = "UPI/Online",
                notes = "Design system delivery milestone",
                isTaxDeductible = false
            ),
            TransactionItem(
                id = "tx_3",
                type = "expense",
                title = "Whole Foods Organic Market",
                amount = 138.40,
                category = "Food & Groceries",
                date = dateWithOffset(1),
                paymentMethod = "Card",
                notes = "Produce, groceries, and pantry supplies",
                receiptName = "whole_foods_oct_receipt.pdf",
                isTaxDeductible = false
            ),
            TransactionItem(
                id = "tx_4",
                type = "expense",
                title = "Pacific Gas & Electric",
                amount = 89.20,
                category = "Utilities",
                date = dateWithOffset(5),
                paymentMethod = "UPI/Online",
                notes = "Home electricity & heating billing",
                receiptName = "pge_oct_bill.pdf",
                isTaxDeductible = false
            ),
            TransactionItem(
                id = "tx_5",
                type = "expense",
                title = "Shell Gas & Fuel",
                amount = 52.00,
                category = "Transportation",
                date = dateWithOffset(3),
                paymentMethod = "Card",
                notes = "Gas tank refill",
                isTaxDeductible = false
            ),
            TransactionItem(
                id = "tx_6",
                type = "expense",
                title = "CVS Pharmacy Prescription",
                amount = 34.50,
                category = "Health",
                date = dateWithOffset(7),
                paymentMethod = "Card",
                notes = "Health checkup and vitamins (Eligible HSA expense)",
                isTaxDeductible = true
            ),
            TransactionItem(
                id = "tx_7",
                type = "expense",
                title = "Blue Bottle Coffee",
                amount = 14.80,
                category = "Food & Groceries",
                date = dateWithOffset(2),
                paymentMethod = "Card",
                notes = "Work discussion coffee & snack",
                isTaxDeductible = false
            ),
            TransactionItem(
                id = "tx_8",
                type = "expense",
                title = "Gigabit Fiber Internet",
                amount = 70.00,
                category = "Utilities",
                date = dateWithOffset(8),
                paymentMethod = "Bank Transfer",
                notes = "Monthly fiber connection for remote work",
                isTaxDeductible = true
            )
        )

        val initialBudget = MonthlyBudget(
            id = "budget_$currentMonth",
            month = currentMonth,
            totalBudget = 2900.0,
            categoryBudgets = mapOf(
                "Food & Groceries" to 600.0,
                "Housing & Rent" to 1400.0,
                "Utilities" to 250.0,
                "Transportation" to 180.0,
                "Shopping" to 220.0,
                "Entertainment" to 150.0,
                "Health" to 100.0
            )
        )

        val initialPayments = listOf(
            MonthlyPaymentItem(
                id = "bill_1",
                title = "Apartment Rent",
                amount = 1350.00,
                dueDay = 1,
                category = "Rent",
                autoPay = true,
                notes = "Direct ACH transfer to landlord",
                alarmEnabled = true,
                alarmTime = "09:00",
                paidMonths = listOf(currentMonth)
            ),
            MonthlyPaymentItem(
                id = "bill_2",
                title = "Netflix 4K UHD",
                amount = 22.99,
                dueDay = 5,
                category = "Subscription",
                autoPay = true,
                notes = "Streaming subscription",
                alarmEnabled = false,
                alarmTime = "10:00",
                paidMonths = listOf(currentMonth)
            ),
            MonthlyPaymentItem(
                id = "bill_3",
                title = "Car Loan EMI",
                amount = 320.00,
                dueDay = 15,
                category = "EMI / Loan",
                autoPay = false,
                notes = "Vehicle financing payment with Chase",
                alarmEnabled = true,
                alarmTime = "09:00",
                paidMonths = emptyList()
            ),
            MonthlyPaymentItem(
                id = "bill_4",
                title = "Health & Dental Premium",
                amount = 165.00,
                dueDay = 20,
                category = "Insurance",
                autoPay = true,
                notes = "Comprehensive health & vision coverage",
                alarmEnabled = true,
                alarmTime = "09:30",
                paidMonths = emptyList()
            ),
            MonthlyPaymentItem(
                id = "bill_5",
                title = "Equinox Gym Fitness",
                amount = 65.00,
                dueDay = 25,
                category = "Subscription",
                autoPay = false,
                notes = "Monthly gym club access",
                alarmEnabled = true,
                alarmTime = "08:00",
                paidMonths = emptyList()
            )
        )

        return Triple(initialTransactions, mapOf(currentMonth to initialBudget), initialPayments)
    }

    private fun loadFromDisk() {
        // Items
        val itemsStr = prefs.getString("items_json", "[]") ?: "[]"
        val itemsArray = JSONArray(itemsStr)
        val loadedItems = mutableListOf<VaultItem>()
        for (i in 0 until itemsArray.length()) {
            loadedItems.add(VaultItem.fromJson(itemsArray.getJSONObject(i)))
        }
        _items.value = loadedItems

        // Secrets
        val secretsStr = prefs.getString("secrets_json", "[]") ?: "[]"
        val secretsArray = JSONArray(secretsStr)
        val loadedSecrets = mutableListOf<SecretItem>()
        for (i in 0 until secretsArray.length()) {
            loadedSecrets.add(SecretItem.fromJson(secretsArray.getJSONObject(i)))
        }
        _secrets.value = loadedSecrets

        // Documents
        val docsStr = prefs.getString("documents_json", "[]") ?: "[]"
        val docsArray = JSONArray(docsStr)
        val loadedDocs = mutableListOf<SecureDocument>()
        for (i in 0 until docsArray.length()) {
            loadedDocs.add(SecureDocument.fromJson(docsArray.getJSONObject(i)))
        }
        _documents.value = loadedDocs

        // Emergency
        val emgStr = prefs.getString("emergency_json", null)
        _emergency.value = if (emgStr != null) EmergencyInfo.fromJson(JSONObject(emgStr)) else EmergencyInfo()

        // Schedules
        val schedStr = prefs.getString("schedules_json", "[]") ?: "[]"
        val schedArray = JSONArray(schedStr)
        val loadedSchedules = mutableListOf<AudioSchedule>()
        for (i in 0 until schedArray.length()) {
            loadedSchedules.add(AudioSchedule.fromJson(schedArray.getJSONObject(i)))
        }
        _schedules.value = loadedSchedules

        // Trash (30 days purge)
        val cutoff = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        val trashStr = prefs.getString("trash_json", "[]") ?: "[]"
        val trashArray = JSONArray(trashStr)
        val loadedTrash = mutableListOf<TrashItem>()
        for (i in 0 until trashArray.length()) {
            val t = TrashItem.fromJson(trashArray.getJSONObject(i))
            if (t.deletedAt > cutoff) {
                loadedTrash.add(t)
            }
        }
        _trash.value = loadedTrash

        // Settings
        val settingsStr = prefs.getString("settings_json", null)
        _settings.value = if (settingsStr != null) AppSettings.fromJson(JSONObject(settingsStr)) else AppSettings()

        // Contacts
        val contactsStr = prefs.getString("contacts_json", null)
        if (contactsStr != null) {
            val contactsArray = JSONArray(contactsStr)
            val loadedContacts = mutableListOf<MemberContact>()
            for (i in 0 until contactsArray.length()) {
                loadedContacts.add(MemberContact.fromJson(contactsArray.getJSONObject(i)))
            }
            _contacts.value = loadedContacts
        } else {
            _contacts.value = listOf(
                MemberContact("c1", "David Miller", "family", "+1 (555) 234-5678", "david@family.com", "", "Spare house keys", "Brother", "👨‍👩‍👧"),
                MemberContact("c2", "Apex Spark Electric", "electrician", "+1 (555) 876-1234", "service@apex.com", "", "24/7 breaker emergency", "Licensed Electrician", "⚡"),
                MemberContact("c3", "RapidFlow Plumbing", "plumber", "+1 (555) 345-6789", "service@rapidflow.com", "", "Leak & boiler specialist", "Master Plumber", "🔧"),
                MemberContact("c4", "Marco's Classic Barber", "barber", "+1 (555) 456-7890", "marco@barber.com", "", "Tuesdays & Saturdays", "Barber / Hair Stylist", "💈")
            )
        }

        // Audit Logs
        val auditStr = prefs.getString("audit_logs_json", "[]") ?: "[]"
        val auditArray = JSONArray(auditStr)
        val loadedAudit = mutableListOf<AuditLogEntry>()
        for (i in 0 until auditArray.length()) {
            loadedAudit.add(AuditLogEntry.fromJson(auditArray.getJSONObject(i)))
        }
        _auditLogs.value = loadedAudit.take(30)

        // Transactions
        val txStr = prefs.getString("transactions_json", null)
        if (txStr != null) {
            val txArr = JSONArray(txStr)
            val loadedTx = mutableListOf<TransactionItem>()
            for (i in 0 until txArr.length()) {
                loadedTx.add(TransactionItem.fromJson(txArr.getJSONObject(i)))
            }
            _transactions.value = loadedTx
        } else {
            val (initialTx, _, _) = seedMoneyData()
            _transactions.value = initialTx
        }

        // Monthly Budgets
        val budgetsStr = prefs.getString("budgets_json", null)
        if (budgetsStr != null) {
            val budgetsArr = JSONArray(budgetsStr)
            val loadedBudgets = mutableMapOf<String, MonthlyBudget>()
            for (i in 0 until budgetsArr.length()) {
                val b = MonthlyBudget.fromJson(budgetsArr.getJSONObject(i))
                loadedBudgets[b.month] = b
            }
            _monthlyBudgets.value = loadedBudgets
        } else {
            val (_, initialBudgets, _) = seedMoneyData()
            _monthlyBudgets.value = initialBudgets
        }

        // Monthly Payments
        val paymentsStr = prefs.getString("monthly_payments_json", null)
        if (paymentsStr != null) {
            val paymentsArr = JSONArray(paymentsStr)
            val loadedPayments = mutableListOf<MonthlyPaymentItem>()
            for (i in 0 until paymentsArr.length()) {
                loadedPayments.add(MonthlyPaymentItem.fromJson(paymentsArr.getJSONObject(i)))
            }
            _monthlyPayments.value = loadedPayments
        } else {
            val (_, _, initialPayments) = seedMoneyData()
            _monthlyPayments.value = initialPayments
        }

        // Reschedule active item alarms and schedule alarms on startup
        _items.value.forEach { item ->
            if (item.audioId != null) {
                scheduleItemAlarm(item)
            }
        }
        _schedules.value.forEach { sched ->
            if (sched.enabled) {
                scheduleAlarm(context, sched)
            }
        }
        _secrets.value.forEach { secret ->
            if (secret.expirationAlertEnabled && !secret.expirationDate.isNullOrBlank()) {
                scheduleSecretAlarm(secret)
            }
        }
    }

    private fun saveAll() {
        val editor = prefs.edit()

        // Items
        val itemsArr = JSONArray()
        _items.value.forEach { itemsArr.put(it.toJson()) }
        editor.putString("items_json", itemsArr.toString())

        // Secrets
        val secretsArr = JSONArray()
        _secrets.value.forEach { secretsArr.put(it.toJson()) }
        editor.putString("secrets_json", secretsArr.toString())

        // Documents
        val docsArr = JSONArray()
        _documents.value.forEach { docsArr.put(it.toJson()) }
        editor.putString("documents_json", docsArr.toString())

        // Emergency
        editor.putString("emergency_json", _emergency.value.toJson().toString())

        // Schedules
        val schedArr = JSONArray()
        _schedules.value.forEach { schedArr.put(it.toJson()) }
        editor.putString("schedules_json", schedArr.toString())

        // Trash
        val trashArr = JSONArray()
        _trash.value.forEach { trashArr.put(it.toJson()) }
        editor.putString("trash_json", trashArr.toString())

        // Settings
        editor.putString("settings_json", _settings.value.toJson().toString())

        // Contacts
        val contactsArr = JSONArray()
        _contacts.value.forEach { contactsArr.put(it.toJson()) }
        editor.putString("contacts_json", contactsArr.toString())

        // Transactions
        val txArr = JSONArray()
        _transactions.value.forEach { txArr.put(it.toJson()) }
        editor.putString("transactions_json", txArr.toString())

        // Budgets
        val budgetsArr = JSONArray()
        _monthlyBudgets.value.values.forEach { budgetsArr.put(it.toJson()) }
        editor.putString("budgets_json", budgetsArr.toString())

        // Monthly Payments
        val paymentsArr = JSONArray()
        _monthlyPayments.value.forEach { paymentsArr.put(it.toJson()) }
        editor.putString("monthly_payments_json", paymentsArr.toString())

        // Audit Logs (keep latest 100)
        val auditArr = JSONArray()
        _auditLogs.value.take(100).forEach { auditArr.put(it.toJson()) }
        editor.putString("audit_logs_json", auditArr.toString())

        editor.apply()
    }

    // --- Saved Attachment Result ---
    data class SavedAttachmentResult(
        val path: String,
        val size: Long,
        val originalName: String,
        val mimeType: String,
        val isEncrypted: Boolean
    )

    // --- Save File Attachment with AES-256-GCM Encryption ---
    fun saveEncryptedAttachment(
        sourceUri: Uri,
        fallbackName: String,
        passphrase: String = ""
    ): SavedAttachmentResult {
        var displayName = fallbackName
        var fileSize = 0L
        var mimeType = context.contentResolver.getType(sourceUri) ?: ""

        try {
            context.contentResolver.query(sourceUri, null, null, null, null)?.use { cursor ->
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIdx != -1) {
                        val n = cursor.getString(nameIdx)
                        if (!n.isNullOrBlank()) displayName = n
                    }
                    if (sizeIdx != -1) {
                        fileSize = cursor.getLong(sizeIdx)
                    }
                }
            }
        } catch (_: Exception) {}

        if (mimeType.isBlank()) {
            val ext = displayName.substringAfterLast(".", "").lowercase()
            if (ext.isNotEmpty()) {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
            } else {
                mimeType = "application/octet-stream"
            }
        }

        val storageDir = File(context.filesDir, "vault_files").apply { mkdirs() }
        val ext = displayName.substringAfterLast(".", "dat")
        val cleanPrefix = displayName.substringBeforeLast(".").replace("[^a-zA-Z0-9_-]".toRegex(), "_").take(24)
        val targetFile = File(storageDir, "enc_${cleanPrefix}_${UUID.randomUUID()}.$ext.vault")

        val encryptedOk = try {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileCrypto.encryptStreamToFile(input, targetFile, passphrase)
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

        if (encryptedOk && targetFile.exists()) {
            return SavedAttachmentResult(
                path = targetFile.absolutePath,
                size = if (fileSize > 0) fileSize else targetFile.length(),
                originalName = displayName,
                mimeType = mimeType,
                isEncrypted = true
            )
        }

        // Direct copy fallback if encryption cipher failed
        try {
            val fallbackFile = File(storageDir, "file_${UUID.randomUUID()}.$ext")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(fallbackFile).use { out -> input.copyTo(out) }
            }
            return SavedAttachmentResult(
                path = fallbackFile.absolutePath,
                size = fallbackFile.length(),
                originalName = displayName,
                mimeType = mimeType,
                isEncrypted = false
            )
        } catch (_: Exception) {
            return SavedAttachmentResult(
                path = sourceUri.toString(),
                size = 0L,
                originalName = displayName,
                mimeType = mimeType,
                isEncrypted = false
            )
        }
    }

    fun saveFileAttachment(sourceUri: Uri, originalName: String): String {
        return saveEncryptedAttachment(sourceUri, originalName).path
    }

    fun saveBitmapAttachment(bitmap: android.graphics.Bitmap, namePrefix: String): String {
        return try {
            val storageDir = File(context.filesDir, "vault_files").apply { mkdirs() }
            val targetFile = File(storageDir, "${namePrefix}_${UUID.randomUUID()}.jpg")
            FileOutputStream(targetFile).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 92, out)
            }
            targetFile.absolutePath
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Compiles multiple scanned page images into a single encrypted PDF in Documents & Files.
     */
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
        if (imageUris.isEmpty()) return null

        val cleanTitle = if (docName.isBlank()) {
            "Scanned_Doc_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(java.util.Date())}"
        } else {
            docName.trim()
        }
        val pdfFileName = if (cleanTitle.endsWith(".pdf", true)) cleanTitle else "$cleanTitle.pdf"

        val scannerDir = File(context.cacheDir, "scanner").apply { mkdirs() }
        val tempPdfFile = File(scannerDir, "scan_${UUID.randomUUID()}.pdf")

        val compiledOk = PdfDocumentHelper.compileImagesToPdf(context, imageUris, tempPdfFile, cleanTitle)
        if (!compiledOk || !tempPdfFile.exists()) {
            return null
        }

        val effectivePin = if (requirePin && customPin.isNotBlank()) customPin.trim() else _settings.value.pin
        val storageDir = File(context.filesDir, "vault_files").apply { mkdirs() }
        val targetVaultFile = File(storageDir, "enc_scan_${UUID.randomUUID()}.pdf.vault")

        val encryptedOk = try {
            FileInputStream(tempPdfFile).use { input ->
                FileCrypto.encryptStreamToFile(input, targetVaultFile, effectivePin)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            try { tempPdfFile.delete() } catch (_: Exception) {}
        }

        if (!encryptedOk || !targetVaultFile.exists()) {
            return null
        }

        val newDoc = SecureDocument(
            id = UUID.randomUUID().toString(),
            name = pdfFileName,
            category = category,
            uri = targetVaultFile.absolutePath,
            fileSize = targetVaultFile.length(),
            notes = notes.ifBlank { "Multi-Page Document Scanner: ${imageUris.size} pages compiled into encrypted PDF." },
            locked = requirePin || require2fa,
            isVideo = false,
            requirePin = requirePin,
            require2fa = require2fa,
            customPin = customPin.trim(),
            custom2faKey = custom2faKey.trim(),
            originalFileName = pdfFileName,
            mimeType = "application/pdf",
            isEncrypted = true
        )

        addDocument(newDoc)
        addAuditLog("SCAN_PDF_COMPILED", "Compiled ${imageUris.size} pages into AES-256 PDF '$pdfFileName'")
        return newDoc
    }

    fun addAuditLog(action: String, details: String) {
        val entry = AuditLogEntry(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            action = action,
            details = details,
            actor = _settings.value.username.ifBlank { "Vault User" }
        )
        _auditLogs.value = (listOf(entry) + _auditLogs.value).take(30)
        saveAll()
    }

    /**
     * Decrypts the secure document and downloads it locally to the public mobile Downloads folder.
     */
    fun downloadDocumentLocally(doc: SecureDocument, pin: String = ""): String? {
        return try {
            val sourceFile = File(doc.uri)
            val decryptedBytes = if (sourceFile.exists()) {
                FileCrypto.decryptFileToBytes(sourceFile, pin)
            } else {
                val sb = StringBuilder()
                sb.append("=== MYVAULT ZERO-KNOWLEDGE SECURE EXPORT ===\n\n")
                sb.append("Document: ").append(doc.name).append("\n")
                sb.append("Category: ").append(doc.category.uppercase(Locale.US)).append("\n")
                sb.append("File Size: ").append(doc.fileSize).append(" bytes\n")
                sb.append("Security: AES-256-GCM Verified (PBKDF2)\n")
                sb.append("Export Timestamp: ").append(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(java.util.Date())).append("\n")
                if (doc.notes.isNotBlank()) sb.append("Notes: ").append(doc.notes).append("\n")
                sb.toString().toByteArray(Charsets.UTF_8)
            } ?: return null

            val ext = when {
                doc.originalFileName.contains(".") -> "." + doc.originalFileName.substringAfterLast(".")
                doc.name.contains(".") -> "." + doc.name.substringAfterLast(".")
                doc.category == "photo" -> ".jpg"
                doc.category == "video" -> ".mp4"
                doc.category == "receipt" -> ".pdf"
                else -> ".pdf"
            }
            val baseName = doc.name.substringBeforeLast(".").replace("[^a-zA-Z0-9_-]".toRegex(), "_").ifBlank { "SecureDocument" }
            val fileName = "MyVault_${baseName}_${System.currentTimeMillis() % 100000}$ext"
            val mimeType = doc.mimeType.ifBlank {
                val cleanExt = ext.removePrefix(".").lowercase(Locale.US)
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(cleanExt) ?: "application/octet-stream"
            }

            var savedPath: String? = null

            // 1. Android 10+ (API 29+) MediaStore.Downloads API - Places directly in user's public Downloads/MyVault
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                        put(MediaStore.Downloads.MIME_TYPE, mimeType)
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/MyVault")
                        put(MediaStore.Downloads.IS_PENDING, 1)
                    }
                    val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    if (uri != null) {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            out.write(decryptedBytes)
                        }
                        values.clear()
                        values.put(MediaStore.Downloads.IS_PENDING, 0)
                        context.contentResolver.update(uri, values, null, null)
                        savedPath = "/storage/emulated/0/Download/MyVault/$fileName"
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 2. Direct public Downloads folder fallback
            if (savedPath == null) {
                try {
                    val publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val vaultFolder = File(publicDownloads, "MyVault").apply { if (!exists()) mkdirs() }
                    val destFile = File(vaultFolder, fileName)
                    destFile.writeBytes(decryptedBytes)
                    MediaScannerConnection.scanFile(context, arrayOf(destFile.absolutePath), arrayOf(mimeType), null)
                    savedPath = destFile.absolutePath
                } catch (_: Exception) {}
            }

            // 3. App-specific external storage fallback
            if (savedPath == null) {
                val extDir = context.getExternalFilesDirs(Environment.DIRECTORY_DOWNLOADS).firstOrNull() ?: context.filesDir
                val destFile = File(extDir, fileName)
                destFile.writeBytes(decryptedBytes)
                savedPath = destFile.absolutePath
            }

            savedPath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Writes decrypted document content to a user-selected SAF Uri (e.g. chosen folder).
     */
    fun saveDecryptedBytesToUri(doc: SecureDocument, targetUri: Uri, pin: String = ""): Boolean {
        return try {
            val sourceFile = File(doc.uri)
            val decryptedBytes = if (sourceFile.exists()) {
                FileCrypto.decryptFileToBytes(sourceFile, pin)
            } else {
                val sb = StringBuilder()
                sb.append("=== MYVAULT SECURE EXPORT ===\n\n")
                sb.append("Document: ").append(doc.name).append("\n")
                sb.append("Category: ").append(doc.category.uppercase(Locale.US)).append("\n")
                if (doc.notes.isNotBlank()) sb.append("Notes: ").append(doc.notes).append("\n")
                sb.toString().toByteArray(Charsets.UTF_8)
            } ?: return false

            context.contentResolver.openOutputStream(targetUri)?.use { out ->
                out.write(decryptedBytes)
                out.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Decrypts temporary file for in-app viewing or third-party app opening.
     */
    fun getDecryptedFileForOpening(doc: SecureDocument, pin: String = ""): File? {
        val sourceFile = File(doc.uri)
        val ext = when {
            doc.originalFileName.contains(".") -> "." + doc.originalFileName.substringAfterLast(".")
            doc.name.contains(".") -> "." + doc.name.substringAfterLast(".")
            doc.category == "photo" -> ".jpg"
            doc.category == "video" -> ".mp4"
            else -> ".pdf"
        }
        val cleanName = doc.name.replace("[^a-zA-Z0-9_-]".toRegex(), "_") + ext
        return if (sourceFile.exists()) {
            FileCrypto.getDecryptedCacheFile(context, sourceFile, cleanName, pin)
        } else {
            val cacheFolder = File(context.cacheDir, "decrypted").apply { if (!exists()) mkdirs() }
            val tempFile = File(cacheFolder, "dec_$cleanName")
            tempFile.writeText("=== MYVAULT SECURE EXPORT ===\n\nName: ${doc.name}\nCategory: ${doc.category}\nDate: ${java.util.Date()}\nNotes: ${doc.notes}\n")
            tempFile
        }
    }

    /**
     * Creates an Android Share Sheet Intent for decrypted document export.
     */
    fun getShareIntent(doc: SecureDocument, pin: String = ""): Intent? {
        val tempFile = getDecryptedFileForOpening(doc, pin) ?: return null
        return try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
            val mimeType = doc.mimeType.ifBlank {
                val ext = tempFile.name.substringAfterLast(".", "").lowercase(Locale.US)
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
            }
            Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Decrypted: ${doc.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Creates an ACTION_VIEW Intent to open the decrypted document in an external reader.
     */
    fun getViewIntent(doc: SecureDocument, pin: String = ""): Intent? {
        val tempFile = getDecryptedFileForOpening(doc, pin) ?: return null
        return try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
            val mimeType = doc.mimeType.ifBlank {
                val ext = tempFile.name.substringAfterLast(".", "").lowercase(Locale.US)
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "*/*"
            }
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // --- Secure Document CRUD ---
    fun addDocument(doc: SecureDocument) {
        _documents.value = listOf(doc) + _documents.value
        saveAll()
    }

    fun updateDocument(doc: SecureDocument) {
        _documents.value = _documents.value.map { if (it.id == doc.id) doc else it }
        saveAll()
    }

    // --- Member & Service Contacts CRUD ---
    fun addContact(contact: MemberContact) {
        _contacts.value = listOf(contact) + _contacts.value
        saveAll()
    }

    fun updateContact(contact: MemberContact) {
        _contacts.value = _contacts.value.map { if (it.id == contact.id) contact else it }
        saveAll()
    }

    fun deleteContact(id: String) {
        _contacts.value = _contacts.value.filter { it.id != id }
        saveAll()
    }

    // --- Money & Finance CRUD ---
    fun addTransaction(item: TransactionItem) {
        _transactions.value = listOf(item) + _transactions.value
        addAuditLog("TRANSACTION_ADDED", "Added ${item.type}: ${item.title} ($${String.format(Locale.US, "%.2f", item.amount)})")
        saveAll()
    }

    fun updateTransaction(item: TransactionItem) {
        _transactions.value = _transactions.value.map { if (it.id == item.id) item else it }
        addAuditLog("TRANSACTION_UPDATED", "Updated transaction: ${item.title}")
        saveAll()
    }

    fun deleteTransaction(id: String) {
        val target = _transactions.value.find { it.id == id }
        if (target != null) {
            _transactions.value = _transactions.value.filter { it.id != id }
            addAuditLog("TRANSACTION_DELETED", "Deleted transaction: ${target.title}")
            saveAll()
        }
    }

    fun setMonthlyBudget(budget: MonthlyBudget) {
        val current = _monthlyBudgets.value.toMutableMap()
        current[budget.month] = budget
        _monthlyBudgets.value = current
        addAuditLog("BUDGET_UPDATED", "Set budget for ${budget.month}: $${String.format(Locale.US, "%.0f", budget.totalBudget)}")
        saveAll()
    }

    fun addMonthlyPayment(item: MonthlyPaymentItem) {
        _monthlyPayments.value = listOf(item) + _monthlyPayments.value
        addAuditLog("BILL_ADDED", "Added recurring bill: ${item.title} ($${String.format(Locale.US, "%.2f", item.amount)}/mo)")
        saveAll()
    }

    fun updateMonthlyPayment(item: MonthlyPaymentItem) {
        _monthlyPayments.value = _monthlyPayments.value.map { if (it.id == item.id) item else it }
        addAuditLog("BILL_UPDATED", "Updated recurring bill: ${item.title}")
        saveAll()
    }

    fun deleteMonthlyPayment(id: String) {
        val target = _monthlyPayments.value.find { it.id == id }
        if (target != null) {
            _monthlyPayments.value = _monthlyPayments.value.filter { it.id != id }
            addAuditLog("BILL_DELETED", "Deleted recurring bill: ${target.title}")
            saveAll()
        }
    }

    fun markMonthlyPaymentPaid(paymentId: String, month: String, autoCreateExpense: Boolean = true) {
        val target = _monthlyPayments.value.find { it.id == paymentId } ?: return
        if (!target.paidMonths.contains(month)) {
            val updated = target.copy(paidMonths = target.paidMonths + month)
            _monthlyPayments.value = _monthlyPayments.value.map { if (it.id == paymentId) updated else it }
            if (autoCreateExpense) {
                val dateStr = String.format(Locale.US, "%s-%02d", month, target.dueDay.coerceIn(1, 28))
                val exp = TransactionItem(
                    type = "expense",
                    title = target.title,
                    amount = target.amount,
                    category = when (target.category.lowercase(Locale.US)) {
                        "rent" -> "Housing & Rent"
                        "subscription" -> "Entertainment"
                        "emi / loan" -> "Utilities"
                        "utility" -> "Utilities"
                        "insurance" -> "Health"
                        else -> "Other"
                    },
                    date = dateStr,
                    paymentMethod = if (target.autoPay) "Bank Transfer" else "Card",
                    notes = "Monthly bill: ${target.title} ($month)",
                    monthlyPaymentId = target.id
                )
                _transactions.value = listOf(exp) + _transactions.value
            }
            addAuditLog("BILL_PAID", "Marked bill paid: ${target.title} for $month")
            saveAll()
        }
    }

    fun unmarkMonthlyPaymentPaid(paymentId: String, month: String) {
        val target = _monthlyPayments.value.find { it.id == paymentId } ?: return
        if (target.paidMonths.contains(month)) {
            val updated = target.copy(paidMonths = target.paidMonths.filter { it != month })
            _monthlyPayments.value = _monthlyPayments.value.map { if (it.id == paymentId) updated else it }
            _transactions.value = _transactions.value.filter { !(it.monthlyPaymentId == paymentId && it.date.startsWith(month)) }
            addAuditLog("BILL_UNPAID", "Unmarked bill paid: ${target.title} for $month")
            saveAll()
        }
    }

    fun saveAudioFile(uri: Uri, name: String): String {
        return try {
            val audioDir = File(context.filesDir, "vault_audio")
            if (!audioDir.exists()) audioDir.mkdirs()
            val cleanName = name.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            val targetFile = File(audioDir, "${System.currentTimeMillis()}_$cleanName")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            uri.toString()
        }
    }

    fun enforceRegimeOnAll(requirePin: Boolean, require2fa: Boolean) {
        _documents.value = _documents.value.map { it.copy(requirePin = requirePin, require2fa = require2fa, locked = requirePin || require2fa) }
        _secrets.value = _secrets.value.map { it.copy(requirePin = requirePin, require2fa = require2fa) }
        _items.value = _items.value.map { it.copy(requirePin = requirePin, require2fa = require2fa, locked = requirePin || require2fa) }
        saveAll()
    }

    fun deleteDocument(id: String) {
        val target = _documents.value.find { it.id == id } ?: return
        _documents.value = _documents.value.filter { it.id != id }
        val trashItem = TrashItem(
            id = target.id,
            name = target.name,
            category = target.category,
            originalJson = target.toJson().toString(),
            deletedAt = System.currentTimeMillis()
        )
        _trash.value = listOf(trashItem) + _trash.value
        saveAll()
    }

    // --- Vault Item CRUD ---
    fun addItem(item: VaultItem) {
        _items.value = listOf(item) + _items.value
        scheduleItemAlarm(item)
        saveAll()
    }

    fun updateItem(item: VaultItem) {
        _items.value = _items.value.map { if (it.id == item.id) item else it }
        cancelItemAlarm(item.id)
        scheduleItemAlarm(item)
        saveAll()
    }

    fun deleteItem(id: String) {
        cancelItemAlarm(id)
        val target = _items.value.find { it.id == id } ?: return
        _items.value = _items.value.filter { it.id != id }
        val trashItem = TrashItem(
            id = target.id,
            name = target.name,
            category = target.category,
            originalJson = target.toJson().toString(),
            deletedAt = System.currentTimeMillis()
        )
        _trash.value = listOf(trashItem) + _trash.value
        saveAll()
    }

    fun markDone(id: String) {
        cancelItemAlarm(id)
        val item = _items.value.find { it.id == id } ?: return
        if (item.recurring && item.recurInterval > 0) {
            val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val cal = Calendar.getInstance()
            try {
                cal.time = df.parse(item.date) ?: cal.time
            } catch (_: Exception) {}
            cal.add(Calendar.DAY_OF_YEAR, item.recurInterval)
            val nextDate = df.format(cal.time)
            updateItem(item.copy(date = nextDate))
        } else {
            deleteItem(id)
        }
    }

    // --- Secret CRUD ---
    fun addSecret(secret: SecretItem) {
        _secrets.value = listOf(secret) + _secrets.value
        saveAll()
        scheduleSecretAlarm(secret)
    }

    fun updateSecret(secret: SecretItem) {
        _secrets.value = _secrets.value.map { if (it.id == secret.id) secret else it }
        saveAll()
        scheduleSecretAlarm(secret)
    }

    fun deleteSecret(id: String) {
        cancelSecretAlarm(id)
        val target = _secrets.value.find { it.id == id } ?: return
        _secrets.value = _secrets.value.filter { it.id != id }
        val trashItem = TrashItem(
            id = target.id,
            name = target.title,
            category = target.category,
            originalJson = target.toJson().toString(),
            deletedAt = System.currentTimeMillis()
        )
        _trash.value = listOf(trashItem) + _trash.value
        saveAll()
    }

    // --- Emergency Info ---
    fun saveEmergency(info: EmergencyInfo) {
        _emergency.value = info
        saveAll()
    }

    // --- Audio Schedules ---
    fun addSchedule(schedule: AudioSchedule) {
        _schedules.value = listOf(schedule) + _schedules.value
        saveAll()
        scheduleAlarm(context, schedule)
    }

    fun toggleSchedule(id: String) {
        _schedules.value = _schedules.value.map {
            if (it.id == id) it.copy(enabled = !it.enabled) else it
        }
        saveAll()
        val updated = _schedules.value.find { it.id == id }
        if (updated != null) {
            if (updated.enabled) {
                scheduleAlarm(context, updated)
            } else {
                cancelAlarm(context, id)
            }
        }
    }

    fun updateSchedule(schedule: AudioSchedule) {
        _schedules.value = _schedules.value.map { if (it.id == schedule.id) schedule else it }
        saveAll()
        cancelAlarm(context, schedule.id)
        if (schedule.enabled) {
            scheduleAlarm(context, schedule)
        }
    }

    fun deleteSchedule(id: String) {
        _schedules.value = _schedules.value.filter { it.id != id }
        saveAll()
        cancelAlarm(context, id)
    }

    private fun scheduleAlarm(context: Context, schedule: AudioSchedule) {
        if (!schedule.enabled) return
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = Intent(context, AudioAlarmReceiver::class.java).apply {
                putExtra("EXTRA_SCHEDULE_ID", schedule.id)
                putExtra("EXTRA_SCHEDULE_NAME", schedule.name)
                putExtra("EXTRA_SOUND_TONE", schedule.soundTone)
                putExtra("EXTRA_CUSTOM_URI", schedule.customAudioUri)
                putExtra("EXTRA_VOLUME", schedule.volume)
            }
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                schedule.id.hashCode(),
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            // Support both ISO format "2026-10-10T14:30" and "2026-10-10 14:30"
            val normalizedDt = schedule.dateTime.replace("T", " ").trim()
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            val sdfSec = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val date = try {
                sdf.parse(normalizedDt)
            } catch (_: Exception) {
                try { sdfSec.parse(normalizedDt) } catch (_: Exception) { null }
            }

            if (date != null) {
                val triggerTime = if (date.time > System.currentTimeMillis()) {
                    date.time
                } else if ((System.currentTimeMillis() - date.time) < 60_000L) {
                    // Within 1 minute grace period, fire in 2 seconds
                    System.currentTimeMillis() + 2_000L
                } else {
                    null
                }

                if (triggerTime != null) {
                    val showIntent = Intent(context, AlarmAlertActivity::class.java).apply {
                        putExtra("EXTRA_SCHEDULE_ID", schedule.id)
                        putExtra("EXTRA_SCHEDULE_NAME", schedule.name)
                    }
                    val showPendingIntent = android.app.PendingIntent.getActivity(
                        context,
                        schedule.id.hashCode() + 1,
                        showIntent,
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                    )
                    val alarmClockInfo = android.app.AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)

                    try {
                        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                    } catch (_: Exception) {
                        try {
                            alarmManager.setExactAndAllowWhileIdle(
                                android.app.AlarmManager.RTC_WAKEUP,
                                triggerTime,
                                pendingIntent
                            )
                        } catch (_: Exception) {
                            alarmManager.setAndAllowWhileIdle(
                                android.app.AlarmManager.RTC_WAKEUP,
                                triggerTime,
                                pendingIntent
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun scheduleSnooze(
        scheduleId: String,
        name: String,
        tone: String,
        uri: String?,
        volume: Int,
        minutes: Int = 5
    ) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = Intent(context, AudioAlarmReceiver::class.java).apply {
                putExtra("EXTRA_SCHEDULE_ID", scheduleId)
                putExtra("EXTRA_SCHEDULE_NAME", "$name (Snoozed)")
                putExtra("EXTRA_SOUND_TONE", tone)
                putExtra("EXTRA_CUSTOM_URI", uri)
                putExtra("EXTRA_VOLUME", volume)
            }
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                scheduleId.hashCode(),
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            val triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000L)
            val showIntent = Intent(context, AlarmAlertActivity::class.java).apply {
                putExtra("EXTRA_SCHEDULE_ID", scheduleId)
                putExtra("EXTRA_SCHEDULE_NAME", "$name (Snoozed)")
            }
            val showPendingIntent = android.app.PendingIntent.getActivity(
                context,
                scheduleId.hashCode() + 1,
                showIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            val alarmClockInfo = android.app.AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
            try {
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } catch (_: Exception) {
                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cancelAlarm(context: Context, scheduleId: String) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = Intent(context, AudioAlarmReceiver::class.java)
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                scheduleId.hashCode(),
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        } catch (_: Exception) {}
    }

    fun scheduleItemAlarm(item: VaultItem) {
        val soundTone = item.audioId ?: return
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = Intent(context, AudioAlarmReceiver::class.java).apply {
                putExtra("EXTRA_SCHEDULE_ID", item.id)
                putExtra("EXTRA_SCHEDULE_NAME", "Due: ${item.name}")
                putExtra("EXTRA_SOUND_TONE", soundTone)
                putExtra("EXTRA_VOLUME", 90)
            }
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                item.id.hashCode(),
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val timePart = if (!item.audioTime.isNullOrBlank()) item.audioTime else "09:00"
            val dateTimeStr = "${item.date.trim()} ${timePart.trim()}"
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            val date = try {
                sdf.parse(dateTimeStr)
            } catch (_: Exception) {
                null
            }

            if (date != null) {
                val triggerTime = if (date.time > System.currentTimeMillis()) {
                    date.time
                } else if ((System.currentTimeMillis() - date.time) < 60_000L) {
                    System.currentTimeMillis() + 2_000L
                } else {
                    null
                }

                if (triggerTime != null) {
                    val showIntent = Intent(context, AlarmAlertActivity::class.java).apply {
                        putExtra("EXTRA_SCHEDULE_ID", item.id)
                        putExtra("EXTRA_SCHEDULE_NAME", "Due: ${item.name}")
                        putExtra("EXTRA_SOUND_TONE", soundTone)
                    }
                    val showPendingIntent = android.app.PendingIntent.getActivity(
                        context,
                        item.id.hashCode() + 1,
                        showIntent,
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                    )
                    val alarmClockInfo = android.app.AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
                    try {
                        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                    } catch (_: Exception) {
                        try {
                            alarmManager.setExactAndAllowWhileIdle(
                                android.app.AlarmManager.RTC_WAKEUP,
                                triggerTime,
                                pendingIntent
                            )
                        } catch (_: Exception) {
                            alarmManager.setAndAllowWhileIdle(
                                android.app.AlarmManager.RTC_WAKEUP,
                                triggerTime,
                                pendingIntent
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelItemAlarm(itemId: String) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = Intent(context, AudioAlarmReceiver::class.java)
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                itemId.hashCode(),
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        } catch (_: Exception) {}
    }

    fun scheduleSecretAlarm(secret: SecretItem) {
        cancelSecretAlarm(secret.id)
        if (!secret.expirationAlertEnabled || secret.expirationDate.isNullOrBlank()) return

        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = Intent(context, AudioAlarmReceiver::class.java).apply {
                putExtra("EXTRA_SCHEDULE_ID", "secret_${secret.id}")
                putExtra("EXTRA_SCHEDULE_NAME", "Password Expiring: ${secret.title}")
                putExtra("EXTRA_SOUND_TONE", secret.expirationAudioTone)
                putExtra("EXTRA_CUSTOM_URI", secret.expirationCustomAudioUri)
                putExtra("EXTRA_VOLUME", 90)
            }
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                secret.id.hashCode() + 7000,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val timePart = if (!secret.expirationTime.isNullOrBlank()) secret.expirationTime else "23:59"
            val dateTimeStr = "${secret.expirationDate} $timePart"
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            val date = try { sdf.parse(dateTimeStr) } catch (_: Exception) { null }

            if (date != null) {
                val triggerTime = if (date.time > System.currentTimeMillis()) {
                    date.time
                } else if ((System.currentTimeMillis() - date.time) < 60_000L) {
                    System.currentTimeMillis() + 2_000L
                } else {
                    null
                }

                if (triggerTime != null) {
                    val showIntent = Intent(context, AlarmAlertActivity::class.java).apply {
                        putExtra("EXTRA_SCHEDULE_ID", "secret_${secret.id}")
                        putExtra("EXTRA_SCHEDULE_NAME", "Password Expiring: ${secret.title}")
                        putExtra("EXTRA_SOUND_TONE", secret.expirationAudioTone)
                        putExtra("EXTRA_CUSTOM_URI", secret.expirationCustomAudioUri)
                    }
                    val showPendingIntent = android.app.PendingIntent.getActivity(
                        context,
                        secret.id.hashCode() + 7001,
                        showIntent,
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                    )
                    val alarmClockInfo = android.app.AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
                    try {
                        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                    } catch (_: Exception) {
                        try {
                            alarmManager.setExactAndAllowWhileIdle(
                                android.app.AlarmManager.RTC_WAKEUP,
                                triggerTime,
                                pendingIntent
                            )
                        } catch (_: Exception) {
                            alarmManager.setAndAllowWhileIdle(
                                android.app.AlarmManager.RTC_WAKEUP,
                                triggerTime,
                                pendingIntent
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelSecretAlarm(secretId: String) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = Intent(context, AudioAlarmReceiver::class.java)
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                secretId.hashCode() + 7000,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        } catch (_: Exception) {}
    }

    // --- Trash ---
    fun restoreTrash(id: String) {
        val target = _trash.value.find { it.id == id } ?: return
        _trash.value = _trash.value.filter { it.id != id }
        try {
            val json = JSONObject(target.originalJson)
            when {
                json.has("title") -> { // Secret
                    val secret = SecretItem.fromJson(json)
                    _secrets.value = listOf(secret) + _secrets.value
                    scheduleSecretAlarm(secret)
                }
                json.has("uri") -> { // Document
                    val doc = SecureDocument.fromJson(json)
                    _documents.value = listOf(doc) + _documents.value
                }
                else -> { // VaultItem
                    val item = VaultItem.fromJson(json)
                    _items.value = listOf(item) + _items.value
                }
            }
        } catch (_: Exception) {}
        saveAll()
    }

    fun purgeTrash(id: String) {
        _trash.value = _trash.value.filter { it.id != id }
        saveAll()
    }

    fun clearAllData() {
        _items.value = emptyList()
        _secrets.value = emptyList()
        _documents.value = emptyList()
        _emergency.value = EmergencyInfo()
        _schedules.value = emptyList()
        _trash.value = emptyList()
        _settings.value = AppSettings()
        prefs.edit().clear().putBoolean("seeded_v7", true).apply()
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        seedInitialData()
    }

    // --- Settings ---
    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        saveAll()
    }

    // --- Backup & Restore ---
    fun exportBackupJson(pin: String = ""): String {
        val root = JSONObject()
        root.put("version", 8)
        root.put("exportedAt", System.currentTimeMillis())

        val itemsArr = JSONArray()
        _items.value.forEach { itemsArr.put(it.toJson()) }
        root.put("items", itemsArr)

        val secretsArr = JSONArray()
        _secrets.value.forEach { secretsArr.put(it.toJson()) }
        root.put("secrets", secretsArr)

        val docsArr = JSONArray()
        _documents.value.forEach { docsArr.put(it.toJson()) }
        root.put("documents", docsArr)

        root.put("emergency", _emergency.value.toJson())

        val schedArr = JSONArray()
        _schedules.value.forEach { schedArr.put(it.toJson()) }
        root.put("schedules", schedArr)

        root.put("settings", _settings.value.toJson())
        val jsonStr = root.toString(2)
        
        if (pin.isNotEmpty()) {
            return com.example.util.BackupCrypto.encrypt(jsonStr, pin)
        }
        return jsonStr
    }

    fun exportBackupToFile(pin: String = ""): String? {
        return try {
            val json = exportBackupJson(pin)
            val downloadsDir = context.getExternalFilesDirs(null).firstOrNull() ?: context.filesDir
            val downloadFolder = File(downloadsDir, "Downloads").apply { if (!exists()) mkdirs() }
            val fileName = "MyVault_Backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(java.util.Date())}.json"
            val file = File(downloadFolder, fileName)
            file.writeText(json, Charsets.UTF_8)
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun importBackupJson(jsonString: String, pin: String = ""): Boolean {
        return try {
            val decryptedStr = if (jsonString.trim().startsWith("{") && jsonString.contains("salt") && jsonString.contains("data")) {
                if (pin.isEmpty()) return false
                com.example.util.BackupCrypto.decrypt(jsonString, pin) ?: return false
            } else {
                if (pin.isNotEmpty()) {
                    com.example.util.BackupCrypto.decrypt(jsonString, pin) ?: jsonString
                } else {
                    jsonString
                }
            }

            val root = JSONObject(decryptedStr)

            val loadedItems = mutableListOf<VaultItem>()
            val itemsArr = root.optJSONArray("items") ?: JSONArray()
            for (i in 0 until itemsArr.length()) {
                loadedItems.add(VaultItem.fromJson(itemsArr.getJSONObject(i)))
            }

            val loadedSecrets = mutableListOf<SecretItem>()
            val secretsArr = root.optJSONArray("secrets") ?: JSONArray()
            for (i in 0 until secretsArr.length()) {
                loadedSecrets.add(SecretItem.fromJson(secretsArr.getJSONObject(i)))
            }

            val loadedDocs = mutableListOf<SecureDocument>()
            val docsArr = root.optJSONArray("documents") ?: JSONArray()
            for (i in 0 until docsArr.length()) {
                loadedDocs.add(SecureDocument.fromJson(docsArr.getJSONObject(i)))
            }

            val loadedEmg = EmergencyInfo.fromJson(root.optJSONObject("emergency"))

            val loadedSched = mutableListOf<AudioSchedule>()
            val schedArr = root.optJSONArray("schedules") ?: JSONArray()
            for (i in 0 until schedArr.length()) {
                loadedSched.add(AudioSchedule.fromJson(schedArr.getJSONObject(i)))
            }

            val loadedSettings = AppSettings.fromJson(root.optJSONObject("settings"))

            _items.value = loadedItems
            _secrets.value = loadedSecrets
            _documents.value = loadedDocs
            _emergency.value = loadedEmg
            _schedules.value = loadedSched
            _settings.value = loadedSettings
            saveAll()
            true
        } catch (_: Exception) {
            false
        }
    }
}
