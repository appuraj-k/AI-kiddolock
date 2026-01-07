package com.aikiddylock.parentcontrol.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.aikiddylock.parentcontrol.R
import com.aikiddylock.parentcontrol.core.Prefs
import com.aikiddylock.parentcontrol.place.GeofenceManager
import com.google.android.gms.location.LocationServices

class PlaceActivity : AppCompatActivity() {
    private val fused by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private val geo by lazy { GeofenceManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place)

        val info = findViewById<TextView>(R.id.txtInfo)
        val result = findViewById<TextView>(R.id.txtResult)

        fun refresh() {
            val home = Prefs.getHome(this)
            val school = Prefs.getSchool(this)
            val state = Prefs.getGeofenceState(this)
            info.text = buildString {
                appendLine("Current state: $state")
                appendLine("Home: ${home?.first}, ${home?.second}")
                appendLine("School: ${school?.first}, ${school?.second}")
                appendLine()
                appendLine("Note: for best geofence behavior, allow background location and disable battery optimizations.")
            }
        }

        fun ensureLocationPerms(): Boolean {
            val fine = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val coarse = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (fine || coarse) return true
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 100)
            return false
        }

        fun setPlace(kind: String) {
            if (!ensureLocationPerms()) return
            fused.lastLocation.addOnSuccessListener { loc ->
                if (loc == null) {
                    result.text = "No last location available. Turn on GPS and try again."
                    return@addOnSuccessListener
                }
                val lat = loc.latitude
                val lng = loc.longitude
                when (kind) {
                    "HOME" -> {
                        Prefs.setHome(this, lat, lng)
                        geo.registerHome(lat, lng)
                        result.text = "Set HOME to $lat,$lng and registered geofence."
                    }
                    "SCHOOL" -> {
                        Prefs.setSchool(this, lat, lng)
                        geo.registerSchool(lat, lng)
                        result.text = "Set SCHOOL to $lat,$lng and registered geofence."
                    }
                }
                refresh()
            }.addOnFailureListener { e ->
                result.text = "Location error: ${e.message}"
            }
        }

        findViewById<Button>(R.id.btnSetHome).setOnClickListener { setPlace("HOME") }
        findViewById<Button>(R.id.btnSetSchool).setOnClickListener { setPlace("SCHOOL") }
        findViewById<Button>(R.id.btnClear).setOnClickListener {
            Prefs.clearPlaces(this)
            geo.clearAll(
                onDone = {
                    result.text = "Cleared places and geofences."
                    refresh()
                },
                onError = { e ->
                    result.text = "Failed to clear geofences: ${e.message}"
                    refresh()
                }
            )
        }

        refresh()
    }
}


