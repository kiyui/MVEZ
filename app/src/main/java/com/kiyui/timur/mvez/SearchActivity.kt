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

/**
 * Main launcher application handling all application logic
 * All application intents are in the form of `Observable<Action>`
 * We implement `Observer<Action>` to handle all side effects reactively
 */
class SearchActivity: Activity(), Observer<Action> {
    private lateinit var adapter: AppDetailAdapter
    private lateinit var appGrid: GridView
    private lateinit var search: EditText
    private lateinit var clear: ImageButton
    private lateinit var packageSource: PackageChangeSource
    private val filter = IntentFilter()

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

        // View items
        appGrid = findViewById(R.id.appsContainer)
        search = findViewById(R.id.action_search)
        clear = findViewById(R.id.clear_button)

        // Create an app change broadcast receiver so we have a source
        // for when an application is installed/uninstalled/updated
        filter.addAction(Intent.ACTION_PACKAGE_ADDED)
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED)
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED)
        filter.addDataScheme("package")

        packageSource = PackageChangeSource()
        registerReceiver(packageSource, filter)

        // Intents
        val changeAppsStream: Observable<Action> = packageSource
                .changeStream
                .map { _ -> getApps() }
                .map { apps -> Action("update-apps", apps) }

        val queryStream: Observable<String> = RxTextView
                .textChanges(search)
                .skip(1) // We can ignore initial empty event
                .map { query -> query.toString() }

        val queryClearStream: Observable<Action> = queryStream
                .map{ query -> query.isBlank() }
                .map{ value -> Action("show-clear", !value) }

        val filterStream: Observable<Action> = queryStream
                .map { query ->
                    when (query.isBlank()) {
                        true -> {
                            apps
                        }
                        false -> {
                            apps.filter { app ->
                                val label: String = app.label.toString()
                                label.startsWith(query, true)
                            }
                        }
                    }
                }
                .map { value -> Action("update-apps", value) }

        val searchStream: Observable<Action> = RxTextView
                .editorActions(search)
                .filter { t -> t == 2 }
                .withLatestFrom(queryStream, BiFunction<Int, String, String> { _, query -> query})
                .map { value -> Action("mvez-search", value) }

        val clearStream: Observable<Action> = RxView
                .clicks(clear)
                .map { _ -> Action("hide-clear", apps) }

        val appLaunchStream: Observable<Action> = RxAdapterView
                .itemClickEvents(appGrid)
                .map { event -> event.position() }
                .map { value -> Action("app-launch", value) }

        val appInformationStream: Observable<Action> = RxAdapterView
                .itemLongClickEvents(appGrid)
                .map { event -> event.position() }
                .map { value -> Action("app-information", value) }

        // Apply side-effects for intents
        filterStream
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this)

        val updateAppStream = Observable.merge(changeAppsStream, filterStream)
        val clearQueryStream = Observable.merge(queryClearStream, clearStream)
        val appStream = Observable.merge(appLaunchStream, appInformationStream)
        Observable.merge(updateAppStream, searchStream, clearQueryStream, appStream)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this)

        changeAppsStream
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: Observer<Action> {
                    override fun onSubscribe(d: Disposable) {
                        // Populate initial view
                        appGrid.adapter = adapter
                    }

                    override fun onNext(t: Action) {
                        // On update apps, we change out loaded app list
                        apps = t.value as List<AppDetail>
                    }

                    override fun onError(e: Throwable) {
                    }

                    override fun onComplete() {
                    }
                })

    }


    /**
     * Handle registration of receiver
     */
    override fun onPause() {
        unregisterReceiver(packageSource)
        super.onPause()
    }

    override fun onResume() {
        registerReceiver(packageSource, filter)
        super.onResume()
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

    /**
     * Update adapter and view application list
     */
    private fun setApps (apps: List<AppDetail>) {
        adapter.clear()
        adapter.addAll(apps)
        adapter.notifyDataSetChanged()
    }

    override fun onSubscribe(d: Disposable) {
    }

    override fun onNext(t: Action) {
        if (adapter == null || t.value == null) {
            return
        }

        when (t.name) {
            "show-clear" -> {
                clear.visibility = when (t.value as Boolean) {
                    true -> View.VISIBLE
                    false -> View.GONE
                }
            }
            "update-apps" -> {
                setApps(t.value as List<AppDetail>)
            }
            "mvez-search" -> {
            }
            "hide-clear" -> {
                clear.visibility = View.GONE
                search.text.clear()
                setApps(t.value as List<AppDetail>)
            }
            "app-launch" -> {
                val app = adapter.getItem(t.value as Int)
                val intent = packageManager.getLaunchIntentForPackage(app.name.toString())
                this@SearchActivity.startActivity(intent)
            }
            "app-information" -> {
                val app = adapter.getItem(t.value as Int)
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:" + app.name.toString())
                this@SearchActivity.startActivity(intent)
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