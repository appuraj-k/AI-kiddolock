package starter

/**
 * Skeleton only (paste into an Android Studio project).
 *
 * In a real Android app:
 * - extend android.accessibilityservice.AccessibilityService
 * - listen for TYPE_WINDOW_STATE_CHANGED to detect foreground package
 * - call DecisionEngine.decide(...)
 * - if shouldLock -> start LockActivity (new task) to block the app
 *
 * Kept as a skeleton here because this repo is not yet an Android project.
 */
object AppLockAccessibilityServiceSkeleton {
    /*
    class AppLockAccessibilityService : AccessibilityService() {
        private val monitor = ForegroundMonitor(...)

        override fun onAccessibilityEvent(event: AccessibilityEvent) {
            if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
            val pkg = event.packageName?.toString() ?: return

            val features = monitor.currentFeatures()
            val d = decisionEngine.decide(pkg, features)
            if (d.shouldLock) {
                LockActivity.start(this, blockedPackage = pkg, reason = d.reason)
            }
        }

        override fun onInterrupt() = Unit
    }
    */
}


