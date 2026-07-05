package com.example.data

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ourin_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_BASE_URL = "key_base_url"
        private const val DEFAULT_BASE_URL = "https://aiodownloader.eu.cc"
        private const val KEY_USERNAME = "key_username"
        private const val DEFAULT_USERNAME = "Anonymous"
        private const val KEY_COOKIES = "key_cookies"
        private const val DEFAULT_COOKIES = ""
    }

    var baseUrl: String
        get() = prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        set(value) {
            prefs.edit().putString(KEY_BASE_URL, value.trim()).apply()
        }

    var username: String
        get() = prefs.getString(KEY_USERNAME, DEFAULT_USERNAME) ?: DEFAULT_USERNAME
        set(value) {
            prefs.edit().putString(KEY_USERNAME, value.trim()).apply()
        }

    var cookies: String
        get() = prefs.getString(KEY_COOKIES, DEFAULT_COOKIES) ?: DEFAULT_COOKIES
        set(value) {
            prefs.edit().putString(KEY_COOKIES, value.trim()).apply()
        }
}
