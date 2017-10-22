package com.kiyui.timur.mvez

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.GridView
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageButton
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxAdapterView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers

class SearchActivity: Activity(), Observer<Action> {
    private lateinit var adapter: AppDetailAdapter
    private lateinit var appGrid: GridView
    private lateinit var search: EditText
    private lateinit var clear: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load interface
        setContentView(R.layout.activity_search)
        window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Initialize application
        var apps: List<AppDetail> = getApps()

        // We initialize out adapter with a mutable list since we need to modify it when searching
        adapter = AppDetailAdapter(this, R.layout.app_item, apps.toMutableList())
        appGrid.adapter = adapter
    }

    /**
     * Get applications in alphabetical order
     */
    private fun getApps (): List<AppDetail> {
        // Get all intents with `CATEGORY_LAUNCHER` category
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(intent, 0)
                .map { ri ->
                    val label = ri.loadLabel(packageManager)
                    val name = ri.activityInfo.packageName
                    val icon = ri.activityInfo.loadIcon(packageManager)
                    AppDetail(label, name, icon) }
                .filter { app -> app.name != packageName }
                .sortedWith(compareBy({it.label as String}))
    }
}