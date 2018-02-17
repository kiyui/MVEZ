package com.kiyui.timur.mvez

import android.os.Bundle
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.*
import com.jakewharton.rxbinding2.widget.RxCompoundButton
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

class SettingsActivity : Activity(), Observer<Action> {
    private val alphabetical by lazy { findViewById<CheckBox>(R.id.settingsAlphabetical) }
    private val appSpinner by lazy { findViewById<Spinner>(R.id.searchSpinner) }
    private val bangText by lazy { findViewById<EditText>(R.id.bangValue) }
    private val bangButton by lazy { findViewById<Button>(R.id.bangSubmit) }
    private val bangList by lazy { findViewById<ListView>(R.id.bangList) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load interface
        setContentView(R.layout.activity_settings)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize activity
        val searchApps = getWebSearchIntents() + getSearchIntents()
        val searchAppNames = searchApps.map { app -> app.label.toString() }

        // Application preferences
        val preferences = getSharedPreferences("MVEZ", Context.MODE_PRIVATE)
        val manager = PreferenceManager(preferences)

        appSpinner.adapter = ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                searchAppNames)

        // Intents
        val alphabeticalStream: Observable<Action> = RxCompoundButton
                .checkedChanges(alphabetical)
                .skip(1)
                .startWith(manager.get("alphabetical") as Boolean)
                .map { value -> Action("settings-checked", value ) }

        // Apply side-effects for intents
        Observable
                .merge(listOf(
                        alphabeticalStream
                ))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this)
    }

    /**
     * Get applications that implement a web search intent
     */
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

    /**
     * Get applications that implement a general search intent
     */
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

    override fun onSubscribe(d: Disposable) {

    }

    override fun onNext(t: Action) {
        val manager = PreferenceManager(getSharedPreferences("MVEZ", Context.MODE_PRIVATE))
        when (t.name) {
            "settings-checked" -> {
                val value = t.value as Boolean
                alphabetical.isChecked = value
                if (value != manager.get("alphabetical")) {
                    manager.set("alphabetical", value)
                }
            }
            else -> {
                val type = t.name
                println("Unknown intent of type $type")
            }
        }
    }

    override fun onError(e: Throwable) {

    }

    override fun onComplete() {

    }
}
