package com.kiyui.timur.mvez

import android.content.SharedPreferences
import com.google.gson.Gson

class PreferenceManager(private val preferences: SharedPreferences) {
    private val gson: Gson = Gson()

    fun get (key: String): Any {
        return when (key) {
            "alphabetical" -> {
                preferences.getBoolean(key, false)
            }
            "mvez" -> {
                val savedPreferences = gson.fromJson(preferences.getString("mvez", ""), MVEZPreferences::class.java)
                when (savedPreferences) {
                    null -> MVEZPreferences()
                    else -> savedPreferences
                }
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
            "mvez-add" -> {
                val savedPreferences = gson.fromJson(preferences.getString("mvez", ""), MVEZPreferences::class.java)
                val mvezPreferences = when (savedPreferences) {
                    null -> MVEZPreferences()
                    else -> savedPreferences
                }

                mvezPreferences.add(value as MVEZ)
                preferences
                        .edit()
                        .putString("mvez", gson.toJson(mvezPreferences))
                        .apply()
            }
            else -> {
                throw Error("Invalid preference key!")
            }
        }
    }
}