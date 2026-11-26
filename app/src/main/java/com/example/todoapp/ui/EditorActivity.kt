package com.example.todoapp.ui

import android.os.Build
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.todoapp.R
import com.example.todoapp.TodoApp
import com.example.todoapp.data.Priority
import com.example.todoapp.data.Task
import com.example.todoapp.databinding.ActivityEditorBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import java.util.Calendar

class EditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditorBinding
    private val viewModel: EditorViewModel by viewModels {
        EditorViewModel.provideFactory(
            (application as TodoApp).repository,
            taskFromIntent()?.id ?: 0L
        )
    }

    private fun taskFromIntent(): Task? =
        if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(EXTRA_TASK, Task::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_TASK)
        }

    private var selectedDueDate: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupPriorityChips()
        setupDatePicker()
        setupTextWatchers()

        savedInstanceState?.let {
            if (it.containsKey(KEY_DUE_DATE)) {
                selectedDueDate = it.getLong(KEY_DUE_DATE)
                updateDueDateLabel()
                updateOverdueHighlight()
            }
        }

        viewModel.load()

        viewModel.uiState.observe(this) { state ->
            if (!state.loaded || binding.editTitle.text?.isNotEmpty() == true) return@observe
            state.task?.let { task -> fillForm(task) }
            if (state.task == null) {
                handlePrefill()
            }
        }

        binding.btnSave.setOnClickListener { saveTask() }
    }

    private fun fillForm(task: Task) {
        binding.editTitle.setText(task.title)
        binding.editDescription.setText(task.description)
        selectedDueDate = task.dueDate
        updateDueDateLabel()
        when (task.priority) {
            Priority.LOW -> binding.chipGroupPriority.check(R.id.chip_low)
            Priority.MEDIUM -> binding.chipGroupPriority.check(R.id.chip_medium)
            Priority.HIGH -> binding.chipGroupPriority.check(R.id.chip_high)
        }
        updateSaveButton()
        updateOverdueHighlight()
        updateCounterText()
    }

    private fun handlePrefill() {
        val prefill = intent.getStringExtra(EXTRA_PREFILL)
        if (!prefill.isNullOrBlank() && binding.editTitle.text.isNullOrBlank()) {
            binding.editTitle.setText(prefill)
            binding.titleLayout.error = null
            updateSaveButton()
            updateCounterText()
            binding.editTitle.requestFocus()
        }
    }

    private fun setupTextWatchers() {
        // 1. Активация кнопки «Сохранить»: поле названия не должно быть пустым.
        binding.editTitle.doAfterTextChanged { text ->
            val value = text?.toString()?.trim().orEmpty()
            binding.btnSave.isEnabled = value.isNotEmpty()
            binding.titleLayout.error =
                if (value.isEmpty()) getString(R.string.error_empty_title) else null
        }

        // 2. Счётчик символов описания.
        binding.editDescription.doAfterTextChanged { _ ->
            updateCounterText()
        }

        // 3. Подсветка просроченной даты при вводе названия.
        binding.editTitle.doAfterTextChanged { _ ->
            updateOverdueHighlight()
        }

        binding.btnSave.isEnabled = false
    }

    private fun updateSaveButton() {
        binding.btnSave.isEnabled = !binding.editTitle.text.isNullOrBlank()
    }

    private fun updateCounterText() {
        val length = binding.editDescription.text?.length ?: 0
        binding.textCounter.text = getString(R.string.description_counter, length, MAX_DESCRIPTION)
    }

    private fun updateOverdueHighlight() {
        val due = selectedDueDate
        val overdue = due != null && due < OverduePolicy.todayEnd()
        val color = if (overdue) {
            getColor(R.color.error_red)
        } else {
            getColor(R.color.md_theme_onSurfaceVariant)
        }
        binding.textDueDate.setTextColor(color)
        updateDueDateLabel()
    }

    private fun setupPriorityChips() {
        binding.chipGroupPriority.setOnCheckedStateChangeListener { _, _ ->
            updateOverdueHighlight()
        }
    }

    private fun setupDatePicker() {
        binding.btnPickDate.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.pick_date)
                .setSelection(selectedDueDate ?: MaterialDatePicker.todayInUtcMilliseconds())
                .build()
            picker.addOnPositiveButtonClickListener(
                object : MaterialPickerOnPositiveButtonClickListener<Long> {
                    override fun onPositiveButtonClick(selection: Long?) {
                        val pickedUtc = selection ?: return
                        val utc = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = pickedUtc
                        }
                        selectedDueDate = Calendar.getInstance().apply {
                            set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        updateDueDateLabel()
                        updateOverdueHighlight()
                    }
                }
            )
            picker.show(supportFragmentManager, "date_picker")
        }
        binding.btnClearDate.setOnClickListener {
            selectedDueDate = null
            updateDueDateLabel()
            updateOverdueHighlight()
        }
    }

    private fun updateDueDateLabel() {
        selectedDueDate?.let {
            binding.textDueDate.text = getString(R.string.due_date_label, OverduePolicy.formatDate(it))
            binding.textDueDate.visibility = View.VISIBLE
        } ?: run {
            binding.textDueDate.text = getString(R.string.no_due_date)
            binding.textDueDate.visibility = View.VISIBLE
        }
    }

    private fun saveTask() {
        val title = binding.editTitle.text?.toString()?.trim().orEmpty()
        if (title.isEmpty()) {
            binding.titleLayout.error = getString(R.string.error_empty_title)
            return
        }
        val description = binding.editDescription.text?.toString()?.trim().orEmpty()
        val priority = when (binding.chipGroupPriority.checkedChipId) {
            R.id.chip_low -> Priority.LOW
            R.id.chip_high -> Priority.HIGH
            else -> Priority.MEDIUM
        }
        viewModel.save(title, description, priority, selectedDueDate)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        selectedDueDate?.let { outState.putLong(KEY_DUE_DATE, it) }
    }

    companion object {
        private const val EXTRA_TASK = "extra_task"
        private const val EXTRA_PREFILL = "extra_prefill"
        private const val KEY_DUE_DATE = "key_due_date"
        private const val MAX_DESCRIPTION = 200

        fun intentFor(context: Context, task: Task): Intent =
            Intent(context, EditorActivity::class.java).putExtra(EXTRA_TASK, task)

        fun prefillIntent(context: Context, prefill: String): Intent =
            Intent(context, EditorActivity::class.java).putExtra(EXTRA_PREFILL, prefill)
    }
}
