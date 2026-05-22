package com.lenicon.floriledia.utils

import android.app.Activity
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.lenicon.floriledia.R
import com.lenicon.floriledia.views.activities.AccountActivity
import com.lenicon.floriledia.views.activities.FlorilegiumActivity
import com.lenicon.floriledia.views.activities.ScannerActivity

object NavigationHelper {

    fun initBottomNavigation(activity: Activity, currentItemId: Int) {
        val bottomNavigation = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation) ?: return

        bottomNavigation.selectedItemId = currentItemId

        bottomNavigation.setOnItemSelectedListener{ item ->
            if (item.itemId == currentItemId) return@setOnItemSelectedListener true

            val targetIntent = when (item.itemId) {
                R.id.nav_florilegium -> Intent(activity, FlorilegiumActivity::class.java)
                R.id.nav_scanner -> Intent(activity, ScannerActivity::class.java)
                R.id.nav_account -> Intent(activity, AccountActivity::class.java)
                else -> return@setOnItemSelectedListener false
            }

            targetIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            activity.startActivity(targetIntent)
            
            activity.finish()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, 0, 0)
            } else {
                @Suppress("DEPRECATION")
                activity.overridePendingTransition(0, 0)
            }
            true
        }
    }
}