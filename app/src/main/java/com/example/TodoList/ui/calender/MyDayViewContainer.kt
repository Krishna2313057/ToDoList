package com.example.TodoList.ui.calendar

import android.view.View
import android.widget.TextView
import com.example.TodoList.R
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate

class MyDayViewContainer(view: View) : ViewContainer(view) {
    
    val textView: TextView = view.findViewById(R.id.calendarDayText)
    var date: LocalDate? = null
}

