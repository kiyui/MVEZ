package com.kiyui.timur.mvez

import java.util.*

/**
 * Created by timur on 11/15/17.
 */
class MVEZPreferences {
    private val preferences: Vector<MVEZ> = Vector()
    fun add (preference: MVEZ) {
        preferences.add(preference)
    }

    fun getLabels (): List<String> {
        return preferences.map { mvez -> mvez.toString() }
    }
}