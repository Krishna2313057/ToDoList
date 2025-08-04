package com.example.TodoList.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

enum class Priority {
    HIGH,
    MEDIUM,
    LOW
}

@Parcelize
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String = "",

    val description: String = "",

    val priority: Priority = Priority.LOW, // Use ENUM not String

    val isCompleted: Boolean = false,

    val dueDate: Long? = null
) : Parcelable
