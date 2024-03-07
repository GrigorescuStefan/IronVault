package com.example.ironvault

import android.content.Context
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import android.util.Base64
import android.util.Patterns
import android.widget.Toast
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader

object UtilityFunctions {
     fun encryptAES(dataToEncrypt: String, secretKey: SecretKey, iv: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ivParameterSpec = IvParameterSpec(iv.toByteArray())
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
        val encryptedBytes = cipher.doFinal(dataToEncrypt.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    fun decryptAES(dataToDecrypt: String, secretKey: SecretKey, iv: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ivParameterSpec = IvParameterSpec(iv.toByteArray())
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
        val encryptedBytes = Base64.decode(dataToDecrypt, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes)
    }

    fun readEncryptionDataFromFile(context: Context): Pair<String, String> {
        var privateKey = ""
        var iv = ""

        try {
            val filename = "data.txt"
            val fileInputStream: FileInputStream = context.openFileInput(filename)
            val inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)

            val line = bufferedReader.readLine()
            val parts = line.split(",")
            privateKey = parts[0]
            iv = parts[1]
            bufferedReader.close()
            inputStreamReader.close()
            fileInputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Pair(privateKey, iv)
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
}