package com.aikiddylock.parentcontrol.place

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aikiddylock.parentcontrol.core.Prefs
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) return

        val transition = event.geofenceTransition
        val ids = event.triggeringGeofences?.mapNotNull { it.requestId }.orEmpty()

        val entering = transition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            transition == Geofence.GEOFENCE_TRANSITION_DWELL
        val exiting = transition == Geofence.GEOFENCE_TRANSITION_EXIT

        if (entering) {
            when {
                ids.contains(GeofenceManager.ID_HOME) -> Prefs.setGeofenceState(context, "HOME")
                ids.contains(GeofenceManager.ID_SCHOOL) -> Prefs.setGeofenceState(context, "SCHOOL")
            }
        } else if (exiting) {
            // If we exit a place, revert to NONE (MVP).
            Prefs.setGeofenceState(context, "NONE")
        }
    }
}


