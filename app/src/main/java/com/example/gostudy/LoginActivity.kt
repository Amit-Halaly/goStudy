package com.example.gostudy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class LoginActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvSignup: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvSignup = findViewById(R.id.tvSignup)

        btnLogin.setOnClickListener {
            handleLogin()
        }

        tvSignup.setOnClickListener {
            Toast.makeText(this, "Signup screen coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            return
        }

        Toast.makeText(this, "Trying to login as $email", Toast.LENGTH_SHORT).show()

        goToHome()
    }

    private fun goToHome() { //Replace with firebase Auto
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }}