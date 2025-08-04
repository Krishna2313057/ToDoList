package com.example.TodoList.ui.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.TodoList.R
import com.example.TodoList.databinding.ItemTaskBinding
import com.example.TodoList.model.Task

class TaskAdapter(
    private val onDeleteClick: (Task) -> Unit,
    private val onItemClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) = with(binding) {
            textViewTitle.text = task.title
            textViewDescription.text = task.description
            textViewPriority.text = task.priority

            // Apply strike-through if task is completed
            val paintFlags = if (task.isCompleted) Paint.STRIKE_THRU_TEXT_FLAG else 0
            textViewTitle.paintFlags = paintFlags
            textViewDescription.paintFlags = paintFlags

            // Set priority text color
            val priorityColor = when (task.priority.lowercase()) {
                "high" -> R.color.priority_high
                "medium" -> R.color.priority_medium
                "low" -> R.color.priority_low
                else -> android.R.color.darker_gray
            }
            textViewPriority.setTextColor(
                ContextCompat.getColor(root.context, priorityColor)
            )

            // Handle delete icon click
            imageViewDelete.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onDeleteClick(task)
                }
            }

            // Handle item click
            root.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(task)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
