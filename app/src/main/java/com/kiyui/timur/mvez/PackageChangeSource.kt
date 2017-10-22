package com.kiyui.timur.mvez

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.reactivex.subjects.PublishSubject

/**
 * Source for package change intent
 * ```kotlin
 * val filter = IntentFilter()
 * filter.addAction(Intent.ACTION_PACKAGE_ADDED)
 * filter.addAction(Intent.ACTION_PACKAGE_CHANGED)
 * filter.addAction(Intent.ACTION_PACKAGE_REMOVED)
 * filter.addAction(Intent.ACTION_PACKAGE_REPLACED)
 * filter.addDataScheme("package")
 * val packageSource = PackageChangeSource()
 * registerReceiver(packageSource, filter)
 * ```
 * This stream is used to map to a call to get all
 * apps so we can update the view when the installed
 * application list has changed
 */
class PackageChangeSource: BroadcastReceiver() {
    val changeStream: PublishSubject<Intent> = PublishSubject.create()
    override fun onReceive(context: Context, intent: Intent) {
        changeStream.onNext(intent)
    }
}