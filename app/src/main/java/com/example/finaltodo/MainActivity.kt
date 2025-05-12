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
import java.util.Locale
import com.example.finaltodo.R
import com.example.finaltodo.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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
        // Use a try block to handle initialization
        val isDarkMode = try {
            // It's acceptable to use runBlocking here during initial UI setup
            runBlocking { settingsDataStore.darkModeFlow.first() }
        } catch (e: Exception) {
            false // Default to light mode if there's an exception
        }

        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        // Start async observation of DataStore
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
            getString(R.string.sort_alphabetical),
            getString(R.string.sort_priority)
        )
        AlertDialog.Builder(this)
            .setTitle(R.string.sort_tasks_by)
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
        taskViewModel.sortTasksByDate()
    }

    private fun sortAlphabetically() {
        taskViewModel.sortTasksAlphabetically()
    }

    private fun sortByPriority() {
        taskViewModel.sortTasksByPriority()
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "Español", "Tiếng Việt")
        val languageCodes = arrayOf("en", "es", "vi")

        AlertDialog.Builder(this)
            .setTitle(R.string.choose_language)
            .setItems(languages) { _, which ->
                // Change language
                val language = languageCodes[which]

                // Save language preference using DataStore
                lifecycleScope.launch {
                    TaskApplication.setLanguage(this@MainActivity, language)

                    // Apply language change immediately
                    val config = resources.configuration
                    val locale = Locale(language)
                    Locale.setDefault(locale)
                    config.setLocale(locale)
                    resources.updateConfiguration(config, resources.displayMetrics)

                    // Restart activity to apply changes
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
