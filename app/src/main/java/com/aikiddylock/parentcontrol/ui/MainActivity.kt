package com.aikiddylock.parentcontrol.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.aikiddylock.parentcontrol.R
import com.aikiddylock.parentcontrol.core.Prefs
import com.aikiddylock.parentcontrol.lock.AppLockAccessibilityService

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnAccessibility).setOnClickListener {
            AppLockAccessibilityService.openAccessibilitySettings(this)
        }

        findViewById<Button>(R.id.btnUsage).setOnClickListener {
            // Optional; some devices need this for better usage stats, but we rely on Accessibility for foreground detection.
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        findViewById<Button>(R.id.btnApps).setOnClickListener {
            startActivity(Intent(this, AppPickerActivity::class.java))
        }

        findViewById<Button>(R.id.btnPlaces).setOnClickListener {
            startActivity(Intent(this, PlaceActivity::class.java))
        }

        findViewById<Button>(R.id.btnLabel).setOnClickListener {
            startActivity(Intent(this, LabelActivity::class.java))
        }

        findViewById<Button>(R.id.btnStatus).setOnClickListener { refreshStatus() }

        refreshStatus()
    }

    private fun refreshStatus() {
        val blocked = Prefs.getBlockedApps(this).size
        val state = Prefs.getGeofenceState(this)
        val (csv, ts) = Prefs.getLastFeatures(this)
        val info = buildString {
            appendLine("Blocked apps: $blocked")
            appendLine("Place state: $state")
            appendLine("Last features: ${if (csv == null) "none" else "available"}")
            appendLine("Last features ts: $ts")
            appendLine("Parent PIN: ${Prefs.getParentPin(this@MainActivity)} (change in code for now)")
            appendLine()
            appendLine("Tip: if lock doesn't trigger, ensure Accessibility is enabled for AIkiddylock.")
        }
        findViewById<TextView>(R.id.txtStatus).text = info
    }

    private fun openBatteryOptimizationSettings() {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }
}


