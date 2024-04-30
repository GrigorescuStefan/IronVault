package com.example.ironvault

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class VaultFragment : Fragment(), AddElementFragment.OnAccountAddedListener {
    private lateinit var recyclerView: RecyclerView
    private val itemList = mutableListOf<Item>()

    override fun onAccountAdded() {
        loadData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewInflate = inflater.inflate(R.layout.fragment_vault, container, false)
        val argumentsBundle = arguments
        recyclerView = viewInflate.findViewById(R.id.accountItemsRecycleView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        loadData()

        val searchButton: ImageButton = viewInflate.findViewById(R.id.searchButton)
        val searchItemText: EditText = viewInflate.findViewById(R.id.searchItem)
        val addButton: ImageButton = viewInflate.findViewById(R.id.addButton)

        addButton.setOnClickListener {
            val dialogFragment = AddElementFragment()
            dialogFragment.setOnAccountAddedListener(this)
            val emailAddress = argumentsBundle?.getBundle("credentials")?.getString("emailAddress")
            val masterPassword =
                argumentsBundle?.getBundle("credentials")?.getString("masterPassword")
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

        searchButton.setOnClickListener {
            val searchText = searchItemText.text.toString()
            filterData(searchText)
        }

        return viewInflate
    }
    private fun filterData(query: String) {
        if (query.isNotEmpty()) {
            val filteredList = itemList.filter { item ->
                item.url.contains(query, ignoreCase = true)
            }
            val adapter = ItemAdapter(filteredList.toMutableList(), childFragmentManager)
            recyclerView.adapter = adapter
        } else {
            val adapter = ItemAdapter(itemList, childFragmentManager)
            recyclerView.adapter = adapter
        }
    }


    private fun loadData() {
        val argumentsBundle = arguments
        val emailAddressFromBundle = argumentsBundle?.getBundle("credentials")?.getString("emailAddress")
        val masterPasswordFromBundle = argumentsBundle?.getBundle("credentials")?.getString("masterPassword")
        val initialVectorFromBundle = argumentsBundle?.getBundle("credentials")?.getString("iv")

        if(emailAddressFromBundle != null && masterPasswordFromBundle != null && initialVectorFromBundle != null) {
            fetchDataFromFirestore(emailAddressFromBundle, masterPasswordFromBundle, initialVectorFromBundle)
        }
    }

        private fun fetchDataFromFirestore(emailFromDB: String, passwordString: String, initVector: String) {
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("accounts")
        val decryptionKey = UtilityFunctions.generateAESKeyFromHash(passwordString.toByteArray())
        val iv = Base64.decode(initVector, Base64.DEFAULT)
        collectionRef.get()
            .addOnSuccessListener { result ->
                itemList.clear()
                for (document in result) {
                    val emailAddress = document.getString("emailAddress")
                    if(emailAddress == emailFromDB) {
                        val url = document.getString("url") ?: ""
                        val username = document.getString("username") ?: ""
                        val password = document.getString("password") ?: ""
                        if(url != "" && username != "" && password != "")
                        {
                            val decryptUrl = UtilityFunctions.decryptAES(url, decryptionKey, iv)
                            val decryptUsername = UtilityFunctions.decryptAES(username, decryptionKey, iv)
                            val decryptPassword = UtilityFunctions.decryptAES(password, decryptionKey, iv)
                            val item = Item(decryptUrl, decryptUsername, decryptPassword)
                            itemList.add(item)
                        } else {
                            Log.d("ERROR_404", "Item was not found in database!")
                        }
                    }
                }

                val adapter = ItemAdapter(itemList, childFragmentManager)
                recyclerView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.d("ERROR_DOCUMENTS", "Error getting documents.", exception)
            }
    }

}