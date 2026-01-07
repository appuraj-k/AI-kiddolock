package com.aikiddylock.parentcontrol.lock

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import com.aikiddylock.parentcontrol.core.Prefs
import com.aikiddylock.parentcontrol.ml.SessionFeatures
import com.aikiddylock.parentcontrol.ml.SessionMonitor
import com.aikiddylock.parentcontrol.ml.TinyLogReg
import com.aikiddylock.parentcontrol.ui.LockActivity
import java.util.Calendar

class AppLockAccessibilityService : AccessibilityService() {

    private val monitor = SessionMonitor(windowMillis = 10 * 60 * 1000L)

    private lateinit var childModel: TinyLogReg
    private lateinit var agitationModel: TinyLogReg
    private lateinit var decisionEngine: DecisionEngine

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val now = System.currentTimeMillis()
            when (intent.action) {
                Intent.ACTION_SCREEN_ON -> monitor.onScreenOn(now)
                Intent.ACTION_SCREEN_OFF -> monitor.onScreenOff(now)
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        childModel = TinyLogReg(SessionFeatures.FEATURE_COUNT).apply {
            Prefs.loadChildModel(this@AppLockAccessibilityService)?.let { loadStringSnapshot(it) }
        }
        agitationModel = TinyLogReg(SessionFeatures.FEATURE_COUNT).apply {
            Prefs.loadAgitationModel(this@AppLockAccessibilityService)?.let { loadStringSnapshot(it) }
        }
        decisionEngine = DecisionEngine(this, childModel, agitationModel)

        registerReceiver(screenReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        })
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return

        // ignore our own app screens
        if (pkg == packageName) return

        val now = System.currentTimeMillis()
        monitor.onAppForeground(pkg, now)

        val blockedApps = Prefs.getBlockedApps(this)
        val policyRelevant = blockedApps.contains(pkg)
        if (policyRelevant) monitor.onBlockedAttempt(now)

        val f = buildFeatures()
        Prefs.setLastFeatures(this, f.toNormalizedVector().joinToString(","))

        val d = decisionEngine.decide(pkg, f)
        if (d.shouldLock) {
            LockActivity.start(this, blockedPackage = pkg, reason = d.reason)
        }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        try {
            unregisterReceiver(screenReceiver)
        } catch (_: Exception) {
        }
        super.onDestroy()
    }

    private fun buildFeatures(): SessionFeatures {
        val windowSeconds = 10 * 60
        val snap = monitor.snapshot(windowSeconds)

        val state = Prefs.getGeofenceState(this)
        val isInHome = state == "HOME"
        val isInSchool = state == "SCHOOL"
        val isKnown = isInHome || isInSchool

        val (isSchoolHours, isBedtime) = timeFlags()

        return SessionFeatures(
            windowSeconds = snap.windowSeconds,
            appSwitchCount = snap.appSwitchCount,
            uniqueAppCount = snap.uniqueAppCount,
            reopenQuickCount = snap.reopenQuickCount,
            screenOnCount = snap.screenOnCount,
            screenOffCount = snap.screenOffCount,
            screenToggleBurstCount = snap.screenToggleBurstCount,
            blockedAppAttemptCount = snap.blockedAppAttemptCount,
            isSchoolHours = isSchoolHours,
            isBedtime = isBedtime,
            isInKnownPlace = isKnown,
            isInSchoolPlace = isInSchool,
            isInHomePlace = isInHome,
        )
    }

    private fun timeFlags(): Pair<Boolean, Boolean> {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val day = c.get(Calendar.DAY_OF_WEEK)

        val weekday = day != Calendar.SATURDAY && day != Calendar.SUNDAY
        val isSchoolHours = weekday && hour in 9..15
        val isBedtime = hour >= 22 || hour <= 6
        return isSchoolHours to isBedtime
    }

    companion object {
        fun openAccessibilitySettings(ctx: Context) {
            ctx.startActivity(
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}


