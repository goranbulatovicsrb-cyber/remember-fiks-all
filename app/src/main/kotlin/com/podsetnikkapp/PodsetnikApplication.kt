package com.podsetnikkapp

import android.app.Application
import com.podsetnikkapp.data.ReminderDatabase
import com.podsetnikkapp.data.ReminderRepository
import com.podsetnikkapp.utils.NotificationHelper

class PodsetnikApplication : Application() {
    val database by lazy { ReminderDatabase.getInstance(this) }
    val repository by lazy { ReminderRepository(database.reminderDao()) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
    }
}
