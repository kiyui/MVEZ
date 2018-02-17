package com.kiyui.timur.mvez

import android.content.SharedPreferences

class PreferenceManager(val preferences: SharedPreferences) {

    fun get (key: String): Any {
        return when (key) {
            "alphabetical" -> {
                preferences.getBoolean(key, false)
            }
            else -> {
                throw Error("Invalid preference key!")
            }
        }
    }

    fun set (key: String, value: Any) {
        when (key) {
            "alphabetical" -> {
                preferences
                        .edit()
                        .putBoolean(key, value as Boolean)
                        .apply()
            }
            else -> {
                throw Error("Invalid preference key!")
            }
        }
    }
}