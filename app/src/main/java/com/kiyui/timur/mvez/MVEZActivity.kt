package com.kiyui.timur.mvez

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxAdapterView
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_mvez.*

class MVEZActivity : Activity(), Observer<Action> {
    private lateinit var adapter: ArrayAdapter<String>
    private val manager by lazy { PreferenceManager(getSharedPreferences("MVEZ", Context.MODE_PRIVATE)) }
    private val mvezPreferences by lazy { manager.get("mvez") as MVEZPreferences }
    private val mvezPreferenceList by lazy { mvezPreferences.getLabels().toMutableList() }
    private val appSpinner by lazy { findViewById<Spinner>(R.id.searchSpinner) }
    private val bangText by lazy { findViewById<EditText>(R.id.bangValue) }
    private val bangButton by lazy { findViewById<Button>(R.id.bangSubmit) }
    private val bangList by lazy { findViewById<ListView>(R.id.bangList) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load interface
        setContentView(R.layout.activity_mvez)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize activity
        adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                mvezPreferenceList)

        // Initialize lists
        val searchApps = getAppsForIntent(Intent.ACTION_WEB_SEARCH) + getAppsForIntent(Intent.ACTION_SEARCH)
        val appIntentAdapter = AppIntentAdapter(this, searchApps)
        appSpinner.adapter = appIntentAdapter
        bangList.adapter = adapter

        // Intents
        val createBangStream: Observable<Action> = RxView
                .clicks(bangButton)
                .map { _ ->
                    val appIntent = appIntentAdapter.getItemAtPosition(searchSpinner.selectedItemPosition)
                    val mvez = MVEZ(bangText.text.toString(), appIntent.name.toString(), appIntent.action)
                    Action("settings-mvez-add", mvez )
                }
                .filter{ action -> (action.value as MVEZ).isNotEmpty() }

        val deleteBangStream: Observable<Action> = RxAdapterView
                .itemLongClickEvents(bangList)
                .map { item -> Action("settings-mvez-remove", item.position()) }

        // Apply side-effects for intents
        Observable
                .merge(listOf(
                        createBangStream,
                        deleteBangStream
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
            "settings-mvez-add" -> {
                val mvez = t.value as MVEZ
                val success = manager.updateState("mvez-add", mvez)
                when (success) {
                    true -> {
                        mvezPreferenceList.add(mvez.toString())
                        adapter.notifyDataSetChanged()
                    }
                    false -> {
                        Toast.makeText(
                                this,
                                "Failed to add duplicate mvez",
                                Toast.LENGTH_SHORT).show()
                    }
                }
            }
            "settings-mvez-remove" -> {
                val index = t.value as Int
                manager.updateState("mvez-remove", index)
                mvezPreferenceList.removeAt(index)
                adapter.notifyDataSetChanged()
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
