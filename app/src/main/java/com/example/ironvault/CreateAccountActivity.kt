package com.example.ironvault

import android.content.Intent
import android.os.Bundle
import android.util.Base64
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
import java.security.SecureRandom
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CreateAccountActivity : ComponentActivity() {

    private val minNumberOfCharacters = 12

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_account_layout)

        val emailAddressTextView: TextView = findViewById(R.id.emailAddress)
        val masterPasswordTextView: TextView = findViewById(R.id.masterPassword)
        val reTypeMasterPasswordTextView: TextView = findViewById(R.id.reTypeMasterPassword)
        val passwordHintTextView: TextView = findViewById(R.id.MasterPasswordHint)
        val switchCompatCheck: SwitchCompat = findViewById(R.id.switchCompat)
        val continueButton: Button = findViewById(R.id.createAccountButton)

        addUser(
            continueButton,
            emailAddressTextView,
            masterPasswordTextView,
            reTypeMasterPasswordTextView,
            passwordHintTextView,
            switchCompatCheck
        )
    }

    private fun addUser(
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
            "iv" to ""
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
                        val hashedKey = UtilityFunctions.sha256(masterPass.text.toString())
                        val hashedEmail =
                            UtilityFunctions.sha256(emailAddressTextView.text.toString())
                        val secretKeyAfterHash =
                            UtilityFunctions.generateAESKeyFromHash(hashedKey.toByteArray())
                        val initialVector = ByteArray(16)
                        SecureRandom().nextBytes(initialVector)
                        val initVectorString = Base64.encodeToString(initialVector, Base64.DEFAULT)
                        newUser.replace(
                            "emailAddress",
                            hashedEmail
                        )
                        newUser.replace(
                            "masterPassword",
                            hashedKey
                        )
                        newUser.replace(
                            "passwordHint",
                            UtilityFunctions.encryptAES(
                                passHint.text.toString(),
                                secretKeyAfterHash,
                                initialVector
                            )
                        )
                        newUser.replace(
                            "iv",
                            initVectorString
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

    private suspend fun passwordBreachCheckAsync(password: String): Int {
        return suspendCoroutine { continuation ->
            val shaPassword = UtilityFunctions.sha1(password)
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
}