package com.example.TodoList.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.TodoList.databinding.FragmentCalendarBinding
import com.example.TodoList.root.TodoApplication
import com.example.TodoList.ui.calendar.MyDayViewContainer
import com.example.TodoList.viewmodel.TaskViewModel
import com.example.TodoList.viewmodel.TaskViewModelFactory
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.MonthDayBinder
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(
            requireActivity().application,
            (requireActivity().application as TodoApplication).repository
        )
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    private var selectedDate: LocalDate? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(12)
        val lastMonth = currentMonth.plusMonths(12)
        val firstDayOfWeek = java.time.DayOfWeek.SUNDAY

        binding.calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
        binding.calendarView.scrollToMonth(currentMonth)
        binding.monthYearText.text = currentMonth.format(dateFormatter)

        binding.calendarView.dayBinder = object : MonthDayBinder<MyDayViewContainer> {
            override fun create(view: View): MyDayViewContainer {
                return MyDayViewContainer(view)
            }

            override fun bind(container: MyDayViewContainer, data: CalendarDay) {
                val day = data.date
                container.date = day
                container.textView.text = day.dayOfMonth.toString()

                if (data.position == DayPosition.MonthDate) {
                    container.textView.visibility = View.VISIBLE
                    container.textView.setOnClickListener {
                        selectedDate = day
                        val millis = day.atStartOfDay().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
                        viewModel.setFilterDate(millis)
                        binding.textViewSelectedDate.text = "Selected Date: $selectedDate"
                        binding.calendarView.notifyCalendarChanged()
                    }
                } else {
                    container.textView.visibility = View.INVISIBLE
                }

                if (day == selectedDate) {
                    container.textView.setBackgroundResource(android.R.color.holo_blue_light)
                } else {
                    container.textView.setBackgroundResource(android.R.color.transparent)
                }
            }
        }

        // 🔄 Observe filteredTasks and show them
        viewModel.filteredTasks.observe(viewLifecycleOwner) { tasks ->
            if (tasks.isNotEmpty()) {
                binding.textViewTasks.text = tasks.joinToString("\n") { "• ${it.title}" }
            } else {
                binding.textViewTasks.text = "No tasks for selected date."
            }
        }

        // ✅ Add Task button click: navigate with selected date
        binding.buttonAddTask.setOnClickListener {
            selectedDate?.let { date ->
                val millis = date.atStartOfDay().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
                val action = CalendarFragmentDirections.actionCalendarFragmentToAddTaskFragment(null, millis)

                findNavController().navigate(action)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
