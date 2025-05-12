package com.example.finaltodo

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import java.util.Locale
import com.example.finaltodo.R
import com.example.finaltodo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var prefs: SharedPreferences
    private lateinit var navController: NavController
    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(TaskRepostitory(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved language
        val language = TaskApplication.getLanguage(this)
        val config = resources.configuration
        val locale = Locale(language)
        Locale.setDefault(locale)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        // theme
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
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

        settingsDataStore = SettingsDataStore(this)
        setupThemeSwitch()

        // Load tasks once at the beginning
        taskViewModel.loadTasks()

        Log.i("MainActivity", "Activity created, navigation set up")
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
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

                // Save language preference
                TaskApplication.setLanguage(this, language)

                // Apply language change immediately
                val config = resources.configuration
                val locale = Locale(language)
                Locale.setDefault(locale)
                config.setLocale(locale)
                resources.updateConfiguration(config, resources.displayMetrics)

                // Restart activity to apply changes
                recreate()
            }
            .show()
    }

    override fun attachBaseContext(newBase: Context) {
        val language = TaskApplication.getLanguage(newBase)
        super.attachBaseContext(LocaleHelper.setLocale(newBase, language))
    }
}
