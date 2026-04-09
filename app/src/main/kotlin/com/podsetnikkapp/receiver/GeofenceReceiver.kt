package com.podsetnikkapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.android.gms.location.GeofencingEvent
import com.podsetnikkapp.service.AlarmService

class GeofenceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val event = try { GeofencingEvent.fromIntent(intent) } catch (e: Exception) { null } ?: return
        if (event.hasError()) return
        val reminderId = intent.getLongExtra("reminder_id", -1L)
        val title = intent.getStringExtra("reminder_title") ?: "Podsetnik - Lokacija"
        val desc = intent.getStringExtra("reminder_desc") ?: ""
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("reminder_id", reminderId)
            putExtra("reminder_title", title)
            putExtra("reminder_desc", "Stigli ste na lokaciju! $desc".trim())
            putExtra("reminder_ring", "SOUND_AND_VIBRATE")
            putExtra("reminder_lock_screen", true)
            putExtra("snooze_minutes", 10)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
