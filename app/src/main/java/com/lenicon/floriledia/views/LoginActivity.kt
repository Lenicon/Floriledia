package com.lenicon.floriledia.views

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.lenicon.floriledia.R
import com.lenicon.floriledia.contracts.LoginContract
import com.lenicon.floriledia.models.UserPreferences
import com.lenicon.floriledia.presenters.LoginPresenter

// FIX: Implement LoginContract.View to let the presenter control UI transitions safely
class LoginActivity : Activity(), LoginContract.View {

    private lateinit var presenter: LoginPresenter
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize the presenter with its dependencies
        val userPrefs = UserPreferences(applicationContext)
        presenter = LoginPresenter(this, userPrefs)
        
        // 2. Delegate the auto-login session check to the presenter
        presenter.checkUserSession()

        setContentView(R.layout.activity_login)

        progressBar = findViewById(R.id.progress_bar)
        
        val etEmail = findViewById<EditText>(R.id.et_login_email) 
        val etPassword = findViewById<EditText>(R.id.et_login_password)
        val btnLogin = findViewById<Button>(R.id.btn_login_submit)
        val btnToRegister = findViewById<Button>(R.id.btn_to_register)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            // 3. Hand over login responsibility directly to the presenter architecture flow
            presenter.login(email, password)
        }

        btnToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    // --- MVP View Contract Interface Implementations ---

    override fun showLoading() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        progressBar.visibility = View.GONE
    }

    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun navigateToMain() {
        Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, FlorilegiumActivity::class.java)
        startActivity(intent)
        finish() // Terminate this activity state instance so back buttons won't reopen it
    }

    override fun onDestroy() {
        // 4. Clean up background jobs to block asynchronous memory leakage issues
        presenter.detachView()
        super.onDestroy()
    }
}