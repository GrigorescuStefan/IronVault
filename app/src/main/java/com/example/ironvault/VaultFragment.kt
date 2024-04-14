package com.example.ironvault

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment

class VaultFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewInflate = inflater.inflate(R.layout.fragment_vault, container, false)

        val argumentsBundle = arguments

//        val searchButton: ImageButton = viewInflate.findViewById(R.id.searchButton)
//        val searchItemText: EditText = viewInflate.findViewById(R.id.searchItem)
        val addButton: ImageButton = viewInflate.findViewById(R.id.addButton)

        addButton.setOnClickListener {
            val dialogFragment = AddElementFragment()
            val emailAddress = argumentsBundle?.getBundle("credentials")?.getString("emailAddress")
            val masterPassword = argumentsBundle?.getBundle("credentials")?.getString("masterPassword")
            val iv = argumentsBundle?.getBundle("credentials")?.getString("iv")
            if (emailAddress != null && masterPassword != null && iv != null) {
                val bundle = Bundle().apply {
                    putString("emailAddress", emailAddress)
                    putString("masterPassword", masterPassword)
                    putString("iv", iv)
                }
                dialogFragment.arguments = bundle
            }
            dialogFragment.show(childFragmentManager, "AddElementFragment")
        }
        return viewInflate
    }


}