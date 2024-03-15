package com.example.ironvault

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import javax.crypto.spec.SecretKeySpec

class AuthenticationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.authentication_layout)
        lateinit var secretKey: SecretKeySpec
        val passwordHint: TextView = findViewById(R.id.passwordHint)
        val passwordField: TextView = findViewById(R.id.editTextMasterPassword)
        val (privateKey, iv) = UtilityFunctions.readEncryptionDataFromFile(applicationContext)
        val logInButton: Button = findViewById(R.id.LogIn)
        val bundledData = intent.getBundleExtra("loginData")
        val credentialsMap = bundledData?.let { UtilityFunctions.convertBundleToMap(it) }

        if (privateKey.isNotEmpty()) {
            secretKey = SecretKeySpec(privateKey.toByteArray(), "AES")
        }
        val passwordFromDB = credentialsMap!!.getValue("masterPassword")
        val decryptedPasswordHint =
            UtilityFunctions.decryptAES(credentialsMap.getValue("passwordHint"), secretKey, iv)
        passwordHint.text = decryptedPasswordHint

        val switchOffColor = ContextCompat.getColor(this, R.color.SwitchOff)
        val switchOnColor = ContextCompat.getColor(this, R.color.SwitchOn)
        logInButton.setOnClickListener {
            logInButton.isEnabled = false
            logInButton.setBackgroundColor(switchOffColor)
            if(UtilityFunctions.checkEmptyString(passwordField.text.toString())){
                UtilityFunctions.showToastMessage(this, "Password field cannot be left empty!")
                logInButton.isEnabled = true
                logInButton.setBackgroundColor(switchOnColor)
                return@setOnClickListener
            }
            if(UtilityFunctions.encryptAES(passwordField.text.toString(), secretKey, iv) == passwordFromDB) {
//                UtilityFunctions.showToastMessage(this, "Password matches, you finished the login!")
                val sendCredentials = Intent(this, FragmentsActivity::class.java)
                sendCredentials.putExtra("credentials", bundledData)
                startActivity(sendCredentials)
//                logInButton.isEnabled = true
//                logInButton.setBackgroundColor(switchOnColor)
//                return@setOnClickListener
            } else {
                UtilityFunctions.showToastMessage(this, "Password is incorrect!")
                logInButton.isEnabled = true
                logInButton.setBackgroundColor(switchOnColor)
                return@setOnClickListener
            }
        }
    }

}