package com.example.TodoList.root



import android.app.Application
import com.example.TodoList.database.TaskDatabase
import com.example.TodoList.repository.TaskRepository

class TodoApplication : Application() {

    val database by lazy { TaskDatabase.getDatabase(this) }
    val repository by lazy { TaskRepository(database.taskDao()) }
}

