package com.kiyui.timur.mvez

import android.app.Activity
import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Context
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
import android.widget.Toast
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxAdapterView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.google.gson.Gson

/**
 * Main launcher application handling all application logic
 * All application intents are in the form of `Observable<Action>`
 * We implement `Observer<Action>` to handle all side effects reactively
 */
class SearchActivity: Activity(), Observer<Action> {
    private val gson: Gson = Gson()
    private lateinit var adapter: AppDetailAdapter
    private lateinit var mvezPreferences: MVEZPreferences
    private val appGrid by lazy { findViewById<GridView>(R.id.appsContainer) }
    private val search by lazy { findViewById<EditText>(R.id.action_search) }
    private val clear by lazy { findViewById<ImageButton>(R.id.clear_button) }
    private val settings by lazy { findViewById<ImageButton>(R.id.overflow_button) }
    private val packageSource = PackageChangeSource()
    private val filter = IntentFilter()
    private var appList = listOf<AppDetail>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load interface
        setContentView(R.layout.activity_search)
        window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Application preferences
        val preferences = getSharedPreferences("MVEZ", Context.MODE_PRIVATE)
        val manager = PreferenceManager(preferences)
        val preferenceStream = RxSharedPreferences.create(preferences)
        mvezPreferences = manager.get("mvez") as MVEZPreferences

        // Initialize application
        appList = getApps()

        // We initialize out adapter with a mutable list since we need to modify it when searching
        adapter = AppDetailAdapter(
                this,
                R.layout.app_item,
                appList.toMutableList(),
                manager.get("alphabetical") as Boolean)

        // Create an app change broadcast receiver so we have a source
        // for when an application is installed/uninstalled/updated
        filter.addAction(Intent.ACTION_PACKAGE_ADDED)
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED)
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED)
        filter.addDataScheme("package")

        registerReceiver(packageSource, filter)

        // Intents
        val queryStream: Observable<String> = RxTextView
                .textChanges(search)
                .skip(1) // We can ignore initial empty event
                .map { query -> query.toString() }

        // Preference action intents
        val alphabeticalPreferenceStream: Observable<Action> = preferenceStream
                .getBoolean("alphabetical")
                .asObservable()
                .map { value -> Action("pref-alphabetical", value) }

        val mvezPreferenceStream: Observable<Action> = preferenceStream
                .getString("mvez")
                .asObservable()
                .map { value ->
                    Action("pref-mvez", gson.fromJson(value, MVEZPreferences::class.java))
                }

        // Action intents
        val changeAppsStream: Observable<Action> = packageSource
                .changeStream
                .map { _ -> getApps() }
                .map { newApps -> Action("update-app-list", newApps) }

        val filterStream: Observable<Action> = queryStream
                .map { query ->
                    when (query.isBlank()) {
                        true -> {
                            appList
                        }
                        false -> {
                            appList.filter { app ->
                                // Reduce string into a regex that performs case-insensitive fuzzy searching
                                val regex = Regex(query.fold("", { acc, c -> "$acc$c.*"}), RegexOption.IGNORE_CASE)
                                regex.matches(app.label)
                            }
                        }
                    }
                }
                .map { value -> Action("filter-apps", value) }

        val searchStream: Observable<Action> = RxTextView
                .editorActions(search)
                .filter { t -> t == 2 }
                .withLatestFrom(queryStream, BiFunction<Int, String, String> { _, query -> query})
                .map { value -> Action("mvez-search", value) }

        val queryClearStream: Observable<Action> = queryStream
                .map{ query -> query.isBlank() }
                .map{ value -> Action("show-clear", !value) }

        val clearStream: Observable<Action> = RxView
                .clicks(clear)
                .map { _ -> Action("hide-clear", appList) }

        val appLaunchStream: Observable<Action> = RxAdapterView
                .itemClickEvents(appGrid)
                .map { event -> event.position() }
                .map { value -> Action("app-launch", value) }

        val appDetailStream: Observable<Action> = RxAdapterView
                .itemLongClickEvents(appGrid)
                .map { event -> event.position() }
                .map { value -> Action("app-information", value) }

        val settingsStream: Observable<Action> = RxView
                .clicks(settings)
                .map { _ -> Action("app-settings", true) }

        // Apply side-effects for intents
        Observable
                .merge(listOf(
                        // Preferences
                        alphabeticalPreferenceStream,
                        mvezPreferenceStream,
                        // Actions
                        filterStream       // filter-apps
                ))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this)

        Observable
                .merge(listOf(
                        // Actions
                        changeAppsStream,   // update-app-list
                        searchStream,       // mvez-search
                        queryClearStream,   // show-clear
                        clearStream,        // hide-clear
                        appLaunchStream,    // app-launch
                        appDetailStream,    // app-detail
                        settingsStream      // app-settings
                ))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this)
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
                .sortedWith(compareBy({ it -> it.label.toString().toUpperCase()}))
                .toList()
    }

    /**
     * Update adapter and view application list
     */
    private fun setAdapterApps (apps: List<AppDetail>) {
        adapter.clear()
        adapter.addAll(apps)
        adapter.notifyDataSetChanged()
    }

    override fun onSubscribe(d: Disposable) {
        appGrid.adapter = adapter
    }

    override fun onNext(t: Action) {
        when (t.name) {
            "pref-alphabetical" -> {
                adapter.alphabetical = t.value as Boolean
                adapter.notifyDataSetChanged()
            }
            "pref-mvez" -> {
                mvezPreferences = t.value as MVEZPreferences
            }
            "filter-apps" -> {
                // Update the GridView with a new list of applications
                setAdapterApps(t.value as List<AppDetail>)
            }
            "update-app-list" -> {
                appList = t.value as List<AppDetail>
            }
            "mvez-search" -> {
                val query = t.value as String
                val mvez = mvezPreferences.getActionableMVEZ(query)
                when (mvez) {
                    null -> {
                        val intent = Intent()
                        intent.action = Intent.ACTION_WEB_SEARCH
                        intent.putExtra(SearchManager.QUERY, query)

                        try {
                            this@SearchActivity.startActivity(intent)
                        } catch ( e: ActivityNotFoundException ) {
                            Toast.makeText(
                                    this,
                                    "Failed to find web search activity",
                                    Toast.LENGTH_SHORT).show()
                        }
                    }
                    else -> {
                        val filteredQuery = mvez.stripShortcut(query)
                        val intent = Intent()
                        intent.action = mvez.action
                        intent.putExtra(SearchManager.QUERY, filteredQuery)
                        intent.`package` = mvez.application
                        try {
                            this@SearchActivity.startActivity(intent)
                        } catch ( e: ActivityNotFoundException ) {
                            Toast.makeText(
                                    this,
                                    "Failed to launch activity",
                                    Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            "show-clear" -> {
                // Toggle the visibility of the clear button based on length of text input
                clear.visibility = when (t.value as Boolean) {
                    true -> View.VISIBLE
                    false -> View.GONE
                }
            }
            "hide-clear" -> {
                // Clear the current search term and reset application list when clear pressed
                clear.visibility = View.GONE
                search.text.clear()
                setAdapterApps(t.value as List<AppDetail>)
            }
            "app-launch" -> {
                // Launch the clicked application
                val app = adapter.getItem(t.value as Int)
                val intent = packageManager.getLaunchIntentForPackage(app.name.toString())
                try {
                    this@SearchActivity.startActivity(intent)
                } catch ( e: ActivityNotFoundException ) {
                    Toast.makeText(
                            this,
                            "Failed to launch activity",
                            Toast.LENGTH_SHORT).show()
                }
            }
            "app-information" -> {
                // Open details for the application
                // TODO: Review using a context menu, new feature in Oreo
                val app = adapter.getItem(t.value as Int)
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:" + app.name.toString())
                this@SearchActivity.startActivity(intent)
            }
            "app-settings" -> {
                val intent = Intent(this, SettingsActivity::class.java)
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
