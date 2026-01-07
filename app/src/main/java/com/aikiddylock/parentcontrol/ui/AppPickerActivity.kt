package com.aikiddylock.parentcontrol.ui

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.aikiddylock.parentcontrol.R
import com.aikiddylock.parentcontrol.core.Prefs

class AppPickerActivity : AppCompatActivity() {

    data class Row(val label: String, val pkg: String) {
        override fun toString(): String = label
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_list)

        findViewById<TextView>(R.id.title).text = "Pick apps to lock (tap to toggle)"
        val list = findViewById<ListView>(R.id.list)

        val pm = packageManager
        val apps = pm.getInstalledApplications(0)
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
            .map { ai -> Row(pm.getApplicationLabel(ai).toString(), ai.packageName) }
            .sortedBy { it.label.lowercase() }

        val blocked = Prefs.getBlockedApps(this).toMutableSet()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, apps)
        list.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        list.adapter = adapter

        for (i in apps.indices) {
            if (blocked.contains(apps[i].pkg)) list.setItemChecked(i, true)
        }

        list.setOnItemClickListener { _, _, position, _ ->
            val row = apps[position]
            if (blocked.contains(row.pkg)) blocked.remove(row.pkg) else blocked.add(row.pkg)
            Prefs.setBlockedApps(this, blocked)
        }
    }
}


