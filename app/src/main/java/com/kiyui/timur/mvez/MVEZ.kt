package com.kiyui.timur.mvez

/**
 * Created by timur on 11/15/17.
 */
open class MVEZ constructor(private val shortcut: String, val application: String, val action: String) {
    fun isNotEmpty(): Boolean {
        return shortcut.isNotEmpty() && application.isNotEmpty()
    }

    override fun toString(): String {
        return when (shortcut) {
            "" -> {
                "<no shortcut assigned>: $application ($action)"
            }
            else -> {
                "<$shortcut>: $application ($action)"
            }
        }
    }

    fun equals(other: MVEZ): Boolean {
        return this.toString() == other.toString()
    }

    fun stripShortcut (query: String): String {
        return query.replace(Regex("^!$shortcut\\s*|\\s*!$shortcut\\s*$"), "")
    }
}