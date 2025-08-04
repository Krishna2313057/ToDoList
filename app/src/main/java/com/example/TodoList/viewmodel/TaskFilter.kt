package com.example.TodoList.viewmodel

/**
 * Enum class used for filtering and sorting tasks.
 */
enum class TaskFilter {
    // Shows all tasks
    ALL,

    // Shows only completed tasks
    COMPLETED,

    // Shows only incomplete (pending) tasks
    INCOMPLETE,

    // Filters only high-priority tasks
    HIGH_PRIORITY,

    // Sorts tasks by due date (earliest first)
    SORT_BY_DUE_DATE,

    // Sorts tasks by priority (e.g., High > Medium > Low)
    SORT_PRIORITY
}
