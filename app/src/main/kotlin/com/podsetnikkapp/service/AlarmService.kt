package com.podsetnikkapp.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.podsetnikkapp.R
import com.podsetnikkapp.data.RingType
import com.podsetnikkapp.ui.screens.AlarmActivity
import com.podsetnikkapp.utils.NotificationHelper

class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var reminderId: Long = -1L
    private val handler = Handler(Looper.getMainLooper())
    private var isProgressiveVolume = false
    private var repeatUntilDismissed = false
    private var repeatIntervalSeconds = 30
    private var ringTypeName = "SOUND_AND_VIBRATE"
    private var ringtoneUri = "default"
    private var isRunning = false
    private var volumeStep = 0
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "DISMISS") {
            stopAlarmNow()
            return START_NOT_STICKY
        }

        reminderId = intent?.getLongExtra("reminder_id", -1L) ?: -1L
        val title = intent?.getStringExtra("reminder_title") ?: "Podsetnik"
        val desc = intent?.getStringExtra("reminder_desc") ?: ""
        val snoozeMin = intent?.getIntExtra("snooze_minutes", 10) ?: 10
        ringTypeName = intent?.getStringExtra("reminder_ring") ?: "SOUND_AND_VIBRATE"
        isProgressiveVolume = intent?.getBooleanExtra("progressive_volume", false) ?: false
        repeatUntilDismissed = intent?.getBooleanExtra("repeat_until_dismissed", false) ?: false
        repeatIntervalSeconds = intent?.getIntExtra("repeat_interval_seconds", 30) ?: 30
        ringtoneUri = intent?.getStringExtra("ringtone_uri") ?: "default"

        val ringType = try { RingType.valueOf(ringTypeName) } catch (e: Exception) { RingType.SOUND_AND_VIBRATE }

        // Drzi ekran budan dok alarm traje
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "PodsetnikApp:AlarmServiceWakeLock"
        )
        wakeLock?.acquire(5 * 60 * 1000L) // 5 minuta max

        // Full screen intent
        val alarmIntent = Intent(this, AlarmActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("reminder_id", reminderId)
            putExtra("reminder_title", title)
            putExtra("reminder_desc", desc)
            putExtra("snooze_minutes", snoozeMin)
        }
        val fullScreenPI = PendingIntent.getActivity(
            this, reminderId.toInt(), alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val dismissPI = PendingIntent.getService(
            this, (reminderId + 1000).toInt(),
            Intent(this, AlarmService::class.java).apply { action = "DISMISS" },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ALARM)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(desc.ifEmpty { "Vreme je za vas podsetnik!" })
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(desc.ifEmpty { "Vreme je za vas podsetnik!" })
                .setBigContentTitle(title))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPI, true)
            .setContentIntent(fullScreenPI)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(R.drawable.ic_notification, "Odbaci", dismissPI)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(0xFF9D4EDD.toInt())
            .build()
            .also { it.flags = it.flags or Notification.FLAG_INSISTENT }

        startForeground(reminderId.toInt(), notification)

        // Stop prethodni ako postoji
        stopSound()
        stopVibration()

        isRunning = true

        // Pokreni zvuk
        if (ringType == RingType.SOUND || ringType == RingType.SOUND_AND_VIBRATE) {
            if (isProgressiveVolume) {
                startProgressiveSound()
            } else {
                startSound()
            }
        }

        // Pokreni vibraciju
        if (ringType == RingType.VIBRATE || ringType == RingType.SOUND_AND_VIBRATE) {
            startVibration()
        }

        // Repeat cycle ako je ukljuceno
        if (repeatUntilDismissed) {
            scheduleRepeat()
        }

        return START_NOT_STICKY
    }

    private fun startSound() {
        try {
            val uri: Uri = when {
                ringtoneUri.isNotBlank() && ringtoneUri != "default" ->
                    Uri.parse(ringtoneUri)
                else ->
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                try {
                    setDataSource(applicationContext, uri)
                } catch (e: Exception) {
                    // Fallback na default alarm
                    val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    setDataSource(applicationContext, defaultUri)
                }
                isLooping = true  // Loopira dok ne odbaci!
                setVolume(1.0f, 1.0f)
                prepare()
                start()
            }
        } catch (e: Exception) {
            // Zadnji fallback
            try {
                mediaPlayer = MediaPlayer().apply {
                    val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    setDataSource(applicationContext, uri)
                    setAudioAttributes(AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM).build())
                    isLooping = true
                    prepare()
                    start()
                }
            } catch (e2: Exception) { /* ignore */ }
        }
    }

    private fun startProgressiveSound() {
        volumeStep = 0
        try {
            mediaPlayer = MediaPlayer().apply {
                val uri = if (ringtoneUri.isNotBlank() && ringtoneUri != "default")
                    Uri.parse(ringtoneUri)
                else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM).build())
                setDataSource(applicationContext, uri)
                isLooping = true
                setVolume(0.1f, 0.1f)
                prepare()
                start()
            }
        } catch (e: Exception) { startSound(); return }

        // Povecavaj glasnocu svakih 3s
        val volRunnable = object : Runnable {
            override fun run() {
                if (!isRunning || volumeStep >= 10) return
                volumeStep++
                val vol = volumeStep / 10f
                try { mediaPlayer?.setVolume(vol, vol) } catch (e: Exception) {}
                handler.postDelayed(this, 3000L)
            }
        }
        handler.postDelayed(volRunnable, 3000L)
    }

    private fun scheduleRepeat() {
        handler.postDelayed({
            if (isRunning) {
                stopSound()
                startSound()
                scheduleRepeat()
            }
        }, repeatIntervalSeconds * 1000L)
    }

    private fun startVibration() {
        try {
            val pattern = longArrayOf(0, 800, 400, 800, 400, 1200)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getSystemService(VibratorManager::class.java)
                    ?.defaultVibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createWaveform(pattern, 0))
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(pattern, 0)
                }
            }
        } catch (e: Exception) {}
    }

    private fun stopVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getSystemService(VibratorManager::class.java)?.defaultVibrator?.cancel()
            } else {
                @Suppress("DEPRECATION")
                (getSystemService(VIBRATOR_SERVICE) as Vibrator).cancel()
            }
        } catch (e: Exception) {}
    }

    private fun stopSound() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
            mediaPlayer = null
        } catch (e: Exception) {}
    }

    fun stopAlarmNow() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
        stopSound()
        stopVibration()
        try { wakeLock?.apply { if (isHeld) release() } } catch (e: Exception) {}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        if (reminderId > 0) {
            try { NotificationManagerCompat.from(this).cancel(reminderId.toInt()) } catch (e: Exception) {}
        }
        stopSelf()
    }

    override fun onDestroy() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
        stopSound()
        stopVibration()
        try { wakeLock?.apply { if (isHeld) release() } } catch (e: Exception) {}
        super.onDestroy()
    }
}
