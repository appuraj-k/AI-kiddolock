package com.aikiddylock.parentcontrol.ml

import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min

/**
 * Tiny on-device Logistic Regression with online SGD.
 * Stores only weights + bias (very low memory).
 */
class TinyLogReg(
    val featureCount: Int,
    var learningRate: Double = 0.05,
    var l2: Double = 1e-4,
    var bias: Double = 0.0,
    val w: DoubleArray = DoubleArray(featureCount) { 0.0 },
) {
    fun predictProb(x: DoubleArray): Double {
        require(x.size == featureCount) { "Expected $featureCount features, got ${x.size}" }
        var z = bias
        for (i in 0 until featureCount) z += w[i] * x[i]
        return sigmoid(z)
    }

    fun updateOne(x: DoubleArray, y: Int): Double {
        require(y == 0 || y == 1)
        val p = predictProb(x)
        val err = p - y.toDouble()
        for (i in 0 until featureCount) {
            val grad = err * x[i] + l2 * w[i]
            w[i] -= learningRate * grad
        }
        bias -= learningRate * err

        val eps = 1e-12
        return -(
            y.toDouble() * ln(max(eps, p)) +
                (1.0 - y.toDouble()) * ln(max(eps, 1.0 - p))
            )
    }

    fun toStringSnapshot(): String {
        // bias|w0,w1,w2...
        val sb = StringBuilder()
        sb.append(bias)
        sb.append("|")
        for (i in w.indices) {
            if (i > 0) sb.append(",")
            sb.append(w[i])
        }
        return sb.toString()
    }

    fun loadStringSnapshot(s: String) {
        val parts = s.split("|")
        if (parts.size != 2) return
        val b = parts[0].toDoubleOrNull() ?: return
        val weights = parts[1].split(",").mapNotNull { it.toDoubleOrNull() }
        if (weights.size != featureCount) return
        bias = b
        for (i in 0 until featureCount) w[i] = weights[i]
    }

    private fun sigmoid(z: Double): Double {
        val zz = min(30.0, max(-30.0, z))
        return 1.0 / (1.0 + exp(-zz))
    }
}


