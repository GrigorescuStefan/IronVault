package com.example.ironvault

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import javax.crypto.SecretKey

class ChangePasswordFragment : DialogFragment() {

    private val minNumberOfCharacters = 12
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_change_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val switchOffColor = ContextCompat.getColor(requireContext(), R.color.SwitchOff)
        val switchOnColor = ContextCompat.getColor(requireContext(), R.color.SwitchOn)
        val saveButton: Button = view.findViewById(R.id.savePasswordUpdateButton)
        val closeButton: Button = view.findViewById(R.id.closePasswordUpdateButton)
        saveButton.isEnabled = true
        closeButton.isEnabled = true
        saveButton.setBackgroundColor(switchOnColor)
        closeButton.setBackgroundColor(switchOnColor)
        val textFieldNewPassword: EditText = view.findViewById(R.id.textFieldNewPassword)
        val textFieldReTypeNewPassword: EditText =
            view.findViewById(R.id.textFieldReTypeNewPassword)

        closeButton.setOnClickListener {
            dismiss()
        }

        saveButton.setOnClickListener {
            disableButtons(saveButton, closeButton, switchOffColor)
            val credentialsBundle = arguments?.getBundle("credentials")
            val emailAddress = credentialsBundle?.getString("emailAddress")
            val masterPassword = credentialsBundle?.getString("masterPassword")
            val initVector = credentialsBundle?.getString("iv")
            val newMasterPassword = UtilityFunctions.sha256(textFieldNewPassword.text.toString() )
            val encryptionKey =
                UtilityFunctions.generateAESKeyFromHash(masterPassword!!.toByteArray())
            val newEncryptionKey = UtilityFunctions.generateAESKeyFromHash((newMasterPassword).toByteArray())
            val iv = Base64.decode(initVector, Base64.DEFAULT)

            if (UtilityFunctions.checkEmptyString(textFieldNewPassword.text.toString()) || UtilityFunctions.checkEmptyString(
                    textFieldReTypeNewPassword.text.toString()
                )
            ) {
                UtilityFunctions.showToastMessage(
                    requireContext(),
                    "Password field cannot be left empty!"
                )
                reEnableButtons(saveButton, closeButton, switchOnColor)
                return@setOnClickListener
            }

            if (textFieldNewPassword.text.toString().length < minNumberOfCharacters || textFieldReTypeNewPassword.text.toString().length < minNumberOfCharacters) {
                UtilityFunctions.showToastMessage(
                    requireContext(),
                    "Password cannot be smaller than $minNumberOfCharacters characters!"
                )
                reEnableButtons(saveButton, closeButton, switchOnColor)
                return@setOnClickListener
            }

            if (textFieldNewPassword.text.toString() != textFieldReTypeNewPassword.text.toString()) {
                UtilityFunctions.showToastMessage(requireContext(), "Passwords do not match!")
                reEnableButtons(saveButton, closeButton, switchOnColor)
                return@setOnClickListener
            }

            updateUserMasterPassword(requireContext(), emailAddress!!, newMasterPassword, encryptionKey, newEncryptionKey, iv)
        }
    }

    private fun updateUserMasterPassword(context: Context, emailAddress: String, newMasterPassword: String, encryptionKey: SecretKey, newEncryptionKey: SecretKey, iv: ByteArray) {
        Firebase.firestore.collection("users")
            .whereEqualTo("emailAddress", emailAddress)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    UtilityFunctions.showToastMessage(context, "User not found!")
                    return@addOnSuccessListener
                }

                val userDoc = querySnapshot.documents[0]
                val encryptedPasswordHint = userDoc.getString("passwordHint")
                val decryptedPasswordHint = UtilityFunctions.decryptAES(encryptedPasswordHint!!, encryptionKey, iv)
                val reEncryptedPasswordHint = UtilityFunctions.encryptAES(decryptedPasswordHint, newEncryptionKey, iv)

                userDoc.reference.update(
                    mapOf(
                        "masterPassword" to newMasterPassword,
                        "passwordHint" to reEncryptedPasswordHint
                    )
                ).addOnSuccessListener {
                        UtilityFunctions.showToastMessage(context, "User master password updated successfully!")
                        updateAccountInformation(context, emailAddress, encryptionKey, newEncryptionKey, iv)
                    }
                    .addOnFailureListener { e ->
                        UtilityFunctions.showToastMessage(context, "Failed to update user master password: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                UtilityFunctions.showToastMessage(context, "Failed to find user: ${e.message}")
            }
    }


    private fun updateAccountInformation(context: Context, emailAddress: String, encryptionKey: SecretKey, newEncryptionKey: SecretKey, iv: ByteArray) {
        val accountsRef = Firebase.firestore.collection("accounts")
        accountsRef.whereEqualTo("emailAddress", emailAddress).get()
            .addOnSuccessListener { accountDocuments ->
                if (accountDocuments.isEmpty) {
                    UtilityFunctions.showToastMessage(context, "No accounts found for this user!")
                    return@addOnSuccessListener
                }

                for (accountDocument in accountDocuments) {
                    val encryptedURL = accountDocument.getString("url") ?: continue
                    val encryptedUsername = accountDocument.getString("username") ?: continue
                    val encryptedPassword = accountDocument.getString("password") ?: continue

                    val decryptedURL = UtilityFunctions.decryptAES(encryptedURL, encryptionKey, iv)
                    val newEncryptedURL = UtilityFunctions.encryptAES(decryptedURL, newEncryptionKey, iv)

                    val decryptedUsername = UtilityFunctions.decryptAES(encryptedUsername, encryptionKey, iv)
                    val newEncryptedUsername = UtilityFunctions.encryptAES(decryptedUsername, newEncryptionKey, iv)

                    val decryptedPassword = UtilityFunctions.decryptAES(encryptedPassword, encryptionKey, iv)
                    val newEncryptedPassword = UtilityFunctions.encryptAES(decryptedPassword, newEncryptionKey, iv)

                    accountDocument.reference.update(
                        mapOf(
                            "url" to newEncryptedURL,
                            "username" to newEncryptedUsername,
                            "password" to newEncryptedPassword
                        )
                    ).addOnSuccessListener {
                        UtilityFunctions.showToastMessage(context, "Account information updated successfully!")
                        dismiss()
                        logOutUser(context)
                    }.addOnFailureListener { e ->
                        UtilityFunctions.showToastMessage(context, "Failed to update account information: ${e.message}")
                    }
                }
            }.addOnFailureListener { e ->
                UtilityFunctions.showToastMessage(context, "Failed to fetch accounts: ${e.message}")
            }
    }

    private fun disableButtons(saveButton: Button, closeButton: Button, color: Int) {
        saveButton.isEnabled = false
        closeButton.isEnabled = false
        saveButton.setBackgroundColor(color)
        closeButton.setBackgroundColor(color)
    }

    private fun reEnableButtons(saveButton: Button, closeButton: Button, color: Int) {
        saveButton.isEnabled = true
        closeButton.isEnabled = true
        saveButton.setBackgroundColor(color)
        closeButton.setBackgroundColor(color)
    }

    private fun logOutUser(context: Context) {
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
        if(context is Activity) {
            context.finish()
        }
    }
}