package com.kiyui.timur.mvez

import java.util.*

/**
 * Created by timur on 11/15/17.
 */
class MVEZPreferences {
    private val preferences: Vector<MVEZ> = Vector()
    fun add (mvez: MVEZ): Boolean {
        if (!preferences.fold(false, { acc, preference -> acc || preference.equals(mvez) })) {
            preferences.add(mvez)
            return true
        }
        return false
    }

    fun remove (index: Int): Boolean {
        if (index > -1 && index < preferences.size) {
            preferences.removeAt(index)
            return true
        }
        return false
    }

    fun getLabels (): List<String> {
        return preferences.map { mvez -> mvez.toString() }
    }

    fun getActionableMVEZ (query: String): MVEZ? {
       return preferences.findLast { preference ->
           val stripped = preference.stripShortcut(query)
           stripped != query
       }
    }
}