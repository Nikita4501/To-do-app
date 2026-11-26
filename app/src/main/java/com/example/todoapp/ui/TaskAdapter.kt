package com.example.todoapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.data.Priority
import com.example.todoapp.data.Task
import com.example.todoapp.databinding.ItemTaskBinding

class TaskAdapter(
    private val onOpen: (Task) -> Unit,
    private val onToggleDone: (Task, Boolean) -> Unit
) : ListAdapter<TaskListItem, TaskAdapter.TaskViewHolder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun itemAt(position: Int): Task = getItem(position).task

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var current: TaskListItem? = null

        init {
            binding.root.setOnClickListener { current?.let { onOpen(it.task) } }
            binding.checkDone.setOnClickListener {
                current?.let { onToggleDone(it.task, binding.checkDone.isChecked) }
            }
        }

        fun bind(item: TaskListItem) {
            current = item
            val context = binding.root.context
            val task = item.task

            binding.textTitle.text = task.title
            binding.textDescription.text = task.description
            binding.textDescription.visibility =
                if (task.description.isBlank()) android.view.View.GONE else android.view.View.VISIBLE
            binding.checkDone.isChecked = task.isDone

            val (nameRes, colorRes) = when (task.priority) {
                Priority.HIGH -> R.string.priority_high to R.color.priority_high
                Priority.MEDIUM -> R.string.priority_medium to R.color.priority_medium
                Priority.LOW -> R.string.priority_low to R.color.priority_low
            }
            binding.textPriority.text = context.getString(nameRes)
            binding.textPriority.setTextColor(ContextCompat.getColor(context, colorRes))

            when (item) {
                is TaskListItem.Overdue -> {
                    binding.textDue.text = context.getString(R.string.overdue)
                    binding.textDue.setTextColor(ContextCompat.getColor(context, R.color.error_red))
                    binding.textTitle.setTextColor(ContextCompat.getColor(context, R.color.error_red))
                }
                is TaskListItem.Plain -> {
                    if (task.dueDate != null) {
                        binding.textDue.text = context.getString(R.string.due_date_short, OverduePolicy.formatDate(task.dueDate))
                    } else {
                        binding.textDue.text = ""
                    }
                    binding.textDue.setTextColor(ContextCompat.getColor(context, R.color.due_soon))
                    binding.textTitle.setTextColor(ContextCompat.getColor(context, R.color.md_theme_onSurface))
                }
            }
        }
    }

    private object Diff : DiffUtil.ItemCallback<TaskListItem>() {
        override fun areItemsTheSame(oldItem: TaskListItem, newItem: TaskListItem): Boolean =
            oldItem.task.id == newItem.task.id

        override fun areContentsTheSame(oldItem: TaskListItem, newItem: TaskListItem): Boolean =
            oldItem == newItem
    }
}
