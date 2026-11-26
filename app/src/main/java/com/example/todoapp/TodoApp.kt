package com.example.todoapp

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.todoapp.data.SettingsRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TodoApp : Application() {

    val repository: com.example.todoapp.data.TaskRepository by lazy { com.example.todoapp.data.TaskRepository(this) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
    private val appScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CoroutineExceptionHandler { _, _ -> }
    )

    override fun onCreate() {
        super.onCreate()
        applySavedTheme()
    }

    private fun applySavedTheme() {
        appScope.launch {
            val mode = settingsRepository.nightMode.first()
            withContext(Dispatchers.Main.immediate) {
                if (AppCompatDelegate.getDefaultNightMode() != mode) {
                    AppCompatDelegate.setDefaultNightMode(mode)
                }
            }
        }
    }
}
