package com.lenicon.floriledia.views.activities

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

class LoginActivity : Activity(), LoginContract.View {

    private lateinit var presenter: LoginPresenter
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Step 1: Inflate layout structure instantly to avoid window registration context leaks
        setContentView(R.layout.activity_login)

        // Step 2: Initialize views so they are ready for any immediate presenter callbacks
        progressBar = findViewById(R.id.progress_bar)
        
        val etEmail = findViewById<EditText>(R.id.et_login_email) 
        val etPassword = findViewById<EditText>(R.id.et_login_password)
        val btnLogin = findViewById<Button>(R.id.btn_login_submit)
        val btnToRegister = findViewById<Button>(R.id.btn_to_register)

        // Step 3: Set up button click action handlers
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            presenter.login(email, password)
        }

        btnToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Step 4: Initialize presenter dependencies now that the view targets exist safely
        val userPrefs = UserPreferences(applicationContext)
        presenter = LoginPresenter(this, userPrefs)
        
        // Step 5: Safely check session state as the final step
        presenter.checkUserSession()
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

    override fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    override fun navigateToMain() {
        Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, FlorilegiumActivity::class.java)
        startActivity(intent)
        finish() 
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }
}