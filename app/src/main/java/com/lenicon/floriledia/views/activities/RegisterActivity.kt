package com.lenicon.floriledia.views.activities

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.lenicon.floriledia.R
import com.lenicon.floriledia.contracts.RegisterContract
import com.lenicon.floriledia.models.UserPreferences
import com.lenicon.floriledia.presenters.RegisterPresenter

class RegisterActivity : Activity(), RegisterContract.View {

    private lateinit var presenter: RegisterPresenter
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        progressBar = findViewById(R.id.progress_bar)
        presenter = RegisterPresenter(this, UserPreferences(applicationContext))

        val etUser = findViewById<EditText>(R.id.et_reg_username)
        val etEmail = findViewById<EditText>(R.id.et_reg_email)
        val etPassword = findViewById<EditText>(R.id.et_reg_password)
        val btnRegister = findViewById<Button>(R.id.btn_register_submit)
        val btnToLogin = findViewById<Button>(R.id.btn_to_login)

        btnRegister.setOnClickListener {
            val username = etUser.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showMessage("Please fill in all fields")
                return@setOnClickListener
            }

            presenter.register(username, email, password)
        }

        btnToLogin.setOnClickListener { navigateToLogin() }
    }

    override fun showLoading() { progressBar.visibility = View.VISIBLE }
    override fun hideLoading() { progressBar.visibility = View.GONE }
    override fun showMessage(message: String) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }

    override fun navigateToLogin() { finish() }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }
}