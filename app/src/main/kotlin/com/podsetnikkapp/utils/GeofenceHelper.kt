package com.podsetnikkapp.utils

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.podsetnikkapp.data.Reminder
import com.podsetnikkapp.receiver.GeofenceReceiver

object GeofenceHelper {

    @SuppressLint("MissingPermission")
    fun addGeofence(context: Context, reminder: Reminder) {
        if (!reminder.geofenceEnabled) return
        val client = LocationServices.getGeofencingClient(context)

        val geofence = Geofence.Builder()
            .setRequestId("reminder_${reminder.id}")
            .setCircularRegion(reminder.geofenceLat, reminder.geofenceLng, reminder.geofenceRadius)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence).build()

        val intent = Intent(context, GeofenceReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
            putExtra("reminder_title", reminder.title)
            putExtra("reminder_desc", reminder.description)
        }
        val pi = PendingIntent.getBroadcast(
            context, reminder.id.toInt() + 5000, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try { client.addGeofences(request, pi) } catch (e: Exception) { }
    }

    fun removeGeofence(context: Context, reminderId: Long) {
        LocationServices.getGeofencingClient(context).removeGeofences(listOf("reminder_$reminderId"))
    }
}
