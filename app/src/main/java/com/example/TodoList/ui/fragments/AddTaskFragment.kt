package com.example.TodoList.ui.fragments

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.TodoList.database.TaskDatabase
import com.example.TodoList.databinding.FragmentAddTaskBinding
import com.example.TodoList.model.Task
import com.example.TodoList.notifications.NotificationReceiver
import com.example.TodoList.repository.TaskRepository
import com.example.TodoList.util.NotificationHelper
import com.example.TodoList.viewmodel.TaskViewModel
import com.example.TodoList.viewmodel.TaskViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class AddTaskFragment : Fragment() {

    private var _binding: FragmentAddTaskBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskViewModel: TaskViewModel
    private val args: AddTaskFragmentArgs by navArgs()
    private var currentTask: Task? = null

    private val priorities = listOf("Low", "Medium", "High")
    private var dueDate: Long? = null
    private val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
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
        val dueDateArg = args.dueDate // 🆕 Optional dueDate passed from Calendar

        if (currentTask != null) {
            // 📝 Editing existing task
            currentTask?.let { task ->
                binding.editTextTitle.setText(task.title)
                binding.editTextDescription.setText(task.description)
                binding.checkboxCompleted.isChecked = task.isCompleted
                binding.spinnerPriority.setSelection(priorities.indexOf(task.priority))
                task.dueDate?.let {
                    dueDate = it
                    binding.textViewDueDate.text = dateFormat.format(Date(it))
                }
            }
        } else if (dueDateArg != 0L.toLong()) {
            // 📅 Add task with pre-filled date from CalendarFragment
            dueDate = dueDateArg
            binding.textViewDueDate.text = dateFormat.format(Date(dueDateArg))
        }

        binding.textViewDueDate.setOnClickListener {
            openDateTimePicker()
        }

        binding.buttonSaveTask.setOnClickListener {
            saveTask()
        }
    }

    private fun openDateTimePicker() {
        val calendar = Calendar.getInstance()
        dueDate?.let { calendar.timeInMillis = it }

        DatePickerDialog(requireContext(), { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)

            TimePickerDialog(requireContext(), { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)

                dueDate = calendar.timeInMillis
                binding.textViewDueDate.text = dateFormat.format(calendar.time)
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
        val priority = binding.spinnerPriority.selectedItem.toString()

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter both title and description", Toast.LENGTH_SHORT).show()
            return
        }

        val task = if (currentTask != null) {
            currentTask!!.copy(
                title = title,
                description = description,
                isCompleted = isCompleted,
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
                isCompleted = isCompleted,
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

        Log.d("AddTaskFragment", "✅ Alarm scheduled for \"$taskTitle\" at $triggerAtMillis")
        Toast.makeText(requireContext(), "🔔 Reminder set for: ${dateFormat.format(Date(triggerAtMillis))}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
