package com.example.todoapp.Model

import com.google.firebase.database.Exclude

data class StatisticItem (
    val createdTasks: Int = 0,
    val completedTasks: Int = 0,
    val failedTasks: Int = 0) {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "createdTasks" to createdTasks,
            "completedTasks" to completedTasks,
            "failedTasks" to failedTasks
        )
    }
}

