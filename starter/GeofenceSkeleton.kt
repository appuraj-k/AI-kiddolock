package starter

/**
 * Skeleton only (paste into an Android Studio project).
 *
 * Uses GeofencingClient to maintain booleans in SessionFeatures:
 * - isInKnownPlace, isInSchoolPlace, isInHomePlace
 *
 * Typical flow:
 * - Parent defines places (lat/lng/radius) and tags: HOME/SCHOOL/OTHER
 * - Register geofences with DWELL (to avoid GPS jitter false triggers)
 * - BroadcastReceiver receives transitions and updates a small state store
 */
object GeofenceSkeleton {
    /*
    class GeofenceReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val event = GeofencingEvent.fromIntent(intent) ?: return
            if (event.hasError()) return

            val transition = event.geofenceTransition
            val ids = event.triggeringGeofences?.mapNotNull { it.requestId }.orEmpty()
            placeStateStore.applyTransition(ids, transition)
        }
    }

    class PlaceStateStore {
        // store current place flags in DataStore/SharedPreferences
        fun applyTransition(ids: List<String>, transition: Int) { ... }
        fun currentFlags(): PlaceFlags = ...
    }
    */
}


