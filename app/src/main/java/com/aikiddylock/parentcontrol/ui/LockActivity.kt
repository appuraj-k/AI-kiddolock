package com.aikiddylock.parentcontrol.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.aikiddylock.parentcontrol.R
import com.aikiddylock.parentcontrol.core.Prefs

class LockActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock)

        val reason = intent.getStringExtra(EXTRA_REASON) ?: "Blocked"
        val pkg = intent.getStringExtra(EXTRA_PKG) ?: ""

        findViewById<TextView>(R.id.txtReason).text = "$reason\n\nPackage: $pkg"

        val edt = findViewById<EditText>(R.id.edtPin)
        val err = findViewById<TextView>(R.id.txtError)
        findViewById<Button>(R.id.btnUnlock).setOnClickListener {
            val pin = edt.text?.toString()?.trim().orEmpty()
            if (pin == Prefs.getParentPin(this)) {
                // Allow this app for 2 minutes
                Prefs.setTempAllow(this, pkg, System.currentTimeMillis() + 2 * 60 * 1000L)
                finish()
            } else {
                err.text = "Wrong PIN"
            }
        }
    }

    override fun onBackPressed() {
        // Prevent back from bypassing lock screen.
        // If they press Home, they can leave the app, which is acceptable for MVP.
    }

    companion object {
        private const val EXTRA_PKG = "blocked_pkg"
        private const val EXTRA_REASON = "reason"

        fun start(ctx: Context, blockedPackage: String, reason: String) {
            val i = Intent(ctx, LockActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(EXTRA_PKG, blockedPackage)
                .putExtra(EXTRA_REASON, reason)
            ctx.startActivity(i)
        }
    }
}


