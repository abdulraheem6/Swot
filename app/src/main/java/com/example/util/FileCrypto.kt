package com.example.util

import android.content.Context
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object FileCrypto {
    private const val HEADER_MAGIC = "MYVAULT_ENC_V1\n"
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BIT = 128
    private const val IV_LENGTH_BYTE = 12
    private const val SALT_LENGTH_BYTE = 16
    private const val ITERATIONS = 10000
    private const val KEY_LENGTH_BIT = 256
    private const val DEFAULT_SECRET_PASSPHRASE = "MyVault_ZeroKnowledge_MasterEnclaveKey_2026"

    private fun deriveKey(passphrase: String, salt: ByteArray): SecretKeySpec {
        val effectivePass = passphrase.ifBlank { DEFAULT_SECRET_PASSPHRASE }
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(effectivePass.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BIT)
        val secret = factory.generateSecret(spec)
        return SecretKeySpec(secret.encoded, "AES")
    }

    /**
     * Checks if the given file starts with the MyVault AES-GCM encryption header.
     */
    fun isEncrypted(file: File): Boolean {
        if (!file.exists() || file.length() < HEADER_MAGIC.length) return false
        return try {
            FileInputStream(file).use { input ->
                val magicBytes = ByteArray(HEADER_MAGIC.length)
                val read = input.read(magicBytes)
                read == HEADER_MAGIC.length && String(magicBytes, Charsets.UTF_8) == HEADER_MAGIC
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Encrypts input stream bytes into target file with AES-256-GCM.
     */
    fun encryptStreamToFile(
        input: InputStream,
        targetFile: File,
        passphrase: String = ""
    ): Boolean {
        return try {
            val salt = ByteArray(SALT_LENGTH_BYTE).apply { SecureRandom().nextBytes(this) }
            val iv = ByteArray(IV_LENGTH_BYTE).apply { SecureRandom().nextBytes(this) }
            val secretKey = deriveKey(passphrase, salt)

            val cipher = Cipher.getInstance(ALGORITHM)
            val parameterSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)

            val plainBytes = input.readBytes()
            val cipherBytes = cipher.doFinal(plainBytes)

            FileOutputStream(targetFile).use { output ->
                output.write(HEADER_MAGIC.toByteArray(Charsets.UTF_8))
                output.write(salt)
                output.write(iv)
                output.write(cipherBytes)
                output.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun decryptFileToBytes(
        encryptedFile: File,
        passphrase: String = ""
    ): ByteArray? {
        if (!encryptedFile.exists()) return null
        if (!isEncrypted(encryptedFile)) {
            return try {
                encryptedFile.readBytes()
            } catch (e: Exception) {
                null
            }
        }

        fun doDecrypt(keyPass: String): ByteArray {
            return FileInputStream(encryptedFile).use { input ->
                // Skip header
                val headerBytes = ByteArray(HEADER_MAGIC.length)
                input.read(headerBytes)

                val salt = ByteArray(SALT_LENGTH_BYTE)
                input.read(salt)

                val iv = ByteArray(IV_LENGTH_BYTE)
                input.read(iv)

                val cipherBytes = input.readBytes()

                val secretKey = deriveKey(keyPass, salt)
                val cipher = Cipher.getInstance(ALGORITHM)
                val parameterSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)

                cipher.doFinal(cipherBytes)
            }
        }

        return try {
            doDecrypt(passphrase)
        } catch (e: Exception) {
            if (passphrase.isNotBlank() && passphrase != DEFAULT_SECRET_PASSPHRASE) {
                try {
                    doDecrypt(DEFAULT_SECRET_PASSPHRASE)
                } catch (_: Exception) {
                    null
                }
            } else {
                null
            }
        }
    }

    /**
     * Decrypts encrypted file into a temporary cache file (for viewing or sharing).
     */
    fun getDecryptedCacheFile(
        context: Context,
        encryptedFile: File,
        fileName: String,
        passphrase: String = ""
    ): File? {
        return try {
            val decryptedBytes = decryptFileToBytes(encryptedFile, passphrase) ?: return null
            val cacheFolder = File(context.cacheDir, "decrypted").apply { if (!exists()) mkdirs() }
            val cleanName = fileName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            val targetFile = File(cacheFolder, "dec_$cleanName")
            targetFile.writeBytes(decryptedBytes)
            targetFile.deleteOnExit()
            targetFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
