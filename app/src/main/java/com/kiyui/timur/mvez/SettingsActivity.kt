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

class SettingsActivity : Activity(), Observer<Action> {
    private val manager by lazy { PreferenceManager(getSharedPreferences("MVEZ", Context.MODE_PRIVATE)) }
    private val alphabetical by lazy { findViewById<Switch>(R.id.settingsAlphabetical) }
    private val mvez by lazy { findViewById<LinearLayout>(R.id.mvezSettings) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load interface
        setContentView(R.layout.activity_settings)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        // Intents
        val alphabeticalStream: Observable<Action> = RxCompoundButton
                .checkedChanges(alphabetical)
                .skip(1)
                .startWith(manager.get("alphabetical") as Boolean)
                .map { value -> Action("settings-checked", value ) }

        val mvezStream: Observable<Action> = RxView
                .clicks(mvez)
                .map { _ -> Action("app-mvez", false) }

        // Apply side-effects for intents
        Observable
                .merge(listOf(
                        alphabeticalStream,
                        mvezStream
                ))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this)
    }

    override fun onSubscribe(d: Disposable) {

    }

    override fun onNext(t: Action) {
        when (t.name) {
            "settings-checked" -> {
                val value = t.value as Boolean
                alphabetical.isChecked = value
                if (value != manager.get("alphabetical")) {
                    manager.updateState("alphabetical", value)
                }
            }
            "app-mvez" -> {
                val intent = Intent(this, MVEZActivity::class.java)
                this@SettingsActivity.startActivity(intent)
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
