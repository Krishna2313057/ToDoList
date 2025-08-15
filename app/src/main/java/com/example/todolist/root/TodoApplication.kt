package com.example.todolist.root



import android.app.Application
import com.example.todolist.database.TaskDatabase
import com.example.todolist.repository.TaskRepository

class TodoApplication : Application() {

    val database by lazy { TaskDatabase.getDatabase(this) }
    val repository by lazy { TaskRepository(database.taskDao()) }
}

