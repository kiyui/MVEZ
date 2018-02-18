package com.kiyui.timur.mvez

import android.os.Bundle
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.*
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxCompoundButton
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : Activity(), Observer<Action> {
    private lateinit var adapter: ArrayAdapter<String>
    private val manager by lazy { PreferenceManager(getSharedPreferences("MVEZ", Context.MODE_PRIVATE)) }
    private val mvezPreferences by lazy { manager.get("mvez") as MVEZPreferences }
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
        adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                mvezPreferences.getLabels().toMutableList())

        // Initialize lists
        val searchApps = getAppsForIntent(Intent.ACTION_WEB_SEARCH) + getAppsForIntent(Intent.ACTION_SEARCH)
        val appIntentAdapter = AppIntentAdapter(this, searchApps)
        appSpinner.adapter = appIntentAdapter
        bangList.adapter = adapter

        // Intents
        val alphabeticalStream: Observable<Action> = RxCompoundButton
                .checkedChanges(alphabetical)
                .skip(1)
                .startWith(manager.get("alphabetical") as Boolean)
                .map { value -> Action("settings-checked", value ) }

        val createBangStream: Observable<Action> = RxView
                .clicks(bangButton)
                .map { _ ->
                    val appIntent = appIntentAdapter.getItemAtPosition(searchSpinner.selectedItemPosition)
                    val mvez = MVEZ(bangText.text.toString(), appIntent.name.toString(), appIntent.action)
                    Action("settings-mvez-add", mvez )
                }
                .filter{ action -> (action.value as MVEZ).isNotEmpty() }

        // Apply side-effects for intents
        Observable
                .merge(listOf(
                        alphabeticalStream,
                        createBangStream
                ))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this)
    }

    private fun getAppsForIntent(action: String): List<AppIntent> {
        val intent = Intent()
        intent.action = action
        return packageManager
                .queryIntentActivities(intent, 0)
                .map { ri ->
                    val label = ri.loadLabel(packageManager)
                    val name = ri.activityInfo.packageName
                    AppIntent(label, name, action) }
    }

    override fun onSubscribe(d: Disposable) {

    }

    override fun onNext(t: Action) {
        when (t.name) {
            "settings-checked" -> {
                val value = t.value as Boolean
                alphabetical.isChecked = value
                if (value != manager.get("alphabetical")) {
                    manager.set("alphabetical", value)
                }
            }
            "settings-mvez-add" -> {
                val mvez = t.value as MVEZ
                manager.set("mvez-add", mvez)
                adapter.add(mvez.toString())
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
