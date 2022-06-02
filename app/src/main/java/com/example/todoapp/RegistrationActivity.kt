package com.example.todoapp

import android.app.ProgressDialog
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
import com.example.todoapp.Model.StatisticItem
import com.example.todoapp.Model.TodoItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class RegistrationActivity : AppCompatActivity() {
    lateinit var mAuth: FirebaseAuth
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
        setContentView(R.layout.activity_registration)
        mAuth= FirebaseAuth.getInstance()
        val toolbar = findViewById<Toolbar>(R.id.RegistrationToolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Registration"

        val progressBar = findViewById<ProgressBar>(R.id.progressbar)
        val RegEmail = findViewById<EditText>(R.id.RegistrationEmail)
        val RegPwd = findViewById<EditText>(R.id.RegistrationPassword)
        val RegBtn = findViewById<Button>(R.id.RegistrationButton)
        val RegnQn = findViewById<TextView>(R.id.RegistrationPageQuestion)

        RegnQn.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@RegistrationActivity, LoginActivity::class.java)
            startActivity(intent)
        })

        RegBtn.setOnClickListener(View.OnClickListener {
            val email = RegEmail.text.toString().trim()
            val password = RegPwd.text.toString().trim()
            if (TextUtils.isEmpty(email)) {
                RegEmail.error = "email is required"
                return@OnClickListener
            }
            if (TextUtils.isEmpty(password)) {
                RegPwd.error = "Password required"
                return@OnClickListener
            } else {
                progressBar.visibility = View.VISIBLE
                mAuth!!.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val mUser = mAuth.currentUser
                            val onlineUserID = mUser!!.uid
                            val model = StatisticItem( 0, 0, 0)
                            val reference = FirebaseDatabase.getInstance().getReference().child("tasks").child(onlineUserID)
                            reference.setValue(model)
                            val intent = Intent(this@RegistrationActivity, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                            progressBar.visibility = View.GONE
                        } else {
                            val error = task.exception.toString()
                            Toast.makeText(
                                this@RegistrationActivity,
                                "Registration failed$error", Toast.LENGTH_SHORT
                            ).show()
                            progressBar.visibility = View.GONE
                        }
                    }
            }
        })
    }
}