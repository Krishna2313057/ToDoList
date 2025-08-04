package com.example.TodoList.database

import androidx.room.TypeConverter
import com.example.TodoList.model.Priority

class Converters {

    @TypeConverter
    fun fromPriority(priority: Priority): String {
        return priority.name
    }

    @TypeConverter
    fun toPriority(priority: String): Priority {
        return Priority.valueOf(priority)
    }
}
