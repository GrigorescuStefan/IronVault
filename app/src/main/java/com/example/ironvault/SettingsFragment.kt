package com.example.ironvault

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val settingsViewInflater = inflater.inflate(R.layout.fragment_settings, container, false)
        val dialogFragment = ChangePasswordFragment()
        val logOutButton: Button = settingsViewInflater.findViewById(R.id.logOutButton)
        val changeMasterPasswordButton: Button = settingsViewInflater.findViewById(R.id.changeMasterPasswordButton)

        changeMasterPasswordButton.setOnClickListener {
            dialogFragment.arguments = arguments
            dialogFragment.show(childFragmentManager, "ChangePasswordFragment")
        }

        logOutButton.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        return settingsViewInflater
    }

}