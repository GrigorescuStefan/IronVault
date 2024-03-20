package com.example.ironvault

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class FragmentsActivity : FragmentActivity() {

    private lateinit var navigationController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragments_layout)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navigationController = navHostFragment.navController

        val navigationMenu: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        val bundledData = intent.getBundleExtra("credentials")
        val credentialsMap = bundledData?.let { UtilityFunctions.convertBundleToMap(it) }
//        UtilityFunctions.showToastMessage(this, credentialsMap.toString())

        navigationMenu.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.myVault -> {
                    navigationController.navigate(R.id.vaultFragment)
                    true
                }
                R.id.generator -> {
                    navigationController.navigate(R.id.generatorFragment)
                    true
                }
                R.id.scanner -> {
                    navigationController.navigate(R.id.scannerFragment)
                    true
                }
                R.id.settings -> {
                    navigationController.navigate(R.id.settingsFragment)
                    true
                }
                else -> false
            }
        }
    }

}