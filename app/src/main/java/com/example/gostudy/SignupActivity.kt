package com.example.gostudy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.UserProfileChangeRequest
import androidx.core.net.toUri

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var etFullName: EditText
    private lateinit var etSignupEmail: EditText
    private lateinit var etSignupPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnCreateAccount: Button
    private lateinit var tvGoToLogin: TextView
    private lateinit var etPhotoUrl: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        etPhotoUrl = findViewById(R.id.etPhotoUrl)
        etFullName = findViewById(R.id.etFullName)
        etSignupEmail = findViewById(R.id.etSignupEmail)
        etSignupPassword = findViewById(R.id.etSignupPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnCreateAccount = findViewById(R.id.btnCreateAccount)
        tvGoToLogin = findViewById(R.id.tvGoToLogin)


        btnCreateAccount.setOnClickListener {
            signUpUser()
        }

        tvGoToLogin.setOnClickListener {
            finish()  //LoginActivity
        }
    }

    private fun signUpUser() {
        val name = etFullName.text.toString().trim()
        val email = etSignupEmail.text.toString().trim()
        val password = etSignupPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()
        val photoUrl = etPhotoUrl.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show()
            return
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Password and confirmation are required", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user == null) {
                        Toast.makeText(this, "User created but not logged in", Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }

                    val uid = user.uid
                    val db = FirebaseFirestore.getInstance()

                    val profileBuilder = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)

                    if (photoUrl.isNotEmpty()) {
                        profileBuilder.photoUri = photoUrl.toUri()
                    }

                    val profileUpdates = profileBuilder.build()

                    user.updateProfile(profileUpdates)
                        .addOnCompleteListener {
                        }

                    val userData = hashMapOf(
                        "displayName" to name,
                        "photoUrl" to photoUrl
                    )

                    db.collection("users")
                        .document(uid)
                        .set(userData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Signup successful", Toast.LENGTH_SHORT).show()

                            goToHome()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error saving user data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }

                } else {
                    Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun goToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
