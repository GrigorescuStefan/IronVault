package com.example.ironvault

import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.util.Patterns
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
    suspend fun verifyEmail(apiKey: String, email: String, context: Context): Pair<Boolean, Boolean>? {
        return withContext(Dispatchers.IO) {
            val jsonBody = JSONObject().apply {
                put("api_key", apiKey)
                put("email_address", email)
            }

            val url = "https://verify.maileroo.net/check"
            val queue: RequestQueue = Volley.newRequestQueue(context)

            return@withContext suspendCoroutine { continuation ->
                val jsonObjectRequest = object : JsonObjectRequest(
                    Method.POST, url, jsonBody,
                    Response.Listener { response ->
                        try {
                            val success = response.getBoolean("success")
                            if (success) {
                                val data = response.getJSONObject("data")
                                val mxFound = data.getBoolean("mx_found")
                                val disposable = data.getBoolean("disposable")
                                continuation.resume(Pair(mxFound, disposable))
                            } else {
                                continuation.resume(null)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            continuation.resume(null)
                        }
                    },
                    Response.ErrorListener { error ->
                        error.printStackTrace()
                        continuation.resume(null)
                    }
                ) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Authorization"] = "Bearer $apiKey"
                        headers["Content-Type"] = "application/json"
                        return headers
                    }
                }
                queue.add(jsonObjectRequest)
            }
        }
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