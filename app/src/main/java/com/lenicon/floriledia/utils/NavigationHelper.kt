package com.lenicon.floriledia.utils

import android.app.Activity
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.lenicon.floriledia.R
import com.lenicon.floriledia.views.AccountActivity
import com.lenicon.floriledia.views.FlorilegiumActivity
import com.lenicon.floriledia.views.ScannerActivity

object NavigationHelper {

    fun initBottomNavigation(activity: Activity, currentItemId: Int) {
        val bottomNavigation = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        
        // Highlight the active tab icon on launch
        bottomNavigation.selectedItemId = currentItemId

        bottomNavigation.setOnItemSelectedListener { item ->
            if (item.itemId == currentItemId) return@setOnItemSelectedListener true

            var targetIntent: Intent? = null

            when (item.itemId) {
                R.id.nav_florilegium -> {
                    targetIntent = Intent(activity, FlorilegiumActivity::class.java)
                }
                R.id.nav_scanner -> {
                    // Replicating your explicit storage load requirement from Flutter
                    // StorageService.load(activity.applicationContext)
                    
                    targetIntent = Intent(activity, ScannerActivity::class.java)
                }
                R.id.nav_account -> {
                    targetIntent = Intent(activity, AccountActivity::class.java)
                }
            }

            if (targetIntent != null) {
                // Key trick: these flags keep from creating new duplicate screen activities
                targetIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NO_ANIMATION
                activity.startActivity(targetIntent)
                // Disable traditional transitions to imitate a smooth Flutter page-swap feel
                activity.overridePendingTransition(0, 0)
                true
            } else {
                false
            }
        }
    }
}