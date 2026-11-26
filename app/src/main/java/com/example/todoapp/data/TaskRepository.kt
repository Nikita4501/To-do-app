package com.example.todoapp.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class TaskRepository(context: Context) {

    private val dao = AppDatabase.get(context).taskDao()

    val activeTasks: Flow<List<Task>> = dao.activeTasks()
    val doneTasks: Flow<List<Task>> = dao.doneTasks()

    suspend fun getById(id: Long): Task? = dao.getById(id)

    suspend fun save(task: Task) {
        if (task.id == 0L) dao.insert(task) else dao.update(task)
    }

    suspend fun delete(task: Task) = dao.delete(task)

    suspend fun setDone(id: Long, done: Boolean) = dao.setDone(id, done)
}
