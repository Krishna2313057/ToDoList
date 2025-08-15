package com.example.todolist.ui.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.R
import com.example.todolist.databinding.ItemTaskBinding
import com.example.todolist.model.Priority
import com.example.todolist.model.Task
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskAdapter(
    private val onDeleteClick: (Task) -> Unit,
    private val onItemClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(oldItem: Task, newItem: Task) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Task, newItem: Task) =
                oldItem == newItem
        }
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) = with(binding) {
            textViewTitle.text = task.title
            textViewDescription.text = task.description
            textViewPriority.text = task.priority.name


            textViewDueDate.text = task.dueDate?.let {
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(it))
            } ?: "No due date"


            checkBoxCompleted.isChecked = task.completed


            fun applyStrike(tv: android.widget.TextView) {
                tv.paintFlags = if (task.completed)
                    (tv.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG)
                else
                    (tv.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv())
            }
            applyStrike(textViewTitle)
            applyStrike(textViewDescription)


            val colorRes = when (task.priority) {
                Priority.HIGH -> R.color.priority_high
                Priority.MEDIUM -> R.color.priority_medium
                Priority.LOW -> R.color.priority_low
            }
            textViewPriority.setTextColor(
                ContextCompat.getColor(root.context, colorRes)
            )


            buttonDelete.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) onDeleteClick(task)
            }

            root.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) onItemClick(task)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
