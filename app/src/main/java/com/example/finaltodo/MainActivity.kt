package com.example.finaltodo

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.finaltodo.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var navController: NavController
    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(TaskRepostitory(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize settings data store first
        settingsDataStore = SettingsDataStore(this)

        // Apply saved language
        val language = TaskApplication.getLanguage(this)
        val config = resources.configuration
        val locale = Locale(language)
        Locale.setDefault(locale)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        // Apply theme
        applyTheme()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // Set up Navigation Controller
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        setupActionBarWithNavController(navController)

        // Hide Sort icon on Add/Edit screens
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val menu = binding.toolbar.menu
            val isAddEdit = destination.id == R.id.addEditTaskFragment
            menu.findItem(R.id.action_sort)?.isVisible = !isAddEdit
        }

        setupThemeSwitch()

        // Load tasks once at the beginning
        taskViewModel.loadTasks()

        Log.i("MainActivity", "Activity created, navigation set up")
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    // Theme switch using DataStore
    private fun setupThemeSwitch() {
        val themeSwitch = binding.toolbar.findViewById<SwitchCompat>(R.id.themeSwitch)

        // Set initial state from DataStore
        lifecycleScope.launch {
            themeSwitch.isChecked = settingsDataStore.darkModeFlow.first()
        }

        // Listen for changes and update
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                settingsDataStore.saveDarkModeSetting(isChecked)
                AppCompatDelegate.setDefaultNightMode(
                    if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
            }
        }

        // Observe changes from DataStore
        lifecycleScope.launch {
            settingsDataStore.darkModeFlow.collectLatest { isDarkMode ->
                if (themeSwitch.isChecked != isDarkMode) {
                    themeSwitch.isChecked = isDarkMode
                }
            }
        }
    }

    private fun applyTheme() {
        val isDarkMode = try {
            runBlocking { settingsDataStore.darkModeFlow.first() }
        } catch (e: Exception) {
            false
        }

        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        lifecycleScope.launch {
            settingsDataStore.darkModeFlow.collect { darkModeEnabled ->
                AppCompatDelegate.setDefaultNightMode(
                    if (darkModeEnabled) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
            }
        }
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
            R.id.action_language -> {
                showLanguageDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    private fun showSortDialog() {
        val options = arrayOf(
            getString(R.string.sort_date),
            getString(R.string.sort_alphabetical)
        )
        AlertDialog.Builder(this)
            .setTitle(R.string.sort_tasks_by)
            .setItems(options) { _, which ->
                when(which) {
                    0 -> sortByDate()
                    1 -> sortAlphabetically()
                }
            }
            .show()
    }

    private fun sortByDate() {
        taskViewModel.sortTasksByDate()
    }

    private fun sortAlphabetically() {
        taskViewModel.sortTasksAlphabetically()
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "Español", "Tiếng Việt")
        val languageCodes = arrayOf("en", "es", "vi")

        AlertDialog.Builder(this)
            .setTitle(R.string.choose_language)
            .setItems(languages) { _, which ->
                val lang = languageCodes[which]
                lifecycleScope.launch {
                    TaskApplication.setLanguage(this@MainActivity, lang)
                    val cfg = resources.configuration
                    val loc = Locale(lang)
                    Locale.setDefault(loc)
                    cfg.setLocale(loc)
                    resources.updateConfiguration(cfg, resources.displayMetrics)
                    recreate()
                }
            }
            .show()
    }

    override fun attachBaseContext(newBase: Context) {
        val language = TaskApplication.getLanguage(newBase)
        super.attachBaseContext(LocaleHelper.setLocale(newBase, language))
    }
}
