package com.podsetnikkapp.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import com.podsetnikkapp.service.AlarmService
import com.podsetnikkapp.ui.screens.AlarmActivity

class AlarmReceiver : BroadcastReceiver() {
    private val handler = Handler(Looper.getMainLooper())

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("reminder_id", -1L)
        val title = intent.getStringExtra("reminder_title") ?: "Podsetnik"
        val desc = intent.getStringExtra("reminder_desc") ?: ""
        val ringTypeName = intent.getStringExtra("reminder_ring") ?: "SOUND_AND_VIBRATE"
        val showOnLock = intent.getBooleanExtra("reminder_lock_screen", true)
        val snoozeMin = intent.getIntExtra("snooze_minutes", 10)
        val progressive = intent.getBooleanExtra("progressive_volume", false)
        val repeatUntil = intent.getBooleanExtra("repeat_until_dismissed", false)
        val repeatInterval = intent.getIntExtra("repeat_interval_seconds", 30)
        val ringtoneUri = intent.getStringExtra("ringtone_uri") ?: "default"

        // KLJUCNO za Samsung: Probudi ekran ODMAH iz BroadcastReceiver
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = pm.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
            PowerManager.ACQUIRE_CAUSES_WAKEUP or
            PowerManager.ON_AFTER_RELEASE,
            "PodsetnikApp:AlarmWakeLock"
        )
        wakeLock.acquire(10000L) // drzi max 10 sekundi - samo da pokrene activity

        // 1. Pokreni AlarmActivity ODMAH iz receivera (ovo radi na Samsungu)
        val alarmActivityIntent = Intent(context, AlarmActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_NO_USER_ACTION
            )
            putExtra("reminder_id", reminderId)
            putExtra("reminder_title", title)
            putExtra("reminder_desc", desc)
            putExtra("snooze_minutes", snoozeMin)
        }
        try {
            context.startActivity(alarmActivityIntent)
        } catch (e: Exception) { /* fallback na servis */ }

        // 2. Pokreni servis za zvuk i notifikaciju
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("reminder_id", reminderId)
            putExtra("reminder_title", title)
            putExtra("reminder_desc", desc)
            putExtra("reminder_ring", ringTypeName)
            putExtra("reminder_lock_screen", showOnLock)
            putExtra("snooze_minutes", snoozeMin)
            putExtra("progressive_volume", progressive)
            putExtra("repeat_until_dismissed", repeatUntil)
            putExtra("repeat_interval_seconds", repeatInterval)
            putExtra("ringtone_uri", ringtoneUri)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // Oslobodi wakelock odmah - servis vise ne treba budan ekran
        handler.postDelayed({
            try { if (wakeLock.isHeld) wakeLock.release() } catch (e: Exception) {}
        }, 2000L)
    }
}
