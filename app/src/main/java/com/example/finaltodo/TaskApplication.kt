package com.example.finaltodo

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale

class TaskApplication : Application() {
    private lateinit var settingsDataStore: SettingsDataStore

    companion object {
        private var instance: TaskApplication? = null

        fun getLanguage(context: Context): String {
            try {
                val settingsDataStore = SettingsDataStore(context.applicationContext)
                return runBlocking {
                    settingsDataStore.languageFlow.first()
                }
            } catch (e: Exception) {
                return SettingsDataStore.DEFAULT_LANGUAGE
            }
        }

        suspend fun setLanguage(context: Context, language: String) {
            val settingsDataStore = SettingsDataStore(context.applicationContext)
            settingsDataStore.saveLanguageSetting(language)
        }

        // For compatibility with synchronous code
        fun setLanguageSync(context: Context, language: String) {
            runBlocking {
                setLanguage(context.applicationContext, language)
            }
        }
    }

    override fun attachBaseContext(base: Context) {
        // Initialize with a language for the base context
        val language = try {
            val store = SettingsDataStore(base)
            runBlocking { store.languageFlow.first() }
        } catch (e: Exception) {
            SettingsDataStore.DEFAULT_LANGUAGE
        }

        super.attachBaseContext(LocaleHelper.setLocale(base, language))
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        settingsDataStore = SettingsDataStore(applicationContext)

        // Apply language configuration
        val language = runBlocking { settingsDataStore.languageFlow.first() }
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