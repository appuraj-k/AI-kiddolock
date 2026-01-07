package com.aikiddylock.parentcontrol.place

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class GeofenceManager(private val ctx: Context) {
    private val client: GeofencingClient = LocationServices.getGeofencingClient(ctx)

    fun registerHome(lat: Double, lng: Double, radiusMeters: Float = 150f, dwellSeconds: Int = 60) {
        register(ID_HOME, lat, lng, radiusMeters, dwellSeconds)
    }

    fun registerSchool(lat: Double, lng: Double, radiusMeters: Float = 150f, dwellSeconds: Int = 60) {
        register(ID_SCHOOL, lat, lng, radiusMeters, dwellSeconds)
    }

    fun clearAll(onDone: (() -> Unit)? = null, onError: ((Exception) -> Unit)? = null) {
        client.removeGeofences(pendingIntent())
            .addOnSuccessListener { onDone?.invoke() }
            .addOnFailureListener { e -> onError?.invoke(e) }
    }

    private fun register(id: String, lat: Double, lng: Double, radiusMeters: Float, dwellSeconds: Int) {
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) return

        val geofence = Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(lat, lng, radiusMeters)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or
                    Geofence.GEOFENCE_TRANSITION_EXIT or
                    Geofence.GEOFENCE_TRANSITION_DWELL
            )
            .setLoiteringDelay(dwellSeconds * 1000)
            .build()

        val req = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        client.addGeofences(req, pendingIntent())
    }

    private fun pendingIntent(): PendingIntent {
        val intent = Intent(ctx, GeofenceReceiver::class.java)
        return PendingIntent.getBroadcast(
            ctx,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ID_HOME = "HOME"
        const val ID_SCHOOL = "SCHOOL"
    }
}


