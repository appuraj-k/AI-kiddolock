package com.aikiddylock.parentcontrol.lock

import android.content.Context
import com.aikiddylock.parentcontrol.core.Prefs
import com.aikiddylock.parentcontrol.ml.SessionFeatures
import com.aikiddylock.parentcontrol.ml.TinyLogReg

class DecisionEngine(
    private val ctx: Context,
    private val childModel: TinyLogReg,
    private val agitationModel: TinyLogReg,
) {
    data class Decision(
        val shouldLock: Boolean,
        val reason: String,
        val childScore: Double,
        val agitationScore: Double,
        val policyBlocked: Boolean,
    )

    var childThreshold: Double = 0.70
    var agitationThreshold: Double = 0.80

    fun decide(pkg: String, f: SessionFeatures): Decision {
        val isBlocked = isBlockedNow(pkg, f)
        val x = f.toNormalizedVector()
        val childScore = childModel.predictProb(x)
        val agitationScore = agitationModel.predictProb(x)

        val childLikely = childScore >= childThreshold
        val agitationLikely = agitationScore >= agitationThreshold

        val shouldLock = isBlocked && (childLikely || agitationLikely) && !Prefs.isTempAllowed(ctx, pkg)
        val reason = when {
            !isBlocked -> "Allowed by policy"
            shouldLock && agitationLikely -> "Blocked: agitation pattern detected"
            shouldLock && childLikely -> "Blocked: child mode detected"
            else -> "Blocked by policy (waiting for higher confidence)"
        }

        return Decision(
            shouldLock = shouldLock,
            reason = reason,
            childScore = childScore,
            agitationScore = agitationScore,
            policyBlocked = isBlocked,
        )
    }

    private fun isBlockedNow(pkg: String, f: SessionFeatures): Boolean {
        val blockedApps = Prefs.getBlockedApps(ctx)
        if (!blockedApps.contains(pkg)) return false

        // Location-based policy (MVP):
        // - If we are in SCHOOL geofence => block
        // - If we are in HOME geofence => allow (parents can still block via ML agitation)
        // - If unknown location => block (safer default)
        return when {
            f.isInSchoolPlace -> true
            f.isInHomePlace -> false
            else -> true
        }
    }
}


