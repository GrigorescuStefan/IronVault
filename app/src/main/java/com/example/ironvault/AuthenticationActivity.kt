package com.example.ironvault

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat

class AuthenticationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.authentication_layout)
        val passwordHint: TextView = findViewById(R.id.passwordHint)
        val passwordField: TextView = findViewById(R.id.editTextMasterPassword)
        val logInButton: Button = findViewById(R.id.LogIn)
        val bundledData = intent.getBundleExtra("loginData")
        val credentialsMap = bundledData?.let { UtilityFunctions.convertBundleToMap(it) }
        val passwordFromDB = credentialsMap!!.getValue("masterPassword")
        val initVector = credentialsMap.getValue("iv")
        val decryptionKey = UtilityFunctions.generateAESKeyFromHash(passwordFromDB.toByteArray())
        val iv = Base64.decode(initVector, Base64.DEFAULT)
        val decryptedPasswordHint =
            UtilityFunctions.decryptAES(credentialsMap.getValue("passwordHint"), decryptionKey, iv)
        passwordHint.text = decryptedPasswordHint

        val switchOffColor = ContextCompat.getColor(this, R.color.SwitchOff)
        val switchOnColor = ContextCompat.getColor(this, R.color.SwitchOn)
        logInButton.setOnClickListener {
            logInButton.isEnabled = false
            logInButton.setBackgroundColor(switchOffColor)
            if (UtilityFunctions.checkEmptyString(passwordField.text.toString())) {
                UtilityFunctions.showToastMessage(this, "Password field cannot be left empty!")
                logInButton.isEnabled = true
                logInButton.setBackgroundColor(switchOnColor)
                return@setOnClickListener
            }
            if (UtilityFunctions.sha256(passwordField.text.toString()) == passwordFromDB) {
                val sendCredentials = Intent(this, FragmentsActivity::class.java)
                sendCredentials.putExtra("credentials", bundledData)
                startActivity(sendCredentials)
            } else {
                UtilityFunctions.showToastMessage(this, "Password is incorrect!")
                logInButton.isEnabled = true
                logInButton.setBackgroundColor(switchOnColor)
                return@setOnClickListener
            }
        }
    }

}