package com.example.bookcatalogapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bookcatalogapp.databinding.ActivitySignupBinding


class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

            binding = ActivitySignupBinding.inflate(layoutInflater)
            setContentView(binding.root)

            databaseHelper = DatabaseHelper(this)

            binding.signupButton.setOnClickListener {
                val signupUsername = binding.signupUsername.text.toString()
                val signupUEmail = binding.signupEmail.text.toString()
                val signupUPassword = binding.signupPassword.text.toString()
                signupDatabase(signupUsername, signupUEmail, signupUPassword)
            }

            binding.loginRedirect.setOnClickListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
    }
    private fun signupDatabase(username: String, email: String, password: String) {
        val insertedRowId = databaseHelper.insertUser(username, email, password)
        if (insertedRowId != -1L) {
            Toast.makeText(this, "Signup Successful", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Signup Failed", Toast.LENGTH_SHORT).show()
        }
    }
}
