package com.example.finaltodo

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.preference.PreferenceManager
import com.example.finaltodo.R
import java.util.Locale

class TaskApplication : Application() {

    companion object {
        private const val LANGUAGE_KEY = "language_setting"
        private const val DEFAULT_LANGUAGE = "en"

        fun getLanguage(context: Context): String {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return prefs.getString(LANGUAGE_KEY, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
        }

        fun setLanguage(context: Context, language: String) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit().putString(LANGUAGE_KEY, language).apply()
        }
    }

    override fun attachBaseContext(base: Context) {
        val language = getLanguage(base)
        super.attachBaseContext(LocaleHelper.setLocale(base, language))
    }

    override fun onCreate() {
        super.onCreate()

        // Apply saved language
        val language = getLanguage(this)

        // Apply language configuration
        val config = resources.configuration
        val locale = Locale(language)
        Locale.setDefault(locale)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val language = getLanguage(this)
        LocaleHelper.setLocale(this, language)
    }
}