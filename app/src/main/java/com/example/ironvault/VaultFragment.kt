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

class VaultFragment : Fragment(), AddElementFragment.OnAccountAddedListener,
    EditElementFragment.OnAccountEditedListener {
    private lateinit var recyclerView: RecyclerView
    private val itemList = mutableListOf<Item>()
    private lateinit var adapter: ItemAdapter

    override fun onAccountAdded() {
        Log.d("VaultFragment", "onAccountAdded called")
        loadData()
    }

    override fun onAccountEdited() {
        Log.d("VaultFragment", "onAccountEdited called")
        loadData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewInflate = inflater.inflate(R.layout.fragment_vault, container, false)
        recyclerView = viewInflate.findViewById(R.id.accountItemsRecycleView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ItemAdapter(itemList, childFragmentManager, this)
        recyclerView.adapter = adapter
        loadData()

        val searchButton: ImageButton = viewInflate.findViewById(R.id.searchButton)
        val searchItemText: EditText = viewInflate.findViewById(R.id.searchItem)
        val addButton: ImageButton = viewInflate.findViewById(R.id.addButton)

        addButton.setOnClickListener {
            val dialogFragment = AddElementFragment()
            dialogFragment.setOnAccountAddedListener(this)
            val emailAddress = arguments?.getBundle("credentials")?.getString("emailAddress")
            val masterPassword = arguments?.getBundle("credentials")?.getString("masterPassword")
            val iv = arguments?.getBundle("credentials")?.getString("iv")
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
            adapter.updateData(filteredList.toMutableList())
        } else {
            adapter.updateData(itemList)
        }
    }

    private fun loadData() {
        val argumentsBundle = arguments
        val emailAddressFromBundle =
            argumentsBundle?.getBundle("credentials")?.getString("emailAddress")
        val masterPasswordFromBundle =
            argumentsBundle?.getBundle("credentials")?.getString("masterPassword")
        val initialVectorFromBundle = argumentsBundle?.getBundle("credentials")?.getString("iv")

        if (emailAddressFromBundle != null && masterPasswordFromBundle != null && initialVectorFromBundle != null) {
            fetchDataFromFirestore(
                emailAddressFromBundle,
                masterPasswordFromBundle,
                initialVectorFromBundle
            )
        }
    }

    private fun fetchDataFromFirestore(
        emailFromDB: String,
        passwordString: String,
        initVector: String
    ) {
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("accounts")
        val decryptionKey = UtilityFunctions.generateAESKeyFromHash(passwordString.toByteArray())
        val iv = Base64.decode(initVector, Base64.DEFAULT)
        collectionRef.get()
            .addOnSuccessListener { result ->
                itemList.clear()
                for (document in result) {
                    val emailAddress = document.getString("emailAddress")
                    if (emailAddress == emailFromDB) {
                        val url = document.getString("url") ?: ""
                        val username = document.getString("username") ?: ""
                        val password = document.getString("password") ?: ""
                        if (url.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
                            val decryptUrl = UtilityFunctions.decryptAES(url, decryptionKey, iv)
                            val decryptUsername =
                                UtilityFunctions.decryptAES(username, decryptionKey, iv)
                            val decryptPassword =
                                UtilityFunctions.decryptAES(password, decryptionKey, iv)
                            val item =
                                Item(emailAddress, decryptUrl, decryptUsername, decryptPassword)
                            itemList.add(item)
                        } else {
                            Log.d("VaultFragment", "Item was not found in database!")
                        }
                    }
                }
                adapter.updateData(itemList)
            }
            .addOnFailureListener { exception ->
                Log.d("VaultFragment", "Error getting documents.", exception)
            }
    }
}
