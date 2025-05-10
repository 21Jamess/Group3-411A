package com.example.finaltodo

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.appcompat.widget.SwitchCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finaltodo.api.QuoteExecutor
import com.example.finaltodo.databinding.ActivityMainBinding
import com.google.android.material.textfield.TextInputEditText
import org.w3c.dom.Text
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var settingsDataStore:SettingsDataStore
    private lateinit var prefs: SharedPreferences
    private lateinit var adapter: TaskAdapter

    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(TaskRepository(this))
    }

    companion object {
        private const val KEY_TASKS = "key_tasks"
    }

    override fun attachBaseContext(newBase: Context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(newBase)
        val lang = prefs.getString("app_locale", Locale.getDefault().language) ?: "en"
        val contextWrapped = LocaleHelper.wrap(newBase, lang)
        super.attachBaseContext(contextWrapped)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
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

        val textViewQuote = findViewById<TextView>(R.id.textViewQuote)
        val quoteExecutor = QuoteExecutor()
        quoteExecutor.fetchQuote().observe(this) { quote ->
            if (quote != null) {
                textViewQuote.text = "\"${quote.q}\"\n- ${quote.a}"
            }
            else {
                textViewQuote.text = "Failed to load quote."
            }
        }
    }

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
                tvTime.text = SimpleDateFormat("h:mm a",      Locale.getDefault()).format(date)
            }
        }

        view.findViewById<Button>(R.id.buttonPickDate).setOnClickListener {
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    dueCal.set(y, m, d)
                    tvDate.text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                        .format(dueCal.time)
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
                    tvTime.text = SimpleDateFormat("h:mm a", Locale.getDefault())
                        .format(dueCal.time)
                },
                dueCal.get(Calendar.HOUR_OF_DAY),
                dueCal.get(Calendar.MINUTE),
                false
            ).show()
        }

        AlertDialog.Builder(this)
            .setTitle(
                if (task == null) getString(R.string.dialog_title_add)
                else getString(R.string.dialog_title_edit)
            )
            .setView(view)
            .setPositiveButton(
                if (task == null) getString(R.string.button_add)
                else getString(R.string.button_save)
            ) { _, _ ->
                val title = etTitle.text.toString().trim()
                val note = etNote.text.toString().trim()
                if (title.isEmpty()) return@setPositiveButton

                if (task == null) {
                    val newTask = Task(
                        title     = title,
                        description = note,
                        completed = false,
                        dueDate   = dueCal.time
                    )
                    taskViewModel.addTask(newTask)
                    Log.i("FinalTodoApp", "Task Added: $title")
                } else {
                    task.title       = title
                    task.description = note
                    task.dueDate     = dueCal.time
                    taskViewModel.updateTask(task)
                    Log.i("FinalTodoApp", "Task Edited: $title")
                }
            }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }

    private fun applyTheme() {
        val dark = prefs.getBoolean("dark_mode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (dark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_language -> {
            showLanguageDialog(); true
        }
        R.id.action_sort -> {
            showSortDialog(); true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun showLanguageDialog() {
        val locales     = arrayOf("English", "Español", "Tiếng Việt")
        val localeCodes = arrayOf("en", "es", "vi")
        AlertDialog.Builder(this)
            .setTitle(R.string.change_language)
            .setItems(locales) { _, which ->
                prefs.edit().putString("app_locale", localeCodes[which]).apply()
                recreate()
            }
            .show()
    }

    private fun showSortDialog() {
        val opts = arrayOf(R.string.sort_date, R.string.sort_alpha, R.string.sort_priority)
        AlertDialog.Builder(this)
            .setTitle(R.string.sort_tasks_by)
            .setItems(opts.map { getString(it) }.toTypedArray()) { _, i ->
                when (i) {
                    0 -> sortByDate()
                    1 -> sortAlphabetically()
                    2 -> sortByPriority()
                }
            }
            .show()
    }

    private fun sortByDate() = taskViewModel.sortTasksByDate()
    private fun sortAlphabetically() = taskViewModel.sortTasksAlphabetically()
    private fun sortByPriority() = taskViewModel.sortTasksByPriority()
}
