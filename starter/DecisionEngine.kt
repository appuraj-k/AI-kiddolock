package starter

/**
 * Combine policy rules + ML scores to decide whether to lock a foreground app.
 *
 * Keep this logic testable: given (package, place, time, features) -> decision.
 */
class DecisionEngine(
    private val policy: PolicyStore,
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

    fun decide(foregroundPackage: String, f: SessionFeatures): Decision {
        val isBlockedByPolicy = policy.isBlockedNow(foregroundPackage, f)

        val x = f.toNormalizedVector()
        val childScore = childModel.predictProb(x)
        val agitationScore = agitationModel.predictProb(x)

        // Conservative defaults (reduce false positives):
        val childLikely = childScore >= policy.childScoreThreshold
        val agitationLikely = agitationScore >= policy.agitationScoreThreshold

        val shouldLock = isBlockedByPolicy && (childLikely || agitationLikely)

        val reason = when {
            !isBlockedByPolicy -> "Allowed by policy"
            shouldLock && agitationLikely -> "Blocked (agitation pattern)"
            shouldLock && childLikely -> "Blocked (child mode)"
            else -> "Blocked by policy, but ML confidence low (no lock)"
        }

        return Decision(
            shouldLock = shouldLock,
            reason = reason,
            childScore = childScore,
            agitationScore = agitationScore,
            policyBlocked = isBlockedByPolicy,
        )
    }
}

/**
 * Minimal policy interface.
 * In a real app, back this with Room / DataStore and per-place rule sets.
 */
interface PolicyStore {
    val childScoreThreshold: Double
    val agitationScoreThreshold: Double

    /**
     * Return true if the app is blocked due to current schedule/place rules.
     */
    fun isBlockedNow(foregroundPackage: String, f: SessionFeatures): Boolean
}


