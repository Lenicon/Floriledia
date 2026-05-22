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

        // Step 1: Force state configuration matching without refiring execution handlers
        bottomNavigation.selectedItemId = currentItemId

        // Step 2: Handle routing transitions cleanly
        bottomNavigation.setOn someItemClickAction@{ item ->
            if (item.itemId == currentItemId) return@someItemClickAction true

            val targetIntent = when (item.itemId) {
                R.id.nav_florilegium -> Intent(activity, FlorilegiumActivity::class.java)
                R.id.nav_scanner -> Intent(activity, ScannerActivity::class.java)
                R.id.nav_account -> Intent(activity, AccountActivity::class.java)
                else -> return@someItemClickAction false
            }

            // Kill intermediate entry animation lags entirely via intent processing structures
            targetIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            activity.startActivity(targetIntent)
            
            // Instantly sync finish context to avoid stacking historical activities
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