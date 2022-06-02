package com.example.todoapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener

class LoginActivity : AppCompatActivity() {
    lateinit var mAuth: FirebaseAuth
    lateinit var authStateListener: AuthStateListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        setContentView(R.layout.activity_login)

        mAuth= FirebaseAuth.getInstance()

        authStateListener = AuthStateListener {
            val user = mAuth.currentUser
            if (user != null) {
                val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        val toolbar = findViewById<Toolbar>(R.id.loginToolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Login"

        val progressBar = findViewById<ProgressBar>(R.id.progressbar)
        val loginEmail = findViewById<EditText>(R.id.loginEmail)
        val loginPwd = findViewById<EditText>(R.id.loginPassword)
        val loginBtn = findViewById<Button>(R.id.loginButton)
        val loginQn = findViewById<TextView>(R.id.loginPageQuestion)

        loginQn.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@LoginActivity, RegistrationActivity::class.java)
            startActivity(intent)
        })
        loginBtn.setOnClickListener(View.OnClickListener {
            val email = loginEmail.text.toString().trim()
            val password = loginPwd.text.toString().trim()
            if (TextUtils.isEmpty(email)) {
                loginEmail.error = "Email is required"
                return@OnClickListener
            }
            if (TextUtils.isEmpty(password)) {
                loginPwd.error = "Password is required"
                return@OnClickListener
            } else {
                progressBar.visibility = View.VISIBLE
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                        progressBar.visibility = View.GONE
                    } else {
                        val error = task.exception.toString()
                        Toast.makeText(this@LoginActivity, "Login failed$error", Toast.LENGTH_SHORT)
                            .show()
                        progressBar.visibility = View.GONE
                    }
                }
            }
        })
    }
    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
        mAuth.removeAuthStateListener(authStateListener)
    }
}