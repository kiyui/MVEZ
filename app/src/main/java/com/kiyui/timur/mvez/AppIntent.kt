package com.kiyui.timur.mvez

import android.content.Intent

/**
 * Class for serializing information of an app with action
 */
class AppIntent constructor(label: CharSequence, name: CharSequence, val action: String): AppBase(label, name)