package com.example.ironvault

import android.content.Context
import android.os.Bundle
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import android.util.Base64
import android.util.Patterns
import android.widget.Toast
import java.security.MessageDigest
import javax.crypto.spec.SecretKeySpec

object UtilityFunctions {

    fun generateAESKeyFromHash(hashedKey: ByteArray): SecretKey { // function created to adjust AES Key size to fit for the algorithm
        val trimmedKey = hashedKey.copyOf(32)
        return SecretKeySpec(trimmedKey, "AES")
    }

    fun encryptAES(dataToEncrypt: String, secretKey: SecretKey, iv: ByteArray): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
        val encryptedBytes = cipher.doFinal(dataToEncrypt.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    fun decryptAES(dataToDecrypt: String, secretKey: SecretKey, iv: ByteArray): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
        val encryptedBytes = Base64.decode(dataToDecrypt, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes)
    }

    fun checkEmptyString(content: String): Boolean {
        return content.isEmpty()
    }

    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun showToastMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun convertBundleToMap(bundle: Bundle): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for (key in bundle.keySet()) {
            map[key] = bundle.getString(key) ?: ""
        }
        return map
    }

    fun convertMapToBundle(data: MutableMap<String, Any>): Bundle {
        val bundle = Bundle()
        for ((key, value) in data) {
            bundle.putString(key, value.toString())
        }
        return bundle
    }

    fun sha1(input: String): String {
        val digest = MessageDigest.getInstance("SHA-1")
        val result = digest.digest(input.toByteArray())
        val hexString = StringBuilder()

        for (byte in result) {
            val hex = Integer.toHexString(0xFF and byte.toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }

        return hexString.toString().uppercase()
    }

    fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val result = digest.digest(input.toByteArray())
        val hexString = StringBuilder()

        for (byte in result) {
            val hex = Integer.toHexString(0xFF and byte.toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }
        return hexString.toString()
    }
}