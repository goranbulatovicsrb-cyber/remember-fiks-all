package com.podsetnikkapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager

object NotificationHelper {
    const val CHANNEL_ALARM = "alarm_channel"
    const val CHANNEL_REMINDER = "reminder_channel"
    const val CHANNEL_PINNED = "pinned_channel"

    fun createNotificationChannels(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // ALARM kanal - mora biti MAX importance za full screen
        val alarmChannel = NotificationChannel(
            CHANNEL_ALARM,
            "Alarmi podsetnika",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alarm koji se oglasava na vreme"
            enableVibration(true)
            enableLights(true)
            setBypassDnd(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            val audioAttr = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), audioAttr)
        }
        nm.createNotificationChannel(alarmChannel)

        NotificationChannel(CHANNEL_REMINDER, "Podsetnici", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Opsta podsetnik obavestenja"
            nm.createNotificationChannel(this)
        }

        NotificationChannel(CHANNEL_PINNED, "Prikvaceni podsetnici", NotificationManager.IMPORTANCE_LOW).apply {
            description = "Stalni podsetnici na zaklj. ekranu"
            setShowBadge(false)
            nm.createNotificationChannel(this)
        }
    }
}
