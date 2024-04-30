package com.example.ironvault

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
class EditElementFragment: DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_element, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val textFieldURL: EditText = view.findViewById(R.id.textFieldURL)
        val textFieldUsername: EditText = view.findViewById(R.id.textFieldUsername)
        val textFieldPassword: EditText = view.findViewById(R.id.textFieldPassword)
        val closeButton: Button = view.findViewById(R.id.closeButton)

        val bundleURL: String = arguments?.getString("url").toString()
        val bundleUsername: String = arguments?.getString("username").toString()
        val bundlePassword: String = arguments?.getString("password").toString()

        textFieldURL.setText(bundleURL)
        textFieldUsername.setText(bundleUsername)
        textFieldPassword.setText(bundlePassword)

        closeButton.setOnClickListener {
            dismiss()
        }
    }

}