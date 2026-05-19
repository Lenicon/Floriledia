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

class LoginActivity : Activity(), LoginContract.View {

    private lateinit var presenter: LoginPresenter
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        progressBar = findViewById(R.id.progress_bar)
        presenter = LoginPresenter(this, UserPreferences(applicationContext))

        val etEmail = findViewById<EditText>(R.id.et_login_email)
        val etPassword = findViewById<EditText>(R.id.et_login_password)
        val btnLogin = findViewById<Button>(R.id.btn_login_submit)
        val btnToReg = findViewById<Button>(R.id.btn_to_register)

        btnLogin.setOnClickListener {
            presenter.login(etEmail.text.toString().trim(), etPassword.text.toString())
        }

        btnToReg.setOnClickListener { navigateToRegister() }

        presenter.checkUserSession()
    }

    override fun showLoading() { progressBar.visibility = View.VISIBLE }
    override fun hideLoading() { progressBar.visibility = View.GONE }
    override fun showMessage(message: String) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }

    override fun navigateToRegister() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    override fun navigateToMain() {
        showMessage("Success! Logging in...")
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }
}