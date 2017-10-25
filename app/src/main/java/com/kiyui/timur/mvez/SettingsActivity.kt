package com.kiyui.timur.mvez

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.widget.*

class SettingsActivity : Activity() {
    private lateinit var alphabetical: CheckBox
    private lateinit var appSpinner: Spinner
    private lateinit var bangText: EditText
    private lateinit var bangButton: Button
    private lateinit var bangList: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load interface
        setContentView(R.layout.activity_settings)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize activity
        val searchApps = getWebSearchIntents() + getSearchIntents()
        val searchAppNames = searchApps.map { app -> app.label.toString() }

        // View items
        alphabetical = findViewById(R.id.settingsAlphabetical)
        appSpinner = findViewById(R.id.searchSpinner)
        bangText = findViewById(R.id.bangValue)
        bangButton = findViewById(R.id.bangSubmit)
        bangList = findViewById(R.id.bangList)

        appSpinner.adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, searchAppNames)
    }

    private fun getWebSearchIntents (): List<AppBase> {
        val intent = Intent()
        intent.action = Intent.ACTION_WEB_SEARCH
        return packageManager
                .queryIntentActivities(intent, 0)
                .map { ri ->
                    val label = ri.loadLabel(packageManager)
                    val name = ri.activityInfo.packageName
                    AppBase(label, name) }
    }

    private fun getSearchIntents (): List<AppBase> {
        val intent = Intent()
        intent.action = Intent.ACTION_SEARCH
        return packageManager
                .queryIntentActivities(intent, 0)
                .map { ri ->
                    val label = ri.loadLabel(packageManager)
                    val name = ri.activityInfo.packageName
                    AppBase(label, name) }
    }


}
