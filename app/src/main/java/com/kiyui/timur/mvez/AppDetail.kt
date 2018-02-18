package com.kiyui.timur.mvez

import android.graphics.drawable.Drawable

/**
 * Class for serializing information regarding an app
 */
class AppDetail constructor(label: CharSequence, name: CharSequence, val icon: Drawable): AppBase(label, name)