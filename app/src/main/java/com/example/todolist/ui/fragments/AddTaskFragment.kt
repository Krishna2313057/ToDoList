package com.example.todolist.ui.fragments

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.todolist.database.TaskDatabase
import com.example.todolist.databinding.FragmentAddTaskBinding
import com.example.todolist.model.Priority
import com.example.todolist.model.Task
import com.example.todolist.notifications.NotificationReceiver
import com.example.todolist.repository.TaskRepository
import com.example.todolist.util.NotificationHelper
import com.example.todolist.viewmodel.TaskViewModel
import com.example.todolist.viewmodel.TaskViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddTaskFragment : Fragment() {

    private var _binding: FragmentAddTaskBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskViewModel: TaskViewModel
    private val args: AddTaskFragmentArgs by navArgs()
    private var currentTask: Task? = null

    private val priorities = listOf("Low", "Medium", "High")
    private var dueDate: Long? = null
    private val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        NotificationHelper.createNotificationChannel(requireContext())

        val application = requireNotNull(activity).application
        val dao = TaskDatabase.getDatabase(application).taskDao()
        val repository = TaskRepository(dao)
        val factory = TaskViewModelFactory(application, repository)
        taskViewModel = ViewModelProvider(this, factory)[TaskViewModel::class.java]

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPriority.adapter = adapter

        currentTask = args.task
        val dueDateArg = args.dueDate

        currentTask?.let { task ->
            binding.editTextTitle.setText(task.title)
            binding.editTextDescription.setText(task.description)
            binding.checkboxCompleted.isChecked = task.completed
            val pos = priorities.indexOfFirst { it.equals(task.priority.name, ignoreCase = true) }.takeIf { it >= 0 } ?: 1
            binding.spinnerPriority.setSelection(pos)

            task.dueDate?.let {
                dueDate = it
                binding.textViewDueDate.text = dateFormat.format(Date(it))
            }
        } ?: run {
            if (dueDateArg != 0L) {
                dueDate = dueDateArg
                binding.textViewDueDate.text = dateFormat.format(Date(dueDateArg))
            }
        }

        binding.textViewDueDate.setOnClickListener { openDateTimePicker() }
        binding.buttonSaveTask.setOnClickListener { saveTask() }
    }

    private fun openDateTimePicker() {
        dueDate?.let { calendar.timeInMillis = it }

        DatePickerDialog(
            requireContext(),
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                TimePickerDialog(
                    requireContext(),
                    { _: TimePicker, hourOfDay: Int, minute: Int ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        dueDate = calendar.timeInMillis
                        binding.textViewDueDate.text = dateFormat.format(Date(dueDate!!))
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()

            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveTask() {
        val title = binding.editTextTitle.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()
        val isCompleted = binding.checkboxCompleted.isChecked
        val priority = when (binding.spinnerPriority.selectedItem.toString().lowercase(Locale.getDefault())) {
            "high" -> Priority.HIGH
            "medium" -> Priority.MEDIUM
            else -> Priority.LOW
        }

        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a title", Toast.LENGTH_SHORT).show()
            return
        }

        val task = if (currentTask != null) {
            currentTask!!.copy(
                title = title,
                description = description,
                completed = isCompleted,
                priority = priority,
                dueDate = dueDate
            ).also {
                taskViewModel.updateTask(it)
                NotificationHelper.showNotification(requireContext(), "Task Updated", "You updated: \"$title\"")
                Toast.makeText(requireContext(), "Task updated", Toast.LENGTH_SHORT).show()
            }
        } else {
            Task(
                title = title,
                description = description,
                completed = isCompleted,
                priority = priority,
                dueDate = dueDate
            ).also {
                taskViewModel.insertTask(it)
                NotificationHelper.showNotification(requireContext(), "New Task Added", "You added: \"$title\"")
                Toast.makeText(requireContext(), "Task added", Toast.LENGTH_SHORT).show()
            }
        }

        dueDate?.takeIf { it > System.currentTimeMillis() }?.let {
            scheduleNotification(task.title, it)
        }

        findNavController().popBackStack()
    }

    private fun scheduleNotification(taskTitle: String, triggerAtMillis: Long) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = android.net.Uri.parse("package:${requireContext().packageName}")
            }
            startActivity(intent)
            Toast.makeText(requireContext(), "⚠️ Please allow 'Exact Alarms' in settings", Toast.LENGTH_LONG).show()
            return
        }

        val intent = Intent(requireContext(), NotificationReceiver::class.java).apply {
            putExtra("title", "Task Reminder")
            putExtra("message", taskTitle)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            taskTitle.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )

        Log.d("AddTaskFragment", "Alarm scheduled for \"$taskTitle\" at $triggerAtMillis")
        Toast.makeText(requireContext(), "🔔 Reminder set for: ${dateFormat.format(Date(triggerAtMillis))}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

