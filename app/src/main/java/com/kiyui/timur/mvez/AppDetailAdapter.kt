package com.kiyui.timur.mvez

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.SectionIndexer
import android.widget.TextView

/**
 * For our `GridView` in `SearchActivity`, we implement an `ArrayAdapter`
 * that understands how to populate our `app_item` layout with the data
 * encapsulated inside our `AppDetail` class
 */
class AppDetailAdapter(context: Context, resource: Int, private val apps: List<AppDetail>, var alphabetical: Boolean): ArrayAdapter<AppDetail>(context, resource, apps), SectionIndexer {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val appView: View = when (convertView == null) {
            true -> inflater.inflate(R.layout.app_item, null)
            false -> convertView as View
        }

        val appIcon = appView.findViewById<ImageView>(R.id.appIcon)
        appIcon.setImageDrawable(apps[position].icon)
        val appLabel = appView.findViewById<TextView>(R.id.appLabel)
        appLabel.text = apps[position].label

        return appView
    }

    /**
     * We implement section indexer so we can have named scrolling
     */
    override fun getSections(): Array<Any> {
        val labels: List<CharSequence> = apps
                .map{ app -> app.label }
                .map { label ->
                    when (alphabetical) {
                        true -> label[0].toString().toUpperCase()
                        false -> label
                    }
                }
        return labels.toTypedArray()
    }

    override fun getPositionForSection(sectionIndex: Int): Int {
        return sectionIndex
    }

    override fun getSectionForPosition(position: Int): Int {
        return 0
    }
}