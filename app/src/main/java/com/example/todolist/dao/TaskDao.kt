package com.example.todolist.dao

import androidx.room.*
import com.example.todolist.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE completed = 1 ORDER BY id DESC")
    fun getCompletedTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE completed = 0 ORDER BY id DESC")
    fun getIncompleteTasks(): Flow<List<Task>>


    @Query("SELECT * FROM tasks WHERE priority = 'HIGH' ORDER BY id DESC")
    fun getHighPriorityTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    fun getTasksSortedByDueDate(): Flow<List<Task>>

    @Query(
        """
        SELECT * FROM tasks 
        ORDER BY 
            CASE priority
                WHEN 'HIGH' THEN 1
                WHEN 'MEDIUM' THEN 2
                WHEN 'LOW' THEN 3
                ELSE 4
            END, 
            id DESC
        """
    )
    fun getTasksSortedByPriority(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :query || '%' ORDER BY id DESC")
    fun searchTasks(query: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE dueDate BETWEEN :start AND :end ORDER BY dueDate ASC")
    fun getTasksByDateRange(start: Long, end: Long): Flow<List<Task>>
}
