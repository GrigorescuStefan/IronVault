package com.example.ironvault

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

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
        val saveButton: Button = view.findViewById(R.id.saveButton)
        val closeButton: Button = view.findViewById(R.id.closeButton)
        val textFieldNewPassword: EditText = view.findViewById(R.id.textFieldNewPassword)
        val textFieldReTypeNewPassword: EditText = view.findViewById(R.id.textFieldReTypeNewPassword)

        val db = Firebase.firestore

        closeButton.setOnClickListener {
            dismiss()
        }

        saveButton.setOnClickListener {
            val credentialsBundle = arguments?.getBundle("credentials")
            val emailAddress = credentialsBundle?.getString("emailAddress")
            val masterPassword = credentialsBundle?.getString("masterPassword")
            val initVector = credentialsBundle?.getString("iv")

            if(UtilityFunctions.checkEmptyString(textFieldNewPassword.text.toString()) || UtilityFunctions.checkEmptyString(textFieldReTypeNewPassword.text.toString())){
                UtilityFunctions.showToastMessage(requireContext(), "Password field cannot be left empty!")
                return@setOnClickListener
            }

            if(textFieldNewPassword.text.toString().length < minNumberOfCharacters || textFieldReTypeNewPassword.text.toString().length < minNumberOfCharacters){
                UtilityFunctions.showToastMessage(requireContext(), "Password cannot be smaller than $minNumberOfCharacters characters!")
                return@setOnClickListener
            }

            if(textFieldNewPassword.text.toString() != textFieldReTypeNewPassword.text.toString()){
                UtilityFunctions.showToastMessage(requireContext(), "Passwords do not match!")
                return@setOnClickListener
            }

            db.collection("users").get().addOnSuccessListener { documents ->
                for (document in documents){
                    val emailAddressInDB = document.getString("emailAddress")
                    if(emailAddressInDB == emailAddress){
                        UtilityFunctions.showToastMessage(requireContext(), "Email found in database!")
//                        TODO: replace password for this account, and proceed to query the entire collection of users to decrypt and re-encrypt data
                    }
                }
            }
        }
    }
}