package com.example.todoapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class StatisticsFragment : Fragment() {
    lateinit var mAuth: FirebaseAuth
    lateinit var todosReference: DatabaseReference
    lateinit var statisticReference: DatabaseReference
    lateinit var mUser: FirebaseUser
    lateinit var onlineUserID: String
    lateinit var key:String
    lateinit var rootView : View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        rootView = inflater.inflate(R.layout.fragment_statistics, container, false)

        mAuth = FirebaseAuth.getInstance()
        var mUser = mAuth.currentUser
        onlineUserID = mUser!!.uid
        statisticReference = FirebaseDatabase.getInstance().getReference().child("tasks").child(onlineUserID)
        todosReference = FirebaseDatabase.getInstance().getReference().child("tasks").child(onlineUserID).child("listOfTasks")

        val createdTasksTextView = rootView.findViewById<TextView>(R.id.amountOfCreatedTasks)
        val completedTasksTextView = rootView.findViewById<TextView>(R.id.amountOfCompletedTasks)
        val failedTasksTextView = rootView.findViewById<TextView>(R.id.amountOfFailedTasks)
        statisticReference.child("createdTasks").get().addOnSuccessListener {
            createdTasksTextView.text = it.value.toString()
        }
        statisticReference.child("completedTasks").get().addOnSuccessListener {
            completedTasksTextView.text = it.value.toString()
        }
        statisticReference.child("failedTasks").get().addOnSuccessListener {
            failedTasksTextView.text = it.value.toString()
        }
        return rootView
    }

    companion object {
        fun newInstance() = StatisticsFragment()
    }
}