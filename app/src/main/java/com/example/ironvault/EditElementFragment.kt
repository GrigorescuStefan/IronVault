package com.example.ironvault

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.DialogFragment
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore

class EditElementFragment : DialogFragment() {

    private var accountEditedListener: OnAccountEditedListener? = null

    fun setOnAccountEditedListener(listener: OnAccountEditedListener) {
        accountEditedListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_element, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = Firebase.firestore
        val accounts = db.collection("accounts")
        val users = db.collection("users")
        val textFieldURL: EditText = view.findViewById(R.id.textFieldURL)
        val textFieldUsername: EditText = view.findViewById(R.id.textFieldUsername)
        val textFieldPassword: EditText = view.findViewById(R.id.textFieldPassword)
        val closeButton: ImageButton = view.findViewById(R.id.closeButtonEditDialog)
        val saveButton: Button = view.findViewById(R.id.saveButtonEditDialog)
        val deleteButton: Button = view.findViewById(R.id.deleteButtonEditDialog)

        val bundleEmailAddress: String = arguments?.getString("emailAddress").toString()
        val bundleURL: String = arguments?.getString("url").toString()
        val bundleUsername: String = arguments?.getString("username").toString()
        val bundlePassword: String = arguments?.getString("password").toString()

        textFieldURL.setText(bundleURL)
        textFieldUsername.setText(bundleUsername)
        textFieldPassword.setText(bundlePassword)

        closeButton.setOnClickListener {
            dismiss()
        }

        deleteButton.setOnClickListener {
            deleteUserFromDatabase(
                users,
                accounts,
                bundleEmailAddress,
                textFieldURL,
                textFieldUsername,
                textFieldPassword
            )
        }

        saveButton.setOnClickListener {
            if (bundleURL == textFieldURL.text.toString() && bundleUsername == textFieldUsername.text.toString() && bundlePassword == textFieldPassword.text.toString()) {
                UtilityFunctions.showToastMessage(
                    requireContext(),
                    "You haven't modified any field, if you wish to close please press X"
                )
            } else {
                updateUserInDatabase(
                    users,
                    accounts,
                    bundleEmailAddress,
                    bundleURL,
                    textFieldURL,
                    textFieldUsername,
                    textFieldPassword
                )
            }
        }
    }

    interface OnAccountEditedListener {
        fun onAccountEdited()
    }

    private fun updateUserInDatabase(
        users: CollectionReference,
        accounts: CollectionReference,
        bundleEmailAddress: String,
        bundleURL: String,
        textFieldURL: EditText,
        textFieldUsername: EditText,
        textFieldPassword: EditText
    ) {
        if (UtilityFunctions.checkEmptyString(textFieldURL.text.toString())) {
            UtilityFunctions.showToastMessage(requireContext(), "URL cannot be empty!")
            return
        }
        if (UtilityFunctions.checkEmptyString(textFieldUsername.text.toString())) {
            UtilityFunctions.showToastMessage(requireContext(), "Username cannot be empty!")
            return
        }
        if (UtilityFunctions.checkEmptyString(textFieldPassword.text.toString())) {
            UtilityFunctions.showToastMessage(requireContext(), "Password cannot be empty!")
            return
        }

        users.whereEqualTo("emailAddress", bundleEmailAddress).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val userDocument = querySnapshot.documents[0]
                    val masterPassword = userDocument.getString("masterPassword").toString()
                    val initVector = userDocument.getString("iv").toString()
                    val encryptionKey =
                        UtilityFunctions.generateAESKeyFromHash(masterPassword.toByteArray())
                    val iv = Base64.decode(initVector, Base64.DEFAULT)
                    val updatedAccount = hashMapOf<String, Any>(
                        "emailAddress" to bundleEmailAddress,
                        "url" to UtilityFunctions.encryptAES(
                            textFieldURL.text.toString(),
                            encryptionKey,
                            iv
                        ),
                        "username" to UtilityFunctions.encryptAES(
                            textFieldUsername.text.toString(),
                            encryptionKey,
                            iv
                        ),
                        "password" to UtilityFunctions.encryptAES(
                            textFieldPassword.text.toString(),
                            encryptionKey,
                            iv
                        )
                    )
                    val encryptedURL = UtilityFunctions.encryptAES(bundleURL, encryptionKey, iv)
                    accounts.whereEqualTo("emailAddress", bundleEmailAddress)
                        .whereEqualTo("url", encryptedURL).get()
                        .addOnSuccessListener { queryAccountSnapshot ->
                            if (!queryAccountSnapshot.isEmpty) {
                                for (document in queryAccountSnapshot.documents) {
                                    document.reference.update(updatedAccount).addOnSuccessListener {
                                        UtilityFunctions.showToastMessage(
                                            requireContext(),
                                            "Account data has been successfully updated!"
                                        )
                                        accountEditedListener?.onAccountEdited()
                                        dismiss()
                                    }.addOnFailureListener {
                                        UtilityFunctions.showToastMessage(
                                            requireContext(),
                                            "Account data could not be updated!"
                                        )
                                        accountEditedListener?.onAccountEdited()
                                        dismiss()
                                    }
                                }
                            }
                        }.addOnFailureListener {
                            UtilityFunctions.showToastMessage(
                                requireContext(),
                                "Failed to retrieve account data!"
                            )
                        }
                } else {
                    UtilityFunctions.showToastMessage(requireContext(), "User not found!")
                }
            }.addOnFailureListener {
                UtilityFunctions.showToastMessage(requireContext(), "Failed to retrieve user data!")
            }
    }

    private fun deleteUserFromDatabase(
        users: CollectionReference,
        accounts: CollectionReference,
        bundleEmailAddress: String,
        textFieldURL: EditText,
        textFieldUsername: EditText,
        textFieldPassword: EditText
    ) {
        users.whereEqualTo("emailAddress", bundleEmailAddress).get()
            .addOnSuccessListener { queryDeleteSnapshot ->
                if (!queryDeleteSnapshot.isEmpty) {
                    val userDocument = queryDeleteSnapshot.documents[0]
                    val masterPassword = userDocument.getString("masterPassword").toString()
                    val initVector = userDocument.getString("iv").toString()
                    val encryptionKey =
                        UtilityFunctions.generateAESKeyFromHash(masterPassword.toByteArray())
                    val iv = Base64.decode(initVector, Base64.DEFAULT)
                    val encryptedURL =
                        UtilityFunctions.encryptAES(textFieldURL.text.toString(), encryptionKey, iv)
                    val encryptedUsername = UtilityFunctions.encryptAES(
                        textFieldUsername.text.toString(),
                        encryptionKey,
                        iv
                    )
                    val encryptedPassword = UtilityFunctions.encryptAES(
                        textFieldPassword.text.toString(),
                        encryptionKey,
                        iv
                    )
                    Log.d("emailAddress", bundleEmailAddress)
                    Log.d("URL", encryptedURL)
                    Log.d("Username", encryptedUsername)
                    Log.d("Password", encryptedPassword)
                    accounts.whereEqualTo("emailAddress", bundleEmailAddress)
                        .whereEqualTo("url", encryptedURL)
                        .whereEqualTo("username", encryptedUsername)
                        .whereEqualTo("password", encryptedPassword).get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                for (document in querySnapshot.documents) {
                                    document.reference.delete().addOnSuccessListener {
                                        accountEditedListener?.onAccountEdited()
                                        UtilityFunctions.showToastMessage(
                                            requireContext(),
                                            "Account data has been successfully deleted!"
                                        )
                                        dismiss()
                                    }.addOnFailureListener {
                                        UtilityFunctions.showToastMessage(
                                            requireContext(),
                                            "Account data could not be deleted!"
                                        )
                                        dismiss()
                                    }
                                }
                            } else {
                                UtilityFunctions.showToastMessage(
                                    requireContext(),
                                    "Account not found!"
                                )
                            }
                        }.addOnFailureListener {
                            UtilityFunctions.showToastMessage(
                                requireContext(),
                                "Failed to retrieve account data!"
                            )
                        }
                }
            }
    }
}
