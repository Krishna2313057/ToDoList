package com.example.todolist.database

import androidx.room.TypeConverter
import com.example.todolist.model.Priority

object Converters {

    @TypeConverter
    fun fromPriority(priority: Priority): String = priority.name

    @TypeConverter
    fun toPriority(priority: String): Priority = Priority.valueOf(priority)
}
