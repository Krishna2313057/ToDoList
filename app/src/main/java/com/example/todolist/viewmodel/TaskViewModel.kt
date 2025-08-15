package com.example.todolist.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.todolist.model.Task
import com.example.todolist.repository.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskViewModel(
    application: Application,
    private val repository: TaskRepository
) : AndroidViewModel(application) {

    private val _filter = MutableStateFlow(TaskFilter.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedDateMillis = MutableStateFlow<Long?>(null)

    val filteredTasks: LiveData<List<Task>> = combine(
        _filter, _searchQuery, _selectedDateMillis
    ) { filter, query, dateMillis ->
        Triple(filter, query.trim(), dateMillis)
    }.flatMapLatest { (filter, query, dateMillis) ->
        when {
            query.isNotEmpty() -> repository.searchTasks(query)
            dateMillis != null -> repository.getTasksByDate(dateMillis)
            filter == TaskFilter.COMPLETED -> repository.getCompletedTasks()
            filter == TaskFilter.INCOMPLETE -> repository.getIncompleteTasks()
            filter == TaskFilter.HIGH_PRIORITY -> repository.getHighPriorityTasks()
            filter == TaskFilter.SORT_BY_DUE_DATE -> repository.getTasksSortedByDueDate()
            filter == TaskFilter.SORT_PRIORITY -> repository.getTasksSortedByPriority()
            else -> repository.getAllTasks()
        }
    }.asLiveData()

    fun setFilter(filter: TaskFilter) {
        _filter.value = filter
        
        if (filter != TaskFilter.ALL) {
            _searchQuery.value = ""
            _selectedDateMillis.value = null
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _selectedDateMillis.value = null
    }

    fun setFilterDate(millis: Long?) {
        _selectedDateMillis.value = millis
        _searchQuery.value = ""
    }

    fun insertTask(task: Task) = viewModelScope.launch {
        repository.insert(task)
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        repository.delete(task)
    }

    fun updateTask(task: Task) = viewModelScope.launch {
        repository.update(task)
    }
}
