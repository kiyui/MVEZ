package com.kiyui.timur.mvez

import android.graphics.drawable.Drawable

/**
 * Class for serializing information regarding an app
 */
class AppDetail constructor(label: CharSequence, name: CharSequence, icon: Drawable) {
    val label = label
    val name = name
    val icon = icon
}