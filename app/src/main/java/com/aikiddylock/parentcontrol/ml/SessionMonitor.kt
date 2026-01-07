package com.aikiddylock.parentcontrol.ml

import java.util.ArrayDeque

/**
 * Rolling-window counters for lightweight behavior signals.
 * Designed to run inside AccessibilityService.
 */
class SessionMonitor(
    private val windowMillis: Long = 10 * 60 * 1000L, // 10 minutes
) {
    private val appSwitchTimes = ArrayDeque<Long>()
    private val uniqueApps = LinkedHashMap<String, Long>() // pkg -> last seen time
    private val reopenQuickTimes = ArrayDeque<Long>()

    private val screenOnTimes = ArrayDeque<Long>()
    private val screenOffTimes = ArrayDeque<Long>()
    private val screenToggleBurstTimes = ArrayDeque<Long>()

    private val blockedAttemptTimes = ArrayDeque<Long>()

    private var lastPkg: String? = null
    private var lastPkgTs: Long = 0L

    fun onAppForeground(pkg: String, now: Long) {
        trim(now)
        appSwitchTimes.addLast(now)
        uniqueApps[pkg] = now

        val lp = lastPkg
        if (lp != null && lp == pkg && (now - lastPkgTs) <= 10_000L) {
            reopenQuickTimes.addLast(now)
        }
        lastPkg = pkg
        lastPkgTs = now
    }

    fun onScreenOn(now: Long) {
        trim(now)
        screenOnTimes.addLast(now)
        detectToggleBurst(now)
    }

    fun onScreenOff(now: Long) {
        trim(now)
        screenOffTimes.addLast(now)
        detectToggleBurst(now)
    }

    fun onBlockedAttempt(now: Long) {
        trim(now)
        blockedAttemptTimes.addLast(now)
    }

    fun snapshot(windowSeconds: Int): Snapshot {
        val now = System.currentTimeMillis()
        trim(now)
        // remove old unique apps
        uniqueApps.entries.removeIf { (_, ts) -> now - ts > windowMillis }

        return Snapshot(
            windowSeconds = windowSeconds,
            appSwitchCount = appSwitchTimes.size,
            uniqueAppCount = uniqueApps.size,
            reopenQuickCount = reopenQuickTimes.size,
            screenOnCount = screenOnTimes.size,
            screenOffCount = screenOffTimes.size,
            screenToggleBurstCount = screenToggleBurstTimes.size,
            blockedAppAttemptCount = blockedAttemptTimes.size,
        )
    }

    data class Snapshot(
        val windowSeconds: Int,
        val appSwitchCount: Int,
        val uniqueAppCount: Int,
        val reopenQuickCount: Int,
        val screenOnCount: Int,
        val screenOffCount: Int,
        val screenToggleBurstCount: Int,
        val blockedAppAttemptCount: Int,
    )

    private fun trim(now: Long) {
        fun trimDeque(d: ArrayDeque<Long>) {
            while (d.isNotEmpty() && now - d.first > windowMillis) d.removeFirst()
        }
        trimDeque(appSwitchTimes)
        trimDeque(reopenQuickTimes)
        trimDeque(screenOnTimes)
        trimDeque(screenOffTimes)
        trimDeque(screenToggleBurstTimes)
        trimDeque(blockedAttemptTimes)
    }

    private fun detectToggleBurst(now: Long) {
        // crude burst: if >= 4 toggles within 30 seconds, record a burst marker
        val cutoff = now - 30_000L
        var toggles = 0
        for (t in screenOnTimes) if (t >= cutoff) toggles++
        for (t in screenOffTimes) if (t >= cutoff) toggles++
        if (toggles >= 4) {
            screenToggleBurstTimes.addLast(now)
        }
    }
}


