package com.example.todoapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.Priority
import com.example.todoapp.data.Task
import com.example.todoapp.data.TaskRepository
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditorViewModel(
    private val repository: TaskRepository,
    private val initialTaskId: Long
) : ViewModel() {

    data class UiState(
        val task: Task? = null,
        val loaded: Boolean = false
    )

    private val _uiState = MutableLiveData(UiState())
    val uiState: LiveData<UiState> = _uiState

    fun load() {
        if (_uiState.value?.loaded == true) return
        viewModelScope.launch {
            val task = if (initialTaskId > 0) repository.getById(initialTaskId) else null
            _uiState.value = UiState(task = task, loaded = true)
        }
    }

    fun save(title: String, description: String, priority: Priority, dueDate: Long?) {
        val current = _uiState.value?.task
        viewModelScope.launch {
            val task = (current ?: Task(title = "")).copy(
                title = title,
                description = description,
                priority = priority,
                dueDate = dueDate
            )
            withContext(NonCancellable) { repository.save(task) }
        }
    }

    companion object {
        fun provideFactory(repository: TaskRepository, taskId: Long) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    EditorViewModel(repository, taskId) as T
            }
    }
}
