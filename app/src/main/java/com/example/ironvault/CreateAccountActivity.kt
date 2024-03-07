package com.example.ironvault

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request.Method
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.security.MessageDigest
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CreateAccountActivity : ComponentActivity() {

    private val minNumberOfCharacters = 8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_account_layout)
        val (privateKey, iv) = readEncryptionDataFromFile(applicationContext)

        if (privateKey.isNotEmpty()) {
            val secretKey = SecretKeySpec(privateKey.toByteArray(), "AES")
            val emailAddressTextView: TextView = findViewById(R.id.emailAddress)
            val masterPasswordTextView: TextView = findViewById(R.id.masterPassword)
            val reTypeMasterPasswordTextView: TextView = findViewById(R.id.reTypeMasterPassword)
            val passwordHintTextView: TextView = findViewById(R.id.MasterPasswordHint)
            val switchCompatCheck: SwitchCompat = findViewById(R.id.switchCompat)
            val continueButton: Button = findViewById(R.id.button)

            addUser(
                secretKey,
                iv,
                continueButton,
                emailAddressTextView,
                masterPasswordTextView,
                reTypeMasterPasswordTextView,
                passwordHintTextView,
                switchCompatCheck
            )
        } else {
            Log.e("PrivateKeyMissing: ", "Private Key is Empty!")
        }
    }

    private fun addUser(
        secretKey: SecretKey,
        initVector: String,
        button: Button,
        emailAddressTextView: TextView,
        masterPass: TextView,
        reTypeMasterPass: TextView,
        passHint: TextView,
        switchCompatCheck: SwitchCompat
    ) {
        var foundEmail = false
        val switchOffColor = ContextCompat.getColor(this, R.color.SwitchOff)
        val switchOnColor = ContextCompat.getColor(this, R.color.SwitchOn)
        val db = Firebase.firestore
        val users = db.collection("users")
        val newUser = hashMapOf(
            "emailAddress" to "",
            "masterPassword" to "",
            "passwordHint" to "",
        )

        button.setOnClickListener {
            lifecycleScope.launch {
                button.isEnabled = false
                button.setBackgroundColor(switchOffColor)
                if (UtilityFunctions.checkEmptyString(emailAddressTextView.text.toString())) {
                    UtilityFunctions.showToastMessage(
                        this@CreateAccountActivity,
                        "Email field cannot be left empty!"
                    )
                    button.isEnabled = true
                    button.setBackgroundColor(switchOnColor)
                    return@launch
                }
                if (!UtilityFunctions.isValidEmail(emailAddressTextView.text.toString())) {
                    UtilityFunctions.showToastMessage(
                        this@CreateAccountActivity,
                        "Invalid email address!"
                    )
                    emailAddressTextView.text = ""
                    button.isEnabled = true
                    button.setBackgroundColor(switchOnColor)
                    return@launch
                }
                if (UtilityFunctions.checkEmptyString(masterPass.text.toString()) || UtilityFunctions.checkEmptyString(
                        reTypeMasterPass.text.toString()
                    )
                ) {
                    UtilityFunctions.showToastMessage(
                        this@CreateAccountActivity,
                        "Password fields cannot be left empty!"
                    )
                    button.isEnabled = true
                    button.setBackgroundColor(switchOnColor)
                    return@launch
                }
                if (masterPass.text.toString().length < minNumberOfCharacters || reTypeMasterPass.text.toString().length < minNumberOfCharacters) {
                    UtilityFunctions.showToastMessage(
                        this@CreateAccountActivity,
                        "Passwords cannot be smaller than 8 characters!"
                    )
                    button.isEnabled = true
                    button.setBackgroundColor(switchOnColor)
                    return@launch
                }
                if (!matchingPasswordContent(
                        masterPass.text.toString(),
                        reTypeMasterPass.text.toString()
                    )
                ) {
                    UtilityFunctions.showToastMessage(
                        this@CreateAccountActivity,
                        "Passwords do not match!"
                    )
                    masterPass.text = ""
                    reTypeMasterPass.text = ""
                    button.isEnabled = true
                    button.setBackgroundColor(switchOnColor)
                    return@launch
                }

                if (switchCompatCheck.isChecked) {
                    val occurrences = passwordBreachCheckAsync(masterPass.text.toString())
                    if (occurrences > 0) {
                        button.isEnabled = true
                        button.setBackgroundColor(switchOnColor)
                        UtilityFunctions.showToastMessage(
                            this@CreateAccountActivity,
                            "Your password was seen $occurrences times in previous data breaches"
                        )
                        return@launch
                    }
                }
                existingEmailInDB(emailAddressTextView.text.toString(), db) { doesEmailExists ->
                    if (doesEmailExists) {
                        foundEmail = true
                        UtilityFunctions.showToastMessage(
                            this@CreateAccountActivity,
                            "Email is already in use!"
                        )
                        emailAddressTextView.text = ""
                    } else {
                        newUser.replace(
                            "emailAddress",
                            UtilityFunctions.encryptAES(
                                emailAddressTextView.text.toString(),
                                secretKey,
                                initVector
                            )
                        )
                        newUser.replace(
                            "masterPassword",
                            UtilityFunctions.encryptAES(
                                masterPass.text.toString(),
                                secretKey,
                                initVector
                            )
                        )
                        newUser.replace(
                            "passwordHint",
                            UtilityFunctions.encryptAES(
                                passHint.text.toString(),
                                secretKey,
                                initVector
                            )
                        )
                        addUserToDatabase(users, newUser)
                    }
                }
                if (foundEmail) {
                    return@launch
                }
            }
        }

    }

    private fun matchingPasswordContent(password: String, reTypePassword: String): Boolean {
        return password == reTypePassword
    }

    private fun existingEmailInDB(
        email: String,
        db: FirebaseFirestore,
        callback: (Boolean) -> Unit
    ) {
        db.collection("users").whereEqualTo("emailAddress", email).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val emailExists = task.result != null && !task.result.isEmpty
                    callback(emailExists)
                }
            }
    }

    private fun addUserToDatabase(users: CollectionReference, newUser: HashMap<String, String>) {
        if (!UtilityFunctions.checkEmptyString(newUser.getValue("emailAddress"))) {
            users.add(newUser).addOnSuccessListener {
                Toast.makeText(this, "User was registered successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
                .addOnFailureListener {
                    UtilityFunctions.showToastMessage(
                        this,
                        "User could not registered!"
                    )
                }
        }
    }

    private fun sha1(input: String): String {
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

    private suspend fun passwordBreachCheckAsync(password: String): Int {
        return suspendCoroutine { continuation ->
            val shaPassword = sha1(password)
            val firstFiveHexCharacters = shaPassword.substring(0, 5).uppercase()
            val apiURL = "https://api.pwnedpasswords.com/range/$firstFiveHexCharacters"
            val queue = Volley.newRequestQueue(this)

            val stringRequest = StringRequest(Method.GET, apiURL,
                { response ->
                    val map = parseResponseString(response)
                    val suffix = shaPassword.substring(5).uppercase()
                    val occurrences = map[suffix] ?: 0
                    continuation.resume(occurrences)
                },
                { error ->
                    Log.d("---Error---:", error.toString())
                    continuation.resume(0) // or handle the error accordingly
                })
            queue.add(stringRequest)
        }
    }

    private fun parseResponseString(response: String): Map<String, Int> {
        val map = mutableMapOf<String, Int>()

        val lines = response.lines()
        for (line in lines) {
            val parts = line.trim().split(":")
            if (parts.size == 2) {
                val key = parts[0].trim()
                val value = parts[1].trim().toIntOrNull()

                if (key.isNotEmpty() && value != null) {
                    map[key] = value
                }
            }
        }

        return map
    }

    private fun readEncryptionDataFromFile(context: Context): Pair<String, String> {
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

    //Used to save the necessary encryption keys to a file in internal storage
    //Looking for a better alternatives
    // FOUND BETTER ALTERNATIVE - TO DO - IMPLEMENT WHAT YOU LEARNED
    private fun saveToFile(context: Context, data: String) {
        try {
            val filename = "data.txt"
            val fileOutputStream: FileOutputStream =
                context.openFileOutput(filename, Context.MODE_PRIVATE)
            fileOutputStream.write(data.toByteArray())
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}