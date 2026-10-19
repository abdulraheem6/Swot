package com.example

import com.example.data.EmergencyInfo
import com.example.data.SecretItem
import com.example.data.VaultItem
import com.example.util.PasswordUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testPasswordGenerationLength() {
        val pw16 = PasswordUtils.generatePassword(16)
        assertEquals(16, pw16.length)

        val pw32 = PasswordUtils.generatePassword(32)
        assertEquals(32, pw32.length)
    }

    @Test
    fun testPasswordStrength() {
        val weak = PasswordUtils.calculateStrength("1234")
        assertTrue(weak.score <= 1)

        val strong = PasswordUtils.calculateStrength("T7#mK9!wQz84@LaP")
        assertTrue(strong.score >= 3)
    }

    @Test
    fun testVaultItemModel() {
        val item = VaultItem(
            id = "test_1",
            name = "Passport",
            category = "document",
            date = "2026-12-31",
            notes = "International Travel",
            recurring = true,
            recurInterval = 365,
            locked = true
        )

        assertEquals("test_1", item.id)
        assertEquals("Passport", item.name)
        assertEquals("document", item.category)
        assertTrue(item.recurring)
        assertEquals(365, item.recurInterval)
        assertTrue(item.locked)
    }

    @Test
    fun testSecretItemModel() {
        val secret = SecretItem(
            id = "sec_1",
            category = "login",
            title = "GitHub",
            fields = mapOf("username" to "dev", "password" to "secret123"),
            favorite = true
        )

        assertEquals("GitHub", secret.title)
        assertTrue(secret.favorite)
        assertEquals("secret123", secret.fields["password"])
    }

    @Test
    fun testEmergencyInfoConfigured() {
        val empty = EmergencyInfo()
        assertEquals(false, empty.isConfigured)

        val configured = EmergencyInfo(blood = "O+", contactPhone = "+15551234")
        assertEquals(true, configured.isConfigured)
    }
}
