package com.example.ironvault

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView

class VaultFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewInflate = inflater.inflate(R.layout.fragment_vault, container, false)
        val searchButton: ImageButton = viewInflate.findViewById(R.id.searchButton)
        val addButton: ImageButton = viewInflate.findViewById(R.id.addButton)
        val searchItemTextView: TextView = viewInflate.findViewById(R.id.searchItem)


        addButton.setOnClickListener {
            val dialogFragment = AddElementFragment()
            dialogFragment.show(childFragmentManager, "AddElementFragment")
        }
        return viewInflate
    }

}