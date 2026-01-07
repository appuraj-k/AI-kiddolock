package starter

/**
 * A small, privacy-friendly feature set for behavior classification.
 *
 * You compute these over a rolling window (e.g. 5–10 minutes) and normalize into a DoubleArray.
 *
 * Suggested models:
 * - childScore model
 * - agitationScore model
 */
data class SessionFeatures(
    // Window configuration (for normalization)
    val windowSeconds: Int,

    // Usage intensity
    val appSwitchCount: Int,            // foreground app switches
    val uniqueAppCount: Int,            // unique foreground packages
    val reopenQuickCount: Int,          // reopen same app within a short time

    // Screen behavior
    val screenOnCount: Int,
    val screenOffCount: Int,
    val screenToggleBurstCount: Int,    // toggles in bursts (rapid on/off)

    // Restricted behavior
    val blockedAppAttemptCount: Int,    // user tried opening a blocked app

    // Context flags (0/1 features)
    val isSchoolHours: Boolean,
    val isBedtime: Boolean,
    val isInKnownPlace: Boolean,        // inside any configured geofence
    val isInSchoolPlace: Boolean,
    val isInHomePlace: Boolean,
) {
    fun toNormalizedVector(): DoubleArray {
        // Keep featureCount stable for TinyLogReg.
        // Normalize counts by time to make it device-agnostic.
        val minutes = (windowSeconds.coerceAtLeast(60)).toDouble() / 60.0

        fun ratePerMin(c: Int): Double = c.toDouble() / minutes
        fun b(v: Boolean): Double = if (v) 1.0 else 0.0

        return doubleArrayOf(
            // Rates
            ratePerMin(appSwitchCount),
            ratePerMin(uniqueAppCount),
            ratePerMin(reopenQuickCount),
            ratePerMin(screenOnCount),
            ratePerMin(screenOffCount),
            ratePerMin(screenToggleBurstCount),
            ratePerMin(blockedAppAttemptCount),

            // Binary context
            b(isSchoolHours),
            b(isBedtime),
            b(isInKnownPlace),
            b(isInSchoolPlace),
            b(isInHomePlace),
        )
    }

    companion object {
        const val FEATURE_COUNT = 12
    }
}


