package com.example.todolist.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

import androidx.room.Entity
import androidx.room.PrimaryKey


@Parcelize

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String?,
    val priority: Priority,
    val dueDate: Long?,
    val completed: Boolean = false
) : Parcelable

enum class Priority {
    HIGH, MEDIUM, LOW
}
