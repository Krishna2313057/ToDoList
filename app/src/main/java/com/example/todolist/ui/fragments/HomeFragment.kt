package com.example.todolist.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todolist.R
import com.example.todolist.database.TaskDatabase
import com.example.todolist.databinding.FragmentHomeBinding
import com.example.todolist.repository.TaskRepository
import com.example.todolist.ui.adapter.TaskAdapter
import com.example.todolist.viewmodel.TaskFilter
import com.example.todolist.viewmodel.TaskViewModel
import com.example.todolist.viewmodel.TaskViewModelFactory

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskViewModel: TaskViewModel
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.title = "Tasks"

        val application = requireNotNull(activity).application
        val dao = TaskDatabase.getDatabase(application).taskDao()
        val repository = TaskRepository(dao)
        val factory = TaskViewModelFactory(application, repository)
        taskViewModel = ViewModelProvider(this, factory)[TaskViewModel::class.java]

        taskAdapter = TaskAdapter(
            onDeleteClick = { task ->
                taskViewModel.deleteTask(task)
                Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show()
            },
            onItemClick = { task ->

                val action = HomeFragmentDirections.actionHomeFragmentToAddTaskFragment(task, null)
                findNavController().navigate(action)
            }
        )

        binding.recyclerViewTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }

        taskViewModel.filteredTasks.observe(viewLifecycleOwner) { tasks ->
            taskAdapter.submitList(tasks)
            binding.textViewEmpty.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabAddTask.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToAddTaskFragment(null, null)
            findNavController().navigate(action)
        }

        setupMenu()
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_home, menu)

                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as SearchView
                searchView.queryHint = "Search tasks..."

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        taskViewModel.setSearchQuery(query.orEmpty())
                        taskViewModel.setFilter(TaskFilter.ALL)
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        taskViewModel.setSearchQuery(newText.orEmpty())
                        taskViewModel.setFilter(TaskFilter.ALL)
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                fun clearSearchAndSetFilter(filter: TaskFilter): Boolean {
                    taskViewModel.setSearchQuery("")
                    taskViewModel.setFilter(filter)
                    return true
                }

                return when (menuItem.itemId) {
                    R.id.action_toggle_theme -> {
                        toggleDarkMode()
                        true
                    }
                    R.id.sort_by_priority -> clearSearchAndSetFilter(TaskFilter.SORT_PRIORITY)
                    R.id.sort_by_due_date -> clearSearchAndSetFilter(TaskFilter.SORT_BY_DUE_DATE)
                    R.id.filter_completed -> clearSearchAndSetFilter(TaskFilter.COMPLETED)
                    R.id.filter_incomplete -> clearSearchAndSetFilter(TaskFilter.INCOMPLETE)
                    R.id.action_sort_filter -> clearSearchAndSetFilter(TaskFilter.ALL)
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun toggleDarkMode() {
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        AppCompatDelegate.setDefaultNightMode(
            if (currentMode == AppCompatDelegate.MODE_NIGHT_YES)
                AppCompatDelegate.MODE_NIGHT_NO
            else
                AppCompatDelegate.MODE_NIGHT_YES
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
