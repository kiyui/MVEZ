package com.kiyui.timur.mvez

import android.graphics.drawable.Drawable

/**
 * Class for serializing information regarding an app
 */
class AppDetail constructor(label: CharSequence, name: CharSequence, icon: Drawable): AppBase(label, name) {
    val icon = icon
}