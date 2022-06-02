package com.example.todoapp.Model

data class TodoItem (
    val task: String = "",
    val description: String = "",
    val id: String = "",
    val date: String = "",
    val importance: Int = 0
)
