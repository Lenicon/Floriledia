package com.lenicon.floriledia.views.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.lenicon.floriledia.R
import com.lenicon.floriledia.contracts.AccountContract
import com.lenicon.floriledia.models.UserPreferences
import com.lenicon.floriledia.presenters.AccountPresenter
import com.lenicon.floriledia.utils.NavigationHelper
import androidx.appcompat.app.AppCompatActivity

class AccountActivity : AppCompatActivity(), AccountContract.View {

    private lateinit var presenter: AccountPresenter
    
    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvScanCount: TextView
    private lateinit var tvSavedCount: TextView
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        tvUsername = findViewById(R.id.tv_account_username)
        tvEmail = findViewById(R.id.tv_account_email)
        tvScanCount = findViewById(R.id.tv_scan_count)
        tvSavedCount = findViewById(R.id.tv_saved_count)
        btnLogout = findViewById(R.id.btn_logout)

        presenter = AccountPresenter(this, UserPreferences(applicationContext))

        btnLogout.setOnClickListener {
            presenter.logout()
        }

        NavigationHelper.initBottomNavigation(this, R.id.nav_account)
    }

    override fun onResume() {
        super.onResume()
        // Refresh values whenever the view layout becomes active on the glass screen surface
        presenter.loadUserData()
    }

    override fun displayUserData(username: String, email: String, scanCount: Int, savedCount: Int) {
        tvUsername.text = username
        tvEmail.text = email
        tvScanCount.text = scanCount.toString()
        tvSavedCount.text = savedCount.toString()
    }

    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }
}