package com.kiyui.timur.mvez

import android.content.Context
import android.widget.ArrayAdapter

/**
 * Created by timur on 2/18/18.
 */
class AppIntentAdapter(context: Context, private val apps: List<AppIntent>):
        ArrayAdapter<String>(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                apps.map { appIntent -> appIntent.label.toString() }) {

    fun getItemAtPosition (position: Int): AppIntent {
        return apps[position]
    }
}