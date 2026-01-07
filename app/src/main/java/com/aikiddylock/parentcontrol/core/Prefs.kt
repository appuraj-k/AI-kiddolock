package com.aikiddylock.parentcontrol.core

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private const val PREFS = "aikiddylock_prefs"

    private const val KEY_BLOCKED_APPS = "blocked_apps"
    private const val KEY_PARENT_PIN = "parent_pin"

    private const val KEY_HOME_LAT = "home_lat"
    private const val KEY_HOME_LNG = "home_lng"
    private const val KEY_SCHOOL_LAT = "school_lat"
    private const val KEY_SCHOOL_LNG = "school_lng"
    private const val KEY_GEOFENCE_STATE = "geofence_state" // NONE/HOME/SCHOOL

    private const val KEY_LAST_FEATURES = "last_features_csv"
    private const val KEY_LAST_FEATURES_TS = "last_features_ts"

    private const val KEY_CHILD_MODEL = "child_model"
    private const val KEY_AGITATION_MODEL = "agitation_model"

    private const val KEY_TEMP_ALLOW_PREFIX = "temp_allow_" // + package -> millis

    private fun sp(ctx: Context): SharedPreferences =
        ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getBlockedApps(ctx: Context): Set<String> =
        sp(ctx).getStringSet(KEY_BLOCKED_APPS, emptySet()) ?: emptySet()

    fun setBlockedApps(ctx: Context, pkgs: Set<String>) {
        sp(ctx).edit().putStringSet(KEY_BLOCKED_APPS, pkgs).apply()
    }

    fun getParentPin(ctx: Context): String =
        sp(ctx).getString(KEY_PARENT_PIN, "1234") ?: "1234"

    fun setParentPin(ctx: Context, pin: String) {
        sp(ctx).edit().putString(KEY_PARENT_PIN, pin).apply()
    }

    fun setHome(ctx: Context, lat: Double, lng: Double) {
        sp(ctx).edit().putString(KEY_HOME_LAT, lat.toString()).putString(KEY_HOME_LNG, lng.toString()).apply()
    }

    fun setSchool(ctx: Context, lat: Double, lng: Double) {
        sp(ctx).edit().putString(KEY_SCHOOL_LAT, lat.toString()).putString(KEY_SCHOOL_LNG, lng.toString()).apply()
    }

    fun clearPlaces(ctx: Context) {
        sp(ctx).edit()
            .remove(KEY_HOME_LAT).remove(KEY_HOME_LNG)
            .remove(KEY_SCHOOL_LAT).remove(KEY_SCHOOL_LNG)
            .putString(KEY_GEOFENCE_STATE, "NONE")
            .apply()
    }

    fun getHome(ctx: Context): Pair<Double, Double>? {
        val lat = sp(ctx).getString(KEY_HOME_LAT, null) ?: return null
        val lng = sp(ctx).getString(KEY_HOME_LNG, null) ?: return null
        return lat.toDoubleOrNull()?.let { la -> lng.toDoubleOrNull()?.let { lo -> la to lo } }
    }

    fun getSchool(ctx: Context): Pair<Double, Double>? {
        val lat = sp(ctx).getString(KEY_SCHOOL_LAT, null) ?: return null
        val lng = sp(ctx).getString(KEY_SCHOOL_LNG, null) ?: return null
        return lat.toDoubleOrNull()?.let { la -> lng.toDoubleOrNull()?.let { lo -> la to lo } }
    }

    fun setGeofenceState(ctx: Context, state: String) {
        sp(ctx).edit().putString(KEY_GEOFENCE_STATE, state).apply()
    }

    fun getGeofenceState(ctx: Context): String =
        sp(ctx).getString(KEY_GEOFENCE_STATE, "NONE") ?: "NONE"

    fun setLastFeatures(ctx: Context, csv: String) {
        sp(ctx).edit()
            .putString(KEY_LAST_FEATURES, csv)
            .putLong(KEY_LAST_FEATURES_TS, System.currentTimeMillis())
            .apply()
    }

    fun getLastFeatures(ctx: Context): Pair<String?, Long> =
        (sp(ctx).getString(KEY_LAST_FEATURES, null)) to sp(ctx).getLong(KEY_LAST_FEATURES_TS, 0L)

    fun saveChildModel(ctx: Context, snapshot: String) {
        sp(ctx).edit().putString(KEY_CHILD_MODEL, snapshot).apply()
    }

    fun loadChildModel(ctx: Context): String? = sp(ctx).getString(KEY_CHILD_MODEL, null)

    fun saveAgitationModel(ctx: Context, snapshot: String) {
        sp(ctx).edit().putString(KEY_AGITATION_MODEL, snapshot).apply()
    }

    fun loadAgitationModel(ctx: Context): String? = sp(ctx).getString(KEY_AGITATION_MODEL, null)

    fun setTempAllow(ctx: Context, pkg: String, untilMillis: Long) {
        sp(ctx).edit().putLong(KEY_TEMP_ALLOW_PREFIX + pkg, untilMillis).apply()
    }

    fun isTempAllowed(ctx: Context, pkg: String, nowMillis: Long = System.currentTimeMillis()): Boolean {
        val until = sp(ctx).getLong(KEY_TEMP_ALLOW_PREFIX + pkg, 0L)
        return until > nowMillis
    }
}


