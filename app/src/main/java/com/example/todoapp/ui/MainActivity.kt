package com.example.todoapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.databinding.ActivityMainBinding
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: TaskAdapter
    private val viewModel: TaskListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = TaskAdapter(
            onOpen = { task -> startActivity(EditorActivity.intentFor(this, task)) },
            onToggleDone = { task, done -> viewModel.setDone(task, done) }
        )
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        setSupportActionBar(binding.toolbar)
        setupSwipeToDelete()
        observeViewModel()

        binding.fabAdd.setOnClickListener {
            startActivity(EditorActivity.intentFor(this, com.example.todoapp.data.Task(title = "")))
        }
    }

    private fun setupSwipeToDelete() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position == RecyclerView.NO_POSITION) return
                val task = adapter.itemAt(position)
                viewModel.delete(task)
            }
        }).attachToRecyclerView(binding.recycler)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.taskListItems.collect { items ->
                        adapter.submitList(items)
                        binding.emptyView.visibility =
                            if (items.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        updateThemeMenuTitle(menu.findItem(R.id.action_toggle_theme))
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        updateThemeMenuTitle(menu.findItem(R.id.action_toggle_theme))
        return super.onPrepareOptionsMenu(menu)
    }

    private fun updateThemeMenuTitle(item: MenuItem) {
        val dark = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        item.setTitle(if (dark) R.string.toggle_theme_light else R.string.toggle_theme)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_done -> {
                startActivity(Intent(this, DoneActivity::class.java))
                true
            }
            R.id.action_toggle_theme -> {
                toggleTheme()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleTheme() {
        val app = application as com.example.todoapp.TodoApp
        val toDark = AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES
        val newMode = if (toDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(newMode)
        lifecycleScope.launch {
            withContext(NonCancellable) { app.settingsRepository.setNightMode(newMode) }
        }
    }
}
