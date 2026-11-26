package com.example.todoapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoapp.databinding.ActivityDoneBinding
import kotlinx.coroutines.launch

class DoneActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoneBinding
    private lateinit var adapter: TaskAdapter
    private val viewModel: TaskListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = TaskAdapter(
            onOpen = { task -> startActivity(EditorActivity.intentFor(this, task)) },
            onToggleDone = { task, done -> viewModel.setDone(task, done) }
        )
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.doneTasks.collect { tasks ->
                    adapter.submitList(tasks.map { TaskListItem.Plain(it) })
                    binding.emptyView.visibility =
                        if (tasks.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
