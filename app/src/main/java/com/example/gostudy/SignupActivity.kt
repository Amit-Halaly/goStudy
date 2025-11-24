package com.example.gostudy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var etFullName: EditText
    private lateinit var etSignupEmail: EditText
    private lateinit var etSignupPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnCreateAccount: Button
    private lateinit var tvGoToLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        etFullName = findViewById(R.id.etFullName)
        etSignupEmail = findViewById(R.id.etSignupEmail)
        etSignupPassword = findViewById(R.id.etSignupPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnCreateAccount = findViewById(R.id.btnCreateAccount)
        tvGoToLogin = findViewById(R.id.tvGoToLogin)

        btnCreateAccount.setOnClickListener {
            handleSignup()
        }

        tvGoToLogin.setOnClickListener {
            finish()  //LoginActivity
        }
    }

    private fun handleSignup() {
        val fullName = etFullName.text.toString().trim()
        val email = etSignupEmail.text.toString().trim()
        val password = etSignupPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (fullName.isEmpty()) {
            etFullName.error = "Name is required"
            return
        }
        if (email.isEmpty()) {
            etSignupEmail.error = "Email is required"
            return
        }
        if (password.isEmpty()) {
            etSignupPassword.error = "Password is required"
            return
        }
        if (password.length < 6) {
            etSignupPassword.error = "At least 6 characters"
            return
        }
        if (password != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                    goToHome()
                } else {
                    Toast.makeText(
                        this,
                        "Signup failed: ${task.exception?.localizedMessage ?: "Unknown error"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun goToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
