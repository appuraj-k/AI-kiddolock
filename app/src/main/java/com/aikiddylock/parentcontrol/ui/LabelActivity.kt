package com.aikiddylock.parentcontrol.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.aikiddylock.parentcontrol.R
import com.aikiddylock.parentcontrol.core.Prefs
import com.aikiddylock.parentcontrol.ml.SessionFeatures
import com.aikiddylock.parentcontrol.ml.TinyLogReg

class LabelActivity : AppCompatActivity() {
    private val childModel = TinyLogReg(SessionFeatures.FEATURE_COUNT)
    private val agitationModel = TinyLogReg(SessionFeatures.FEATURE_COUNT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_label)

        Prefs.loadChildModel(this)?.let { childModel.loadStringSnapshot(it) }
        Prefs.loadAgitationModel(this)?.let { agitationModel.loadStringSnapshot(it) }

        val info = findViewById<TextView>(R.id.txtInfo)
        val result = findViewById<TextView>(R.id.txtResult)

        fun loadX(): DoubleArray? {
            val (csv, ts) = Prefs.getLastFeatures(this)
            if (csv.isNullOrBlank()) return null
            val parts = csv.split(",").mapNotNull { it.toDoubleOrNull() }
            if (parts.size != SessionFeatures.FEATURE_COUNT) return null
            info.text = "Last feature vector saved at: $ts\n\nx = ${csv.take(160)}${if (csv.length > 160) "..." else ""}"
            return parts.toDoubleArray()
        }

        fun updateScores() {
            val x = loadX()
            if (x == null) {
                info.text = "No features yet.\n\nOpen some apps for 1–2 minutes after enabling Accessibility, then come back."
                return
            }
            val childScore = childModel.predictProb(x)
            val agScore = agitationModel.predictProb(x)
            result.text = "Scores:\nchildScore=$childScore\nagitationScore=$agScore"
        }

        fun train(model: TinyLogReg, y: Int, modelName: String) {
            val x = loadX()
            if (x == null) {
                result.text = "No features to train on yet."
                return
            }
            val loss = model.updateOne(x, y)
            result.text = "Trained $modelName with y=$y\nloss=$loss"
            updateScores()
        }

        findViewById<Button>(R.id.btnChild).setOnClickListener {
            train(childModel, 1, "childModel")
            Prefs.saveChildModel(this, childModel.toStringSnapshot())
        }
        findViewById<Button>(R.id.btnNotChild).setOnClickListener {
            train(childModel, 0, "childModel")
            Prefs.saveChildModel(this, childModel.toStringSnapshot())
        }
        findViewById<Button>(R.id.btnAgitated).setOnClickListener {
            train(agitationModel, 1, "agitationModel")
            Prefs.saveAgitationModel(this, agitationModel.toStringSnapshot())
        }
        findViewById<Button>(R.id.btnCalm).setOnClickListener {
            train(agitationModel, 0, "agitationModel")
            Prefs.saveAgitationModel(this, agitationModel.toStringSnapshot())
        }

        updateScores()
    }
}


