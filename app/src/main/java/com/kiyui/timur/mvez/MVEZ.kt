package com.kiyui.timur.mvez

/**
 * Created by timur on 11/15/17.
 */
open class MVEZ constructor(private val shortcut: String, private val application: String, private val action: String) {
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

    fun performAction () {
        println("doing $action")
    }
}