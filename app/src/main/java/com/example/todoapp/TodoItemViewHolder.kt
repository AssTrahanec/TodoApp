package com.example.todoapp

import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TodoItemViewHolder(var mView: View) : RecyclerView.ViewHolder(mView) {
    fun setTask(task: String?) {
        val taskTextView = mView.findViewById<TextView>(R.id.taskTv)
        taskTextView.text = task
    }

    fun setDesc(desc: String?) {
        val descTextView = mView.findViewById<TextView>(R.id.descriptionTv)
        descTextView.text = desc
    }

    fun setDate(date: String?) {
        val dateTextView = mView.findViewById<TextView>(R.id.dateTv)
        dateTextView.text = date
    }
    fun setImportance(importance: Int?){
        val view = mView.findViewById<View>(R.id.importanceColor)

        when (importance) {
            3 -> {
                view.setBackgroundColor(Color.parseColor("#CD5C5C"))
            }
            2 -> {
                view.setBackgroundColor(Color.parseColor("#FFD700"))
            }
            else -> {
                view.setBackgroundColor(Color.parseColor("#9ACD32"))
            }
        }

    }
}