package starter

import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min

/**
 * Tiny on-device Logistic Regression with online SGD.
 *
 * - No ML library needed
 * - Model = weights + bias (very small memory)
 * - Use for binary classification: y in {0,1}
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

    /**
     * One SGD step on a single labeled example.
     * @param y label 0 or 1
     * @return negative log-likelihood loss for this example
     */
    fun updateOne(x: DoubleArray, y: Int): Double {
        require(y == 0 || y == 1) { "y must be 0 or 1" }
        val p = predictProb(x)
        val err = p - y.toDouble() // dL/dz for logistic + NLL

        // L2 regularization (do not regularize bias)
        for (i in 0 until featureCount) {
            val grad = err * x[i] + l2 * w[i]
            w[i] -= learningRate * grad
        }
        bias -= learningRate * err

        // loss = -[y*log(p) + (1-y)*log(1-p)]
        val eps = 1e-12
        return -(
            y.toDouble() * ln(max(eps, p)) +
                (1.0 - y.toDouble()) * ln(max(eps, 1.0 - p))
            )
    }

    fun toSnapshot(): Snapshot = Snapshot(featureCount, learningRate, l2, bias, w.copyOf())

    fun loadSnapshot(s: Snapshot) {
        require(s.featureCount == featureCount) {
            "Snapshot featureCount=${s.featureCount} doesn't match model featureCount=$featureCount"
        }
        learningRate = s.learningRate
        l2 = s.l2
        bias = s.bias
        for (i in 0 until featureCount) w[i] = s.w[i]
    }

    data class Snapshot(
        val featureCount: Int,
        val learningRate: Double,
        val l2: Double,
        val bias: Double,
        val w: DoubleArray,
    )

    private fun sigmoid(z: Double): Double {
        // numerically stable-ish clamp
        val zz = min(30.0, max(-30.0, z))
        return 1.0 / (1.0 + exp(-zz))
    }
}


