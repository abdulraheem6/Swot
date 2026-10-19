package com.example.data

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class VaultItem(
    val id: String,
    val name: String,
    val category: String, // "product", "document", "subscription", "card", "event"
    val date: String,     // ISO YYYY-MM-DD
    val notes: String = "",
    val cardNumber: String = "",
    val recurring: Boolean = false,
    val recurInterval: Int = 0, // days
    val locked: Boolean = false,
    val requirePin: Boolean = true,
    val require2fa: Boolean = false,
    val customPin: String = "",
    val custom2faKey: String = "",
    val audioId: String? = null,
    val audioTime: String? = null,
    val attachments: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val customFields: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("category", category)
        put("date", date)
        put("notes", notes)
        put("cardNumber", cardNumber)
        put("recurring", recurring)
        put("recurInterval", recurInterval)
        put("locked", locked)
        put("requirePin", requirePin)
        put("require2fa", require2fa)
        put("customPin", customPin)
        put("custom2faKey", custom2faKey)
        put("audioId", audioId ?: JSONObject.NULL)
        put("audioTime", audioTime ?: JSONObject.NULL)
        val attachArr = JSONArray()
        attachments.forEach { attachArr.put(it) }
        put("attachments", attachArr)
        val tagsArr = JSONArray()
        tags.forEach { tagsArr.put(it) }
        put("tags", tagsArr)
        val customObj = JSONObject()
        customFields.forEach { (k, v) -> customObj.put(k, v) }
        put("customFields", customObj)
        put("createdAt", createdAt)
    }

    companion object {
        fun fromJson(obj: JSONObject): VaultItem {
            val attachList = mutableListOf<String>()
            val attachArr = obj.optJSONArray("attachments")
            if (attachArr != null) {
                for (i in 0 until attachArr.length()) {
                    attachList.add(attachArr.optString(i))
                }
            }
            val tagList = mutableListOf<String>()
            val tagArr = obj.optJSONArray("tags")
            if (tagArr != null) {
                for (i in 0 until tagArr.length()) {
                    tagList.add(tagArr.optString(i))
                }
            }
            val customMap = mutableMapOf<String, String>()
            val customObj = obj.optJSONObject("customFields")
            if (customObj != null) {
                val keys = customObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    customMap[k] = customObj.optString(k)
                }
            }
            return VaultItem(
                id = obj.optString("id"),
                name = obj.optString("name"),
                category = obj.optString("category", "product"),
                date = obj.optString("date"),
                notes = obj.optString("notes"),
                cardNumber = obj.optString("cardNumber"),
                recurring = obj.optBoolean("recurring", false),
                recurInterval = obj.optInt("recurInterval", 0),
                locked = obj.optBoolean("locked", false),
                requirePin = obj.optBoolean("requirePin", obj.optBoolean("locked", true)),
                require2fa = obj.optBoolean("require2fa", false),
                customPin = obj.optString("customPin", ""),
                custom2faKey = obj.optString("custom2faKey", ""),
                audioId = if (obj.has("audioId") && !obj.isNull("audioId")) obj.getString("audioId") else null,
                audioTime = if (obj.has("audioTime") && !obj.isNull("audioTime")) obj.getString("audioTime") else null,
                attachments = attachList,
                tags = tagList,
                customFields = customMap,
                createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            )
        }
    }
}

data class SecretItem(
    val id: String,
    val category: String, // "login", "bank", "card", "wifi", "identity", "api", "recovery", "medical", "vehicle", "crypto", "note"
    val title: String,
    val subtitle: String = "",
    val favorite: Boolean = false,
    val requirePin: Boolean = true,
    val require2fa: Boolean = false,
    val customPin: String = "",
    val custom2faKey: String = "",
    val fields: Map<String, String> = emptyMap(),
    val customFields: Map<String, String> = emptyMap(),
    val attachments: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val notes: String = "",
    val expirationDate: String? = null, // "YYYY-MM-DD"
    val expirationTime: String? = null, // "HH:MM"
    val expirationAlertEnabled: Boolean = false,
    val expirationAudioTone: String = "chime", // "chime", "radar", "gentle", "alarm", "digital", "custom"
    val expirationCustomAudioUri: String? = null,
    val expirationCustomAudioName: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isExpired: Boolean
        get() {
            if (expirationDate.isNullOrBlank()) return false
            val timePart = if (!expirationTime.isNullOrBlank()) expirationTime else "23:59"
            return try {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
                val expDate = sdf.parse("$expirationDate $timePart")
                expDate != null && expDate.time < System.currentTimeMillis()
            } catch (_: Exception) {
                false
            }
        }

    val isNearExpiration: Boolean
        get() {
            if (expirationDate.isNullOrBlank() || isExpired) return false
            val timePart = if (!expirationTime.isNullOrBlank()) expirationTime else "23:59"
            return try {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
                val expDate = sdf.parse("$expirationDate $timePart")
                if (expDate != null) {
                    val diff = expDate.time - System.currentTimeMillis()
                    val diffDays = diff / (1000.0 * 60 * 60 * 24)
                    diffDays in 0.0..7.0
                } else false
            } catch (_: Exception) {
                false
            }
        }

    val daysUntilExpiration: Long?
        get() {
            if (expirationDate.isNullOrBlank()) return null
            val timePart = if (!expirationTime.isNullOrBlank()) expirationTime else "23:59"
            return try {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
                val expDate = sdf.parse("$expirationDate $timePart")
                if (expDate != null) {
                    val diff = expDate.time - System.currentTimeMillis()
                    val days = Math.ceil(diff / (1000.0 * 60 * 60 * 24)).toLong()
                    maxOf(0L, days)
                } else null
            } catch (_: Exception) {
                null
            }
        }

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("category", category)
        put("title", title)
        put("subtitle", subtitle)
        put("favorite", favorite)
        put("requirePin", requirePin)
        put("require2fa", require2fa)
        put("customPin", customPin)
        put("custom2faKey", custom2faKey)
        val fieldsObj = JSONObject()
        fields.forEach { (k, v) -> fieldsObj.put(k, v) }
        put("fields", fieldsObj)

        val customFieldsObj = JSONObject()
        customFields.forEach { (k, v) -> customFieldsObj.put(k, v) }
        put("customFields", customFieldsObj)

        val attachArr = JSONArray()
        attachments.forEach { attachArr.put(it) }
        put("attachments", attachArr)

        val tagsArr = JSONArray()
        tags.forEach { tagsArr.put(it) }
        put("tags", tagsArr)

        put("notes", notes)
        put("expirationDate", expirationDate ?: JSONObject.NULL)
        put("expirationTime", expirationTime ?: JSONObject.NULL)
        put("expirationAlertEnabled", expirationAlertEnabled)
        put("expirationAudioTone", expirationAudioTone)
        put("expirationCustomAudioUri", expirationCustomAudioUri ?: JSONObject.NULL)
        put("expirationCustomAudioName", expirationCustomAudioName ?: JSONObject.NULL)
        put("createdAt", createdAt)
    }

    companion object {
        fun fromJson(obj: JSONObject): SecretItem {
            val fieldsMap = mutableMapOf<String, String>()
            val fieldsObj = obj.optJSONObject("fields")
            if (fieldsObj != null) {
                val keys = fieldsObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    fieldsMap[k] = fieldsObj.optString(k)
                }
            }

            val customMap = mutableMapOf<String, String>()
            val customObj = obj.optJSONObject("customFields")
            if (customObj != null) {
                val keys = customObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    customMap[k] = customObj.optString(k)
                }
            }

            val attachList = mutableListOf<String>()
            val attachArr = obj.optJSONArray("attachments")
            if (attachArr != null) {
                for (i in 0 until attachArr.length()) {
                    attachList.add(attachArr.optString(i))
                }
            }

            val tagList = mutableListOf<String>()
            val tagArr = obj.optJSONArray("tags")
            if (tagArr != null) {
                for (i in 0 until tagArr.length()) {
                    tagList.add(tagArr.optString(i))
                }
            }

            return SecretItem(
                id = obj.optString("id"),
                category = obj.optString("category", "login"),
                title = obj.optString("title"),
                subtitle = obj.optString("subtitle"),
                favorite = obj.optBoolean("favorite", false),
                requirePin = obj.optBoolean("requirePin", true),
                require2fa = obj.optBoolean("require2fa", false),
                customPin = obj.optString("customPin", ""),
                custom2faKey = obj.optString("custom2faKey", ""),
                fields = fieldsMap,
                customFields = customMap,
                attachments = attachList,
                tags = tagList,
                notes = obj.optString("notes"),
                expirationDate = if (obj.has("expirationDate") && !obj.isNull("expirationDate")) obj.getString("expirationDate") else null,
                expirationTime = if (obj.has("expirationTime") && !obj.isNull("expirationTime")) obj.getString("expirationTime") else null,
                expirationAlertEnabled = obj.optBoolean("expirationAlertEnabled", false),
                expirationAudioTone = obj.optString("expirationAudioTone", "chime"),
                expirationCustomAudioUri = if (obj.has("expirationCustomAudioUri") && !obj.isNull("expirationCustomAudioUri")) obj.getString("expirationCustomAudioUri") else null,
                expirationCustomAudioName = if (obj.has("expirationCustomAudioName") && !obj.isNull("expirationCustomAudioName")) obj.getString("expirationCustomAudioName") else null,
                createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            )
        }
    }
}

data class SecureDocument(
    val id: String,
    val name: String,
    val category: String, // "photo", "video", "document", "id", "contract", "receipt", "financial", "medical"
    val uri: String,
    val fileSize: Long = 0,
    val notes: String = "",
    val locked: Boolean = true,
    val isVideo: Boolean = false,
    val requirePin: Boolean = true,
    val require2fa: Boolean = false,
    val customPin: String = "",
    val custom2faKey: String = "",
    val originalFileName: String = "",
    val mimeType: String = "",
    val isEncrypted: Boolean = true,
    val customFields: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("category", category)
        put("uri", uri)
        put("fileSize", fileSize)
        put("notes", notes)
        put("locked", locked)
        put("isVideo", isVideo || category == "video")
        put("requirePin", requirePin)
        put("require2fa", require2fa)
        put("customPin", customPin)
        put("custom2faKey", custom2faKey)
        put("originalFileName", originalFileName)
        put("mimeType", mimeType)
        put("isEncrypted", isEncrypted)
        val customObj = JSONObject()
        customFields.forEach { (k, v) -> customObj.put(k, v) }
        put("customFields", customObj)
        put("createdAt", createdAt)
    }

    companion object {
        fun fromJson(obj: JSONObject): SecureDocument {
            val customMap = mutableMapOf<String, String>()
            val customObj = obj.optJSONObject("customFields")
            if (customObj != null) {
                val keys = customObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    customMap[k] = customObj.optString(k)
                }
            }
            val cat = obj.optString("category", "photo")
            val isVid = obj.optBoolean("isVideo", cat == "video")
            val isLocked = obj.optBoolean("locked", true)
            return SecureDocument(
                id = obj.optString("id"),
                name = obj.optString("name"),
                category = cat,
                uri = obj.optString("uri"),
                fileSize = obj.optLong("fileSize", 0),
                notes = obj.optString("notes"),
                locked = isLocked,
                isVideo = isVid,
                requirePin = obj.optBoolean("requirePin", isLocked),
                require2fa = obj.optBoolean("require2fa", false),
                customPin = obj.optString("customPin", ""),
                custom2faKey = obj.optString("custom2faKey", ""),
                originalFileName = obj.optString("originalFileName", ""),
                mimeType = obj.optString("mimeType", ""),
                isEncrypted = obj.optBoolean("isEncrypted", true),
                customFields = customMap,
                createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            )
        }
    }
}

data class AuditLogEntry(
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val action: String, // "VAULT_UNLOCK", "CARD_SCAN", "RECEIPT_SCAN", "ITEM_ADDED", "DOC_SAVED", "EXPORT_REPORT"
    val details: String,
    val actor: String = "Vault User"
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("timestamp", timestamp)
        put("action", action)
        put("details", details)
        put("actor", actor)
    }

    companion object {
        fun fromJson(obj: JSONObject): AuditLogEntry = AuditLogEntry(
            id = obj.optString("id"),
            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
            action = obj.optString("action"),
            details = obj.optString("details"),
            actor = obj.optString("actor", "Vault User")
        )
    }
}

data class EmergencyInfo(
    val blood: String = "",
    val allergies: String = "",
    val conditions: String = "",
    val meds: String = "",
    val contactName: String = "",
    val contactPhone: String = "",
    val doctor: String = "",
    val organ: String = "",
    val notes: String = ""
) {
    val isConfigured: Boolean
        get() = blood.isNotBlank() || allergies.isNotBlank() || conditions.isNotBlank() ||
                meds.isNotBlank() || contactName.isNotBlank() || contactPhone.isNotBlank() ||
                doctor.isNotBlank() || organ.isNotBlank() || notes.isNotBlank()

    fun toJson(): JSONObject = JSONObject().apply {
        put("blood", blood)
        put("allergies", allergies)
        put("conditions", conditions)
        put("meds", meds)
        put("contactName", contactName)
        put("contactPhone", contactPhone)
        put("doctor", doctor)
        put("organ", organ)
        put("notes", notes)
    }

    companion object {
        fun fromJson(obj: JSONObject?): EmergencyInfo {
            if (obj == null) return EmergencyInfo()
            return EmergencyInfo(
                blood = obj.optString("blood"),
                allergies = obj.optString("allergies"),
                conditions = obj.optString("conditions"),
                meds = obj.optString("meds"),
                contactName = obj.optString("contactName"),
                contactPhone = obj.optString("contactPhone"),
                doctor = obj.optString("doctor"),
                organ = obj.optString("organ"),
                notes = obj.optString("notes")
            )
        }
    }
}

data class AudioSchedule(
    val id: String,
    val name: String,
    val soundTone: String = "chime", // "chime", "radar", "gentle", "alarm", "pulse", "custom"
    val customAudioUri: String? = null,
    val customAudioName: String? = null,
    val dateTime: String,            // ISO "YYYY-MM-DDTHH:MM"
    val repeat: String = "none",     // "none", "daily", "weekly", "monthly", "yearly"
    val volume: Int = 80,
    val enabled: Boolean = true,
    val lastFired: String? = null,
    val linkedItemId: String? = null
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("soundTone", soundTone)
        put("customAudioUri", customAudioUri ?: JSONObject.NULL)
        put("customAudioName", customAudioName ?: JSONObject.NULL)
        put("dateTime", dateTime)
        put("repeat", repeat)
        put("volume", volume)
        put("enabled", enabled)
        put("lastFired", lastFired ?: JSONObject.NULL)
        put("linkedItemId", linkedItemId ?: JSONObject.NULL)
    }

    companion object {
        fun fromJson(obj: JSONObject): AudioSchedule = AudioSchedule(
            id = obj.optString("id"),
            name = obj.optString("name"),
            soundTone = obj.optString("soundTone", "chime"),
            customAudioUri = if (obj.has("customAudioUri") && !obj.isNull("customAudioUri")) obj.getString("customAudioUri") else null,
            customAudioName = if (obj.has("customAudioName") && !obj.isNull("customAudioName")) obj.getString("customAudioName") else null,
            dateTime = obj.optString("dateTime"),
            repeat = obj.optString("repeat", "none"),
            volume = obj.optInt("volume", 80),
            enabled = obj.optBoolean("enabled", true),
            lastFired = if (obj.has("lastFired") && !obj.isNull("lastFired")) obj.getString("lastFired") else null,
            linkedItemId = if (obj.has("linkedItemId") && !obj.isNull("linkedItemId")) obj.getString("linkedItemId") else null
        )
    }
}

data class TrashItem(
    val id: String,
    val name: String,
    val category: String,
    val originalJson: String,
    val deletedAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("category", category)
        put("originalJson", originalJson)
        put("deletedAt", deletedAt)
    }

    companion object {
        fun fromJson(obj: JSONObject): TrashItem = TrashItem(
            id = obj.optString("id"),
            name = obj.optString("name"),
            category = obj.optString("category"),
            originalJson = obj.optString("originalJson"),
            deletedAt = obj.optLong("deletedAt", System.currentTimeMillis())
        )
    }
}

data class AppSettings(
    val pin: String = "123456",
    val master2faKey: String = "123456",
    val securityRegime: String = "standard", // "standard", "high_security", "strict_enclave"
    val timeOfReset: String = "00:00 UTC",
    val username: String = "Alex Rivera",
    val organization: String = "CyberVault Security",
    val department: String = "Cyber Defense & Operations",
    val employeeId: String = "EMP-9482-SEC",
    val securityClearance: String = "Level 4 - Confidential Enclave",
    val workEmail: String = "alex.rivera@cybervault.io",
    val themeName: String = "bright_light",
    val autoLockMinutes: Int = 5,
    val notifs: Boolean = true,
    val audioEnabled: Boolean = true,
    val personalizedAds: Boolean = false,
    val adFree: Boolean = true,
    val widget: Boolean = true,
    val clipboardClear: Boolean = true,
    val masterVolume: Int = 80
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("pin", pin)
        put("master2faKey", master2faKey)
        put("securityRegime", securityRegime)
        put("timeOfReset", timeOfReset)
        put("username", username)
        put("organization", organization)
        put("department", department)
        put("employeeId", employeeId)
        put("securityClearance", securityClearance)
        put("workEmail", workEmail)
        put("themeName", themeName)
        put("autoLockMinutes", autoLockMinutes)
        put("notifs", notifs)
        put("audioEnabled", audioEnabled)
        put("personalizedAds", personalizedAds)
        put("adFree", adFree)
        put("widget", widget)
        put("clipboardClear", clipboardClear)
        put("masterVolume", masterVolume)
    }

    companion object {
        fun fromJson(obj: JSONObject?): AppSettings {
            if (obj == null) return AppSettings()
            val rawPin = obj.optString("pin", "123456")
            val validatedPin = if (rawPin.length == 6 && rawPin.all { it.isDigit() }) rawPin else "123456"
            return AppSettings(
                pin = validatedPin,
                master2faKey = obj.optString("master2faKey", "123456"),
                securityRegime = obj.optString("securityRegime", "standard"),
                timeOfReset = obj.optString("timeOfReset", "00:00 UTC"),
                username = obj.optString("username", "Alex Rivera"),
                organization = obj.optString("organization", "CyberVault Security"),
                department = obj.optString("department", "Cyber Defense & Operations"),
                employeeId = obj.optString("employeeId", "EMP-9482-SEC"),
                securityClearance = obj.optString("securityClearance", "Level 4 - Confidential Enclave"),
                workEmail = obj.optString("workEmail", "alex.rivera@cybervault.io"),
                themeName = obj.optString("themeName", "bright_light"),
                autoLockMinutes = obj.optInt("autoLockMinutes", 5),
                notifs = obj.optBoolean("notifs", true),
                audioEnabled = obj.optBoolean("audioEnabled", true),
                personalizedAds = obj.optBoolean("personalizedAds", false),
                adFree = obj.optBoolean("adFree", true),
                widget = obj.optBoolean("widget", true),
                clipboardClear = obj.optBoolean("clipboardClear", true),
                masterVolume = obj.optInt("masterVolume", 80)
            )
        }
    }
}

data class MemberContact(
    val id: String,
    val name: String,
    val category: String, // "family", "electrician", "plumber", "barber", "doctor", "mechanic", "contractor", "other"
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val notes: String = "",
    val specialty: String = "",
    val avatarEmoji: String = "👤"
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("category", category)
        put("phone", phone)
        put("email", email)
        put("address", address)
        put("notes", notes)
        put("specialty", specialty)
        put("avatarEmoji", avatarEmoji)
    }

    companion object {
        fun fromJson(obj: JSONObject): MemberContact = MemberContact(
            id = obj.optString("id", UUID.randomUUID().toString()),
            name = obj.optString("name", "Unknown Contact"),
            category = obj.optString("category", "other"),
            phone = obj.optString("phone", ""),
            email = obj.optString("email", ""),
            address = obj.optString("address", ""),
            notes = obj.optString("notes", ""),
            specialty = obj.optString("specialty", ""),
            avatarEmoji = obj.optString("avatarEmoji", "👤")
        )
    }
}

data class TransactionItem(
    val id: String = UUID.randomUUID().toString(),
    val type: String, // "expense" or "income"
    val title: String,
    val amount: Double,
    val category: String, // "Food & Groceries", "Housing & Rent", "Utilities", "Transportation", "Shopping", "Entertainment", "Health", "Salary", "Freelance", "Investment", "Business", "Other"
    val date: String, // YYYY-MM-DD
    val paymentMethod: String = "Card", // "Card", "Cash", "UPI/Online", "Bank Transfer"
    val notes: String = "",
    val receiptUri: String? = null,
    val receiptName: String = "",
    val isTaxDeductible: Boolean = false,
    val monthlyPaymentId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("type", type)
        put("title", title)
        put("amount", amount)
        put("category", category)
        put("date", date)
        put("paymentMethod", paymentMethod)
        put("notes", notes)
        put("receiptUri", receiptUri ?: JSONObject.NULL)
        put("receiptName", receiptName)
        put("isTaxDeductible", isTaxDeductible)
        put("monthlyPaymentId", monthlyPaymentId ?: JSONObject.NULL)
        put("createdAt", createdAt)
    }

    companion object {
        fun fromJson(obj: JSONObject): TransactionItem = TransactionItem(
            id = obj.optString("id", UUID.randomUUID().toString()),
            type = obj.optString("type", "expense"),
            title = obj.optString("title", "Untitled Transaction"),
            amount = obj.optDouble("amount", 0.0),
            category = obj.optString("category", "Other"),
            date = obj.optString("date", ""),
            paymentMethod = obj.optString("paymentMethod", "Card"),
            notes = obj.optString("notes", ""),
            receiptUri = if (obj.has("receiptUri") && !obj.isNull("receiptUri")) obj.getString("receiptUri") else null,
            receiptName = obj.optString("receiptName", ""),
            isTaxDeductible = obj.optBoolean("isTaxDeductible", false),
            monthlyPaymentId = if (obj.has("monthlyPaymentId") && !obj.isNull("monthlyPaymentId")) obj.getString("monthlyPaymentId") else null,
            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
        )
    }
}

data class MonthlyBudget(
    val id: String = UUID.randomUUID().toString(),
    val month: String, // "YYYY-MM"
    val totalBudget: Double = 3000.0,
    val categoryBudgets: Map<String, Double> = emptyMap()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("month", month)
        put("totalBudget", totalBudget)
        val catObj = JSONObject()
        categoryBudgets.forEach { (k, v) -> catObj.put(k, v) }
        put("categoryBudgets", catObj)
    }

    companion object {
        fun fromJson(obj: JSONObject): MonthlyBudget {
            val catMap = mutableMapOf<String, Double>()
            val catObj = obj.optJSONObject("categoryBudgets")
            if (catObj != null) {
                val keys = catObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    catMap[k] = catObj.optDouble(k, 0.0)
                }
            }
            return MonthlyBudget(
                id = obj.optString("id", UUID.randomUUID().toString()),
                month = obj.optString("month", ""),
                totalBudget = obj.optDouble("totalBudget", 3000.0),
                categoryBudgets = catMap
            )
        }
    }
}

data class MonthlyPaymentItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val dueDay: Int = 1, // 1 to 31
    val category: String = "Subscription", // "Rent", "Subscription", "EMI / Loan", "Utility", "Insurance", "Education", "Other"
    val autoPay: Boolean = false,
    val notes: String = "",
    val alarmEnabled: Boolean = true,
    val alarmTime: String = "09:00",
    val paidMonths: List<String> = emptyList(), // "YYYY-MM"
    val createdAt: Long = System.currentTimeMillis()
) {
    fun isPaidForMonth(month: String): Boolean = paidMonths.contains(month)

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("amount", amount)
        put("dueDay", dueDay)
        put("category", category)
        put("autoPay", autoPay)
        put("notes", notes)
        put("alarmEnabled", alarmEnabled)
        put("alarmTime", alarmTime)
        val monthsArr = JSONArray()
        paidMonths.forEach { monthsArr.put(it) }
        put("paidMonths", monthsArr)
        put("createdAt", createdAt)
    }

    companion object {
        fun fromJson(obj: JSONObject): MonthlyPaymentItem {
            val monthsList = mutableListOf<String>()
            val monthsArr = obj.optJSONArray("paidMonths")
            if (monthsArr != null) {
                for (i in 0 until monthsArr.length()) {
                    monthsList.add(monthsArr.optString(i))
                }
            }
            return MonthlyPaymentItem(
                id = obj.optString("id", UUID.randomUUID().toString()),
                title = obj.optString("title", "Untitled Bill"),
                amount = obj.optDouble("amount", 0.0),
                dueDay = obj.optInt("dueDay", 1),
                category = obj.optString("category", "Subscription"),
                autoPay = obj.optBoolean("autoPay", false),
                notes = obj.optString("notes", ""),
                alarmEnabled = obj.optBoolean("alarmEnabled", true),
                alarmTime = obj.optString("alarmTime", "09:00"),
                paidMonths = monthsList,
                createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            )
        }
    }
}
