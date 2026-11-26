package com.example.todoapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.TodoApp
import com.example.todoapp.data.Task
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface TaskListItem {
    val task: Task

    data class Plain(override val task: Task) : TaskListItem
    data class Overdue(override val task: Task) : TaskListItem
}

class TaskListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as TodoApp).repository

    val doneTasks: StateFlow<List<Task>> = repository.doneTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val activeTasks: StateFlow<List<Task>> = repository.activeTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val taskListItems: StateFlow<List<TaskListItem>> = activeTasks
        .map { tasks -> tasks.map { it.toListItem() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun Task.toListItem(): TaskListItem {
        val todayEnd = OverduePolicy.todayEnd()
        return if (dueDate != null && dueDate < todayEnd) {
            TaskListItem.Overdue(this)
        } else {
            TaskListItem.Plain(this)
        }
    }

    fun setDone(task: Task, done: Boolean) {
        viewModelScope.launch { repository.setDone(task.id, done) }
    }

    fun delete(task: Task) {
        viewModelScope.launch { repository.delete(task) }
    }
}
