package com.example.todolist.repository

import com.example.todolist.dao.TaskDao
import com.example.todolist.model.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {

    fun getAllTasks(): Flow<List<Task>> = dao.getAllTasks()

    fun searchTasks(query: String): Flow<List<Task>> = dao.searchTasks(query)

    fun getCompletedTasks(): Flow<List<Task>> = dao.getCompletedTasks()

    fun getIncompleteTasks(): Flow<List<Task>> = dao.getIncompleteTasks()

    fun getHighPriorityTasks(): Flow<List<Task>> = dao.getHighPriorityTasks()

    fun getTasksSortedByPriority(): Flow<List<Task>> = dao.getTasksSortedByPriority()

    fun getTasksSortedByDueDate(): Flow<List<Task>> = dao.getTasksSortedByDueDate()

    fun getTasksByDate(dateMillis: Long): Flow<List<Task>> {
        val startOfDay = dateMillis
        val endOfDay = dateMillis + (24 * 60 * 60 * 1000) - 1
        return dao.getTasksByDateRange(startOfDay, endOfDay)
    }

    suspend fun insert(task: Task) = dao.insertTask(task)

    suspend fun update(task: Task) = dao.updateTask(task)

    suspend fun delete(task: Task) = dao.deleteTask(task)
}
