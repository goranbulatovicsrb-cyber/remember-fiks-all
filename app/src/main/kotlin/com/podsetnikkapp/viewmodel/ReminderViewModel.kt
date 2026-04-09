package com.podsetnikkapp.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.podsetnikkapp.PodsetnikApplication
import com.podsetnikkapp.data.*
import com.podsetnikkapp.utils.AlarmScheduler
import com.podsetnikkapp.utils.GeofenceHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortOrder(val label: String) {
    DATE_ASC("Datum (rastuce)"),
    DATE_DESC("Datum (opadajuce)"),
    PRIORITY("Prioritet"),
    NAME("Ime (A-Z)"),
    COLOR("Boja")
}

class ReminderViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as PodsetnikApplication).repository
    private val context = application.applicationContext

    val allReminders = repo.allReminders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeReminders = repo.activeReminders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val pinnedReminders = repo.pinnedReminders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val favoriteReminders = repo.favoriteReminders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val archivedReminders = repo.archivedReminders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.DATE_ASC)
    val sortOrder = _sortOrder.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _groupByDay = MutableStateFlow(false)
    val groupByDay = _groupByDay.asStateFlow()

    val searchResults = _searchQuery.flatMapLatest { q ->
        if (q.isBlank()) repo.allReminders else repo.search(q)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sorted + filtered list
    val displayList = combine(allReminders, _sortOrder, _selectedCategory, _searchQuery, searchResults) {
        all, sort, cat, query, search ->
        val base = if (query.isNotBlank()) search else all
        val filtered = if (cat != null) base.filter { it.category == cat } else base
        when (sort) {
            SortOrder.DATE_ASC -> filtered.sortedBy { it.dateTimeMillis }
            SortOrder.DATE_DESC -> filtered.sortedByDescending { it.dateTimeMillis }
            SortOrder.PRIORITY -> filtered.sortedByDescending { it.priority.ordinal }
            SortOrder.NAME -> filtered.sortedBy { it.title.lowercase() }
            SortOrder.COLOR -> filtered.sortedBy { it.colorHex }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(q: String) { _searchQuery.value = q }
    fun setSelectedTab(tab: Int) { _selectedTab.value = tab }
    fun setSortOrder(sort: SortOrder) { _sortOrder.value = sort }
    fun setCategory(cat: Category?) { _selectedCategory.value = cat }
    fun toggleGroupByDay() { _groupByDay.value = !_groupByDay.value }

    suspend fun getReminderById(id: Long): Reminder? = repo.getById(id)

    fun saveReminder(reminder: Reminder) = viewModelScope.launch {
        val id = if (reminder.id == 0L) repo.insert(reminder) else { repo.update(reminder); reminder.id }
        val saved = reminder.copy(id = id)
        if (saved.isActive && saved.dateTimeMillis > System.currentTimeMillis()) {
            AlarmScheduler.schedule(context, saved)
        }
        if (saved.isPinned) schedulePinnedNotification(saved)
        if (saved.geofenceEnabled) GeofenceHelper.addGeofence(context, saved)
    }

    fun deleteReminder(reminder: Reminder) = viewModelScope.launch {
        AlarmScheduler.cancel(context, reminder.id)
        if (reminder.geofenceEnabled) GeofenceHelper.removeGeofence(context, reminder.id)
        repo.delete(reminder)
    }

    fun archiveReminder(reminder: Reminder) = viewModelScope.launch {
        AlarmScheduler.cancel(context, reminder.id)
        repo.setArchived(reminder.id, true)
    }

    fun unarchiveReminder(reminder: Reminder) = viewModelScope.launch {
        repo.setArchived(reminder.id, false)
        if (reminder.isActive && reminder.dateTimeMillis > System.currentTimeMillis()) {
            AlarmScheduler.schedule(context, reminder)
        }
    }

    fun toggleActive(reminder: Reminder) = viewModelScope.launch {
        val newActive = !reminder.isActive
        repo.setActive(reminder.id, newActive)
        if (newActive) AlarmScheduler.schedule(context, reminder)
        else AlarmScheduler.cancel(context, reminder.id)
    }

    fun togglePinned(reminder: Reminder) = viewModelScope.launch {
        val newPinned = !reminder.isPinned
        repo.setPinned(reminder.id, newPinned)
        if (newPinned) schedulePinnedNotification(reminder)
    }

    fun toggleFavorite(reminder: Reminder) = viewModelScope.launch {
        repo.setFavorite(reminder.id, !reminder.isFavorite)
    }

    fun snoozeReminder(reminder: Reminder, minutes: Int) = viewModelScope.launch {
        AlarmScheduler.scheduleSnooze(context, reminder, minutes)
    }

    private fun schedulePinnedNotification(reminder: Reminder) {
        val nm = context.getSystemService(android.app.NotificationManager::class.java)
        val intent = android.content.Intent(context, com.podsetnikkapp.MainActivity::class.java)
        val pi = android.app.PendingIntent.getActivity(
            context, reminder.id.toInt() + 9000, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        val sdf = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
        val notif = androidx.core.app.NotificationCompat.Builder(
            context, com.podsetnikkapp.utils.NotificationHelper.CHANNEL_PINNED
        )
            .setSmallIcon(com.podsetnikkapp.R.drawable.ic_notification)
            .setContentTitle("${reminder.category.emoji} ${reminder.title}")
            .setContentText(reminder.description.ifEmpty { sdf.format(java.util.Date(reminder.dateTimeMillis)) })
            .setOngoing(true)
            .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pi).build()
        try { nm.notify(reminder.id.toInt() + 9000, notif) } catch (e: Exception) {}
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ReminderViewModel(application) as T
        }
    }
}
