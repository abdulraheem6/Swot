package com.example.util

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.UrgentRed
import com.example.ui.theme.WarningYellow
import java.security.SecureRandom

object PasswordUtils {

    data class StrengthResult(
        val score: Int, // 0 to 4
        val label: String,
        val color: Color
    )

    fun calculateStrength(password: String): StrengthResult {
        if (password.isEmpty()) return StrengthResult(0, "None", Color.Gray)

        var points = 0
        if (password.length >= 8) points++
        if (password.length >= 12) points++
        if (password.length >= 16) points++
        if (password.any { it.isUpperCase() }) points++
        if (password.any { it.isLowerCase() }) points++
        if (password.any { it.isDigit() }) points++
        if (password.any { !it.isLetterOrDigit() }) points++

        val score = when {
            points <= 2 -> 1
            points <= 4 -> 2
            points <= 5 -> 3
            else -> 4
        }

        return when (score) {
            1 -> StrengthResult(1, "Weak", UrgentRed)
            2 -> StrengthResult(2, "Fair", WarningYellow)
            3 -> StrengthResult(3, "Good", SafeGreen)
            4 -> StrengthResult(4, "Strong", SafeGreen)
            else -> StrengthResult(0, "Very weak", UrgentRed)
        }
    }

    fun generatePassword(
        length: Int = 16,
        includeUpper: Boolean = true,
        includeLower: Boolean = true,
        includeNumbers: Boolean = true,
        includeSymbols: Boolean = true
    ): String {
        val upperChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowerChars = "abcdefghijklmnopqrstuvwxyz"
        val numberChars = "0123456789"
        val symbolChars = "!@#$%^&*()_+-=[]{}|;:,.<>?"

        val charPool = StringBuilder().apply {
            if (includeUpper) append(upperChars)
            if (includeLower) append(lowerChars)
            if (includeNumbers) append(numberChars)
            if (includeSymbols) append(symbolChars)
        }.toString()

        if (charPool.isEmpty()) return ""

        val random = SecureRandom()
        val password = StringBuilder(length)

        // Ensure at least one of each requested type is present
        val guaranteed = mutableListOf<Char>()
        if (includeUpper) guaranteed.add(upperChars[random.nextInt(upperChars.length)])
        if (includeLower) guaranteed.add(lowerChars[random.nextInt(lowerChars.length)])
        if (includeNumbers) guaranteed.add(numberChars[random.nextInt(numberChars.length)])
        if (includeSymbols) guaranteed.add(symbolChars[random.nextInt(symbolChars.length)])

        guaranteed.forEach { password.append(it) }

        while (password.length < length) {
            password.append(charPool[random.nextInt(charPool.length)])
        }

        // Shuffle the characters
        val chars = password.toString().toCharArray()
        for (i in chars.indices.reversed()) {
            val j = random.nextInt(i + 1)
            val temp = chars[i]
            chars[i] = chars[j]
            chars[j] = temp
        }

        return String(chars)
    }
}
