package com.example.ironvault

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.ironvault.ui.theme.IronVaultTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import javax.crypto.spec.SecretKeySpec

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainactivity_layout)

        val createAccount: TextView? = findViewById(R.id.CreateAccountText)
        val continueButton: Button = findViewById(R.id.button)
        val emailAddress: TextView = findViewById(R.id.editTextTextEmailAddress)
        val db = Firebase.firestore
        val switchOffColor = ContextCompat.getColor(this, R.color.SwitchOff)
        val switchOnColor = ContextCompat.getColor(this, R.color.SwitchOn)

        createAccount?.setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
        }

        val (privateKey, iv) = UtilityFunctions.readEncryptionDataFromFile(applicationContext)

        if (privateKey.isNotEmpty()) {
            val secretKey = SecretKeySpec(privateKey.toByteArray(), "AES")

            continueButton.setOnClickListener {
                continueButton.isEnabled = false
                continueButton.setBackgroundColor(switchOffColor)
                if (UtilityFunctions.checkEmptyString(emailAddress.text.toString())) {
                    UtilityFunctions.showToastMessage(this@MainActivity, "Email field cannot be left empty!")
                    continueButton.isEnabled = true
                    continueButton.setBackgroundColor(switchOnColor)
                    return@setOnClickListener
                }
                if (!UtilityFunctions.isValidEmail(emailAddress.text.toString())) {
                    UtilityFunctions.showToastMessage(this@MainActivity, "Invalid email address!")
                    emailAddress.text = ""
                    continueButton.isEnabled = true
                    continueButton.setBackgroundColor(switchOnColor)
                    return@setOnClickListener
                }
                db.collection("users").get().addOnSuccessListener { documents ->
                    var emailFound = false
                    for (document in documents) {
                        val emailAddressField = document.getString("emailAddress")
                        if (emailAddressField == UtilityFunctions.encryptAES(
                                emailAddress.text.toString(),
                                secretKey,
                                iv
                            )
                        ) {
                            emailFound = true
                            val bundledData = UtilityFunctions.convertMapToBundle(document.getData())
                            val sendLoginData = Intent(this, AuthenticationActivity::class.java)
                            sendLoginData.putExtra("loginData", bundledData)
                            startActivity(sendLoginData)
                            break
                        }
                    }
                    if (!emailFound) {
                        UtilityFunctions.showToastMessage(this@MainActivity, "Email address not found!")
                        continueButton.isEnabled = true
                        continueButton.setBackgroundColor(switchOnColor)
                    }
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    IronVaultTheme {
    }
}

