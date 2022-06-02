package com.example.todoapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.Model.TodoItem
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.text.DateFormat
import java.util.*


class HomeActivity : AppCompatActivity() {
    lateinit var mAuth: FirebaseAuth
    lateinit var todosReference: DatabaseReference
    lateinit var statisticReference: DatabaseReference
    lateinit var onlineUserID: String
    lateinit var task: String


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
        mAuth = FirebaseAuth.getInstance()
        var mUser = mAuth.currentUser
        onlineUserID = mUser!!.uid
        todosReference = FirebaseDatabase.getInstance().getReference().child("tasks").child(onlineUserID).child("listOfTasks")
        statisticReference = FirebaseDatabase.getInstance().getReference().child("tasks").child(onlineUserID)
        setContentView(R.layout.activity_home)

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true

        val floatingActionButton = findViewById<FloatingActionButton>(R.id.fab)
        floatingActionButton.setOnClickListener(View.OnClickListener {
            addTask()
        })

        val navView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        val navController = findNavController(R.id.nav_host_fragment)
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    //toolbar?.setTitle("Home")
                    navController.navigate(R.id.action_navigation_statistics_to_navigation_home)
                    true
                }
                R.id.statistics -> {
                    //toolbar?.setTitle("Statistics")
                    navController.navigate(R.id.action_navigation_home_to_navigation_statistics)
                    true
                }

                else -> false
            }

        }
        navView.setOnItemReselectedListener { item ->
            when(item.itemId){
                R.id.home ->{
                    true
                }
                R.id.statistics ->{
                    true
                }
            }
        }
    }
    fun addTask(){
        val myDialog = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)

        val myView = inflater.inflate(R.layout.input_file, null)
        myDialog.setView(myView)

        val dialog = myDialog.create()
        dialog.setCancelable(false)
        val task = myView.findViewById<EditText>(R.id.task)
        val description = myView.findViewById<EditText>(R.id.description)
        val save = myView.findViewById<Button>(R.id.saveBtn)
        val cancel = myView.findViewById<Button>(R.id.CancelBtn)
        val radioGroup = myView.findViewById<RadioGroup>(R.id.radioButtonGroup)

        cancel.setOnClickListener {
            dialog.dismiss()
        }

        save.setOnClickListener(View.OnClickListener {
            val mTask = task.text.toString().trim()
            val mDescription = description.text.toString().trim()
            val id = todosReference.push().getKey()
            val date = DateFormat.getDateInstance().format(Date())
            var radioImportance: Int? = null
            radioImportance = when (radioGroup.checkedRadioButtonId) {
                R.id.radioRed -> {
                    1
                }
                R.id.radioYellow -> {
                    2
                }

                else ->{
                    3
                }
            }
            if (TextUtils.isEmpty(mTask)) {
                task.error = "Task Required"
                return@OnClickListener
            }
            if (TextUtils.isEmpty(mDescription)) {
                description.error = "Description Required"
                return@OnClickListener
            } else {
                val model = TodoItem(mTask, mDescription, id!!, date, radioImportance)
                todosReference.child(id!!).setValue(model).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        statisticReference.child("createdTasks").get().addOnSuccessListener {
                            statisticReference.child("createdTasks").setValue(it.value.toString().toInt() + 1)
                        }
                        Toast.makeText(
                            this@HomeActivity,
                            "Task has been inserted successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val error = task.exception.toString()
                        Toast.makeText(this@HomeActivity, "Failed: $error", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            dialog.dismiss()
        })

        dialog.show()
    }

}