package com.example.ironvault

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class FragmentsActivity : FragmentActivity() {

    private lateinit var navigationController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragments_layout)

        val bundledData = intent.getBundleExtra("credentials")
//        INITIAL SEND OF DATA THROUGH BUNDLE, SO YOU WON'T NEED TO RELOAD THE VAULT FRAGMENT
        val args = Bundle().apply {
            putBundle("credentials", bundledData)
        }
        val initialVaultFragment = VaultFragment().apply {
            arguments = args
        }
        replaceFragment(initialVaultFragment)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navigationController = navHostFragment.navController
        val navigationMenu: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        navigationMenu.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.myVault -> {
                    val vaultArgs = Bundle().apply {
                        putBundle("credentials", bundledData)
                    }
                    val vaultFragment = VaultFragment().apply {
                        arguments = vaultArgs
                    }
                    replaceFragment(vaultFragment)
                    true
                }

                R.id.generator -> {
                    replaceFragment(GeneratorFragment())
                    true
                }

                R.id.scanner -> {
                    replaceFragment(ScannerFragment())
                    true
                }

                R.id.settings -> {
                    val settingsArgs = Bundle().apply {
                        putBundle("credentials", bundledData)
                    }
                    val settingsFragment = SettingsFragment().apply {
                        arguments = settingsArgs
                    }
                    replaceFragment(settingsFragment)
                    true
                }

                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, fragment)
        transaction.commit()
    }
}