package com.example.finaltodo

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finaltodo.databinding.ActivityMainBinding
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var prefs: SharedPreferences
    private var tasks = mutableListOf<Task>()
    private lateinit var adapter: TaskAdapter
    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(TaskRepostitory(this))
    }

    companion object {
        private const val KEY_TASKS = "key_tasks"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // restore tasks
        savedInstanceState?.getSerializable(KEY_TASKS)?.let {
            @Suppress("UNCHECKED_CAST")
            tasks = (it as ArrayList<Task>).toMutableList()
        }

        // theme
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        applyTheme()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        settingsDataStore = SettingsDataStore(this)
        setupThemeSwitch()
        setupRecyclerView()
        setupFab()

        taskViewModel.tasks.observe(this, Observer { tasks ->
            adapter.updateTasks(tasks)
        })
        taskViewModel.loadTasks()
    }

    override fun onSaveInstanceState(out: Bundle) {
        super.onSaveInstanceState(out)
        out.putSerializable(KEY_TASKS, ArrayList(tasks))
    }
    // Theme was able to be saved
    private fun setupThemeSwitch() {
        val themeSwitch = binding.toolbar.findViewById<SwitchCompat>(R.id.themeSwitch)
        themeSwitch.isChecked = prefs.getBoolean("dark_mode", false)
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            emptyList(),
            onDeleteClick = { task ->
                taskViewModel.deleteTask(task)
            },
            onEditClick = { task ->
                showAddEditDialog(task)
            }
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun setupFab() {
        binding.fabAddTask.setOnClickListener {
            showAddEditDialog(null)
        }
    }

    private fun showAddEditDialog(task: Task?) {
        val view = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val etTitle = view.findViewById<TextInputEditText>(R.id.editTextDialogTitle)
        val etNote  = view.findViewById<TextInputEditText>(R.id.editTextDialogNote)
        val tvDate  = view.findViewById<TextView>(R.id.textViewDate)
        val tvTime  = view.findViewById<TextView>(R.id.textViewTime)
        val dueCal  = Calendar.getInstance()

        task?.let {
            etTitle.setText(it.title)
            etNote.setText(it.description)
            it.dueDate?.let { date ->
                dueCal.time = date
                tvDate.text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
                tvTime.text = SimpleDateFormat("h:mm a",    Locale.getDefault()).format(date)
            }
        }

        view.findViewById<Button>(R.id.buttonPickDate).setOnClickListener {
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    dueCal.set(y, m, d)
                    tvDate.text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(dueCal.time)
                },
                dueCal.get(Calendar.YEAR),
                dueCal.get(Calendar.MONTH),
                dueCal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        view.findViewById<Button>(R.id.buttonPickTime).setOnClickListener {
            TimePickerDialog(
                this,
                { _, h, min ->
                    dueCal.set(Calendar.HOUR_OF_DAY, h)
                    dueCal.set(Calendar.MINUTE,      min)
                    tvTime.text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(dueCal.time)
                },
                dueCal.get(Calendar.HOUR_OF_DAY),
                dueCal.get(Calendar.MINUTE),
                false
            ).show()
        }

        AlertDialog.Builder(this)
            .setTitle(if (task == null) "Add Task" else "Edit Task")
            .setView(view)
            .setPositiveButton(if (task==null) "Add" else "Save") { _, _ ->
                val title = etTitle.text.toString().trim()
                val note  = etNote.text.toString().trim()
                if (title.isEmpty()) return@setPositiveButton

                if (task == null) {
                    val newTask = Task(title = title, description = note, completed = false, dueDate = dueCal.time)
                    taskViewModel.addTask(newTask)
                } else {
                    task.title       = title
                    task.description = note
                    task.dueDate     = dueCal.time
                    taskViewModel.updateTask(task)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun applyTheme() {
        val dm = prefs.getBoolean("dark_mode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (dm) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    // ─── Menu & Sort ───────────────────────────────────────────────────────────────

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when(item.itemId) {
            R.id.action_sort -> {
                showSortDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    private fun showSortDialog() {
        val options = arrayOf("Date", "Alphabetical", "Priority")
        AlertDialog.Builder(this)
            .setTitle("Sort Tasks By")
            .setItems(options) { _, which ->
                when(which) {
                    0 -> sortByDate()
                    1 -> sortAlphabetically()
                    2 -> sortByPriority()
                }
            }
            .show()
    }

    private fun sortByDate() {
        tasks.sortBy { it.dueDate }
        adapter.notifyDataSetChanged()
    }
    private fun sortAlphabetically() {
        tasks.sortBy { it.title }
        adapter.notifyDataSetChanged()
    }
    private fun sortByPriority() {
        tasks.sortBy { it.priority }
        adapter.notifyDataSetChanged()
    }
}
