package com.example.ironvault

import android.app.Dialog
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore

class AddElementFragment : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_element, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val switchOffColor = ContextCompat.getColor(requireContext(), R.color.SwitchOff)
        val switchOnColor = ContextCompat.getColor(requireContext(), R.color.SwitchOn)
        val closeButton: Button = view.findViewById(R.id.closeButton)
        val saveButton: Button = view.findViewById(R.id.saveButton)
        saveButton.isEnabled = true
        closeButton.isEnabled = true

        val emailAddress: String = arguments?.getString("emailAddress").toString()
        val masterPassword: String = arguments?.getString("masterPassword").toString()
        val initVector: String = arguments?.getString("iv").toString()
        val encryptionKey = UtilityFunctions.generateAESKeyFromHash(masterPassword.toByteArray())
        val iv = Base64.decode(initVector, Base64.DEFAULT)
        val textFieldURL: EditText = view.findViewById(R.id.textFieldURL)
        val textFieldUsername: EditText = view.findViewById(R.id.textFieldUsername)
        val textFieldPassword: EditText = view.findViewById(R.id.textFieldPassword)

        val db = Firebase.firestore
        val accounts = db.collection("accounts")
        val newAccount = hashMapOf(
            "emailAddress" to emailAddress,
            "url" to "",
            "username" to "",
            "password" to ""
        )

        closeButton.setOnClickListener {
            dismiss()
        }

        saveButton.setOnClickListener {
            saveButton.isEnabled = false
            closeButton.isEnabled = false
            saveButton.setBackgroundColor(switchOffColor)
            closeButton.setBackgroundColor(switchOffColor)
            if (UtilityFunctions.checkEmptyString(textFieldURL.text.toString())) {
                UtilityFunctions.showToastMessage(requireContext(), "URL cannot be left empty!")
                saveButton.isEnabled = true
                saveButton.setBackgroundColor(switchOnColor)
                closeButton.isEnabled = true
                closeButton.setBackgroundColor(switchOnColor)
                return@setOnClickListener
            }
            if (UtilityFunctions.checkEmptyString(textFieldUsername.text.toString())) {
                UtilityFunctions.showToastMessage(
                    requireContext(),
                    "Username cannot be left empty!"
                )
                saveButton.isEnabled = true
                saveButton.setBackgroundColor(switchOnColor)
                closeButton.isEnabled = true
                closeButton.setBackgroundColor(switchOnColor)
                return@setOnClickListener
            }
            if (UtilityFunctions.checkEmptyString(textFieldPassword.text.toString())) {
                UtilityFunctions.showToastMessage(
                    requireContext(),
                    "Password cannot be left empty!"
                )
                saveButton.isEnabled = true
                saveButton.setBackgroundColor(switchOnColor)
                closeButton.isEnabled = true
                closeButton.setBackgroundColor(switchOnColor)
                return@setOnClickListener
            }
            db.collection("accounts").get().addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    UtilityFunctions.showToastMessage(
                        requireContext(),
                        "There is an internal error. Please contact support."
                    )
                    return@addOnSuccessListener
                }
                for (document in documents) {
                    val urlAddressField = document.getString("url")
                    if (urlAddressField == textFieldURL.text.toString() && emailAddress == document.getString(
                            "emailAddress"
                        )
                    ) {
                        UtilityFunctions.showToastMessage(
                            requireContext(),
                            "You already have an account for this website!"
                        )
                        saveButton.isEnabled = true
                        saveButton.setBackgroundColor(switchOnColor)
                        closeButton.isEnabled = true
                        closeButton.setBackgroundColor(switchOnColor)
                        break
                    } else {
                        newAccount.replace("url", UtilityFunctions.encryptAES(textFieldURL.text.toString(), encryptionKey, iv))
                        newAccount.replace("username", UtilityFunctions.encryptAES(textFieldUsername.text.toString(), encryptionKey, iv))
                        newAccount.replace("password", UtilityFunctions.encryptAES(textFieldPassword.text.toString(), encryptionKey, iv))
                        addAccountToDatabase(accounts, newAccount, switchOnColor)
                    }
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    private fun addAccountToDatabase(
        accounts: CollectionReference,
        newAccount: HashMap<String, String>,
        switchOnColor: Int
    ) {
        accounts.add(newAccount).addOnSuccessListener {
            UtilityFunctions.showToastMessage(
                requireContext(),
                "Account was registered successfully!"
            )
            view?.findViewById<EditText>(R.id.textFieldURL)?.setText("")
            view?.findViewById<EditText>(R.id.textFieldUsername)?.setText("")
            view?.findViewById<EditText>(R.id.textFieldPassword)?.setText("")
            dismiss()
            // Navigate back to VaultFragment after adding the account
            parentFragmentManager.popBackStack() // This should take you back to the VaultFragment
        }
            .addOnFailureListener {
                UtilityFunctions.showToastMessage(
                    requireContext(),
                    "Account could not registered!"
                )
                view?.findViewById<Button>(R.id.saveButton)?.isEnabled = true
                view?.findViewById<Button>(R.id.saveButton)?.setBackgroundColor(switchOnColor)
                view?.findViewById<Button>(R.id.closeButton)?.isEnabled = true
                view?.findViewById<Button>(R.id.closeButton)?.setBackgroundColor(switchOnColor)
            }
    }
}
