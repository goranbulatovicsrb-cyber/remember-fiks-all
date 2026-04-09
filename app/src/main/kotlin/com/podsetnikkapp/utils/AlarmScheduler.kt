package com.podsetnikkapp.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.podsetnikkapp.data.Reminder
import com.podsetnikkapp.data.RepeatType
import com.podsetnikkapp.receiver.AlarmReceiver
import java.util.Calendar

object AlarmScheduler {

    fun schedule(context: Context, reminder: Reminder) {
        if (!reminder.isActive) return
        val triggerTime = getNextTriggerTime(reminder)
        if (triggerTime > System.currentTimeMillis()) {
            scheduleAt(context, reminder, triggerTime, reminder.id.toInt())
        }
        // Pre-alerts
        if (reminder.preAlertMinutes.isNotBlank()) {
            reminder.preAlertMinutes.split(",").mapNotNull { it.trim().toIntOrNull() }
                .forEachIndexed { index, minutes ->
                    val preTime = triggerTime - (minutes * 60 * 1000L)
                    if (preTime > System.currentTimeMillis()) {
                        scheduleAt(context, reminder.copy(
                            title = "Za ${if(minutes>=60) "${minutes/60}h" else "${minutes}min"}: ${reminder.title}",
                            repeatUntilDismissed = false, isProgressiveVolume = false
                        ), preTime, (reminder.id * 100 + index + 1).toInt())
                    }
                }
        }
    }

    private fun scheduleAt(context: Context, reminder: Reminder, triggerTime: Long, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Otkaži prethodni alarm sa istim ID-om da ne zvoni dvaput
        try {
            val oldPi = PendingIntent.getBroadcast(
                context, requestCode, Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            oldPi?.let { alarmManager.cancel(it) }
        } catch (e: Exception) {}

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
            putExtra("reminder_title", reminder.title)
            putExtra("reminder_desc", reminder.description)
            putExtra("reminder_ring", reminder.ringType.name)
            putExtra("reminder_lock_screen", reminder.showOnLockScreen)
            putExtra("snooze_minutes", reminder.snoozeDurationMinutes)
            putExtra("progressive_volume", reminder.isProgressiveVolume)
            putExtra("repeat_until_dismissed", reminder.repeatUntilDismissed)
            putExtra("repeat_interval_seconds", reminder.repeatIntervalSeconds)
            putExtra("ringtone_uri", reminder.ringtoneUri)
        }
        val pi = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                // setAlarmClock - najvisi prioritet, prolazi kroz DND, garantuje fullscreen
                val alarmInfo = AlarmManager.AlarmClockInfo(triggerTime, pi)
                alarmManager.setAlarmClock(alarmInfo, pi)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pi)
            } else {
                // Pre Android 12 - setAlarmClock takodje radi odlicno
                val alarmInfo = AlarmManager.AlarmClockInfo(triggerTime, pi)
                alarmManager.setAlarmClock(alarmInfo, pi)
            }
        } catch (e: Exception) {
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pi)
            } catch (e2: Exception) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pi)
            }
        }
    }

    fun cancel(context: Context, reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        cancelById(context, alarmManager, reminderId.toInt())
        for (i in 1..10) cancelById(context, alarmManager, (reminderId * 100 + i).toInt())
    }

    private fun cancelById(context: Context, am: AlarmManager, id: Int) {
        try {
            val pi = PendingIntent.getBroadcast(
                context, id, Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            am.cancel(pi)
        } catch (e: Exception) {}
    }

    fun scheduleSnooze(context: Context, reminder: Reminder, minutes: Int) {
        scheduleAt(context, reminder.copy(repeatUntilDismissed = false, isProgressiveVolume = false),
            System.currentTimeMillis() + (minutes * 60 * 1000L), reminder.id.toInt())
    }

    fun getNextTriggerTime(reminder: Reminder): Long {
        val now = System.currentTimeMillis()
        var triggerTime = reminder.dateTimeMillis
        if (reminder.repeatType == RepeatType.CUSTOM_DAYS && reminder.weekDays.isNotBlank()) {
            val days = reminder.weekDays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
            if (days.isNotEmpty()) {
                val cal = Calendar.getInstance().apply { timeInMillis = reminder.dateTimeMillis }
                val targetHour = cal.get(Calendar.HOUR_OF_DAY)
                val targetMin = cal.get(Calendar.MINUTE)
                val search = Calendar.getInstance()
                for (offset in 0..7) {
                    search.timeInMillis = now
                    search.add(Calendar.DAY_OF_YEAR, offset)
                    if ((search.get(Calendar.DAY_OF_WEEK) - 1) in days) {
                        search.set(Calendar.HOUR_OF_DAY, targetHour)
                        search.set(Calendar.MINUTE, targetMin)
                        search.set(Calendar.SECOND, 0)
                        search.set(Calendar.MILLISECOND, 0)
                        if (search.timeInMillis > now) return search.timeInMillis
                    }
                }
            }
        }
        if (triggerTime <= now && reminder.repeatType != RepeatType.NONE && reminder.repeatType != RepeatType.CUSTOM_DAYS) {
            val cal = Calendar.getInstance().apply { timeInMillis = triggerTime }
            while (cal.timeInMillis <= now) {
                when (reminder.repeatType) {
                    RepeatType.DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
                    RepeatType.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                    RepeatType.MONTHLY -> cal.add(Calendar.MONTH, 1)
                    RepeatType.YEARLY -> cal.add(Calendar.YEAR, 1)
                    else -> break
                }
            }
            triggerTime = cal.timeInMillis
        }
        return triggerTime
    }
}
