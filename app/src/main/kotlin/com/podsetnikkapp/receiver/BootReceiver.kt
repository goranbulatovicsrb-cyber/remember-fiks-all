package com.podsetnikkapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.podsetnikkapp.PodsetnikApplication
import com.podsetnikkapp.utils.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val app = context.applicationContext as PodsetnikApplication
            CoroutineScope(Dispatchers.IO).launch {
                val reminders = app.repository.getUpcoming()
                reminders.forEach { reminder ->
                    if (reminder.isActive) {
                        AlarmScheduler.schedule(context, reminder)
                    }
                }
            }
        }
    }
}
