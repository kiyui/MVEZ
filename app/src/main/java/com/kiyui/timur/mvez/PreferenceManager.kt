package com.kiyui.timur.mvez

import android.content.SharedPreferences
import com.google.gson.Gson

class PreferenceManager(private val preferences: SharedPreferences) {
    private val gson: Gson = Gson()
    private val mvezPreferences by lazy {
        val savedPreferences = gson.fromJson(preferences.getString("mvez", ""), MVEZPreferences::class.java)
        when (savedPreferences) {
            null -> MVEZPreferences()
            else -> savedPreferences
        }
    }

    fun get (key: String): Any {
        return when (key) {
            "alphabetical" -> {
                preferences.getBoolean(key, false)
            }
            "mvez" -> {
                mvezPreferences
            }
            else -> {
                throw Error("Invalid preference key!")
            }
        }
    }

    fun set (key: String, value: Any): Boolean {
        return when (key) {
            "alphabetical" -> {
                preferences
                        .edit()
                        .putBoolean(key, value as Boolean)
                        .apply()
                true
            }
            "mvez-add" -> {
                val success = mvezPreferences.add(value as MVEZ)
                if (success) {
                    preferences
                            .edit()
                            .putString("mvez", gson.toJson(mvezPreferences))
                            .apply()
                }
                success
            }
            "mvez-remove" -> {
                mvezPreferences.remove(value as Int)
                true
            }
            else -> {
                throw Error("Invalid preference key!")
                false
            }
        }
    }
}