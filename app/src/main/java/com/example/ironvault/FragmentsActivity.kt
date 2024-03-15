package com.example.ironvault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class FragmentsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragments_layout)
//        val navigationController = findNavController(this, R.id.)
//        val navigationMenu: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        val bundledData = intent.getBundleExtra("credentials")
        val credentialsMap = bundledData?.let { UtilityFunctions.convertBundleToMap(it) }
        UtilityFunctions.showToastMessage(this, credentialsMap.toString())


    }
}