package com.aikiddylock.parentcontrol.ml

data class SessionFeatures(
    val windowSeconds: Int,
    val appSwitchCount: Int,
    val uniqueAppCount: Int,
    val reopenQuickCount: Int,
    val screenOnCount: Int,
    val screenOffCount: Int,
    val screenToggleBurstCount: Int,
    val blockedAppAttemptCount: Int,
    val isSchoolHours: Boolean,
    val isBedtime: Boolean,
    val isInKnownPlace: Boolean,
    val isInSchoolPlace: Boolean,
    val isInHomePlace: Boolean,
) {
    fun toNormalizedVector(): DoubleArray {
        val minutes = (windowSeconds.coerceAtLeast(60)).toDouble() / 60.0
        fun rate(c: Int): Double = c.toDouble() / minutes
        fun b(v: Boolean): Double = if (v) 1.0 else 0.0
        return doubleArrayOf(
            rate(appSwitchCount),
            rate(uniqueAppCount),
            rate(reopenQuickCount),
            rate(screenOnCount),
            rate(screenOffCount),
            rate(screenToggleBurstCount),
            rate(blockedAppAttemptCount),
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


