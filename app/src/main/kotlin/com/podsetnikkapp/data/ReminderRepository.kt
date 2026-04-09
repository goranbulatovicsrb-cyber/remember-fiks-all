package com.podsetnikkapp.data

import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val dao: ReminderDao) {
    val allReminders: Flow<List<Reminder>> = dao.getAllReminders()
    val activeReminders: Flow<List<Reminder>> = dao.getActiveReminders()
    val pinnedReminders: Flow<List<Reminder>> = dao.getPinnedReminders()
    val favoriteReminders: Flow<List<Reminder>> = dao.getFavoriteReminders()
    val archivedReminders: Flow<List<Reminder>> = dao.getArchivedReminders()

    suspend fun getById(id: Long): Reminder? = dao.getReminderById(id)
    suspend fun getUpcoming(): List<Reminder> = dao.getUpcomingReminders()

    suspend fun insert(reminder: Reminder): Long = dao.insertReminder(reminder)
    suspend fun update(reminder: Reminder) = dao.updateReminder(reminder)
    suspend fun delete(reminder: Reminder) = dao.deleteReminder(reminder)
    suspend fun deleteById(id: Long) = dao.deleteReminderById(id)
    suspend fun setActive(id: Long, active: Boolean) = dao.updateActiveStatus(id, active)
    suspend fun setPinned(id: Long, pinned: Boolean) = dao.updatePinnedStatus(id, pinned)
    suspend fun setFavorite(id: Long, fav: Boolean) = dao.updateFavoriteStatus(id, fav)
    suspend fun setArchived(id: Long, archived: Boolean) = dao.updateArchivedStatus(id, archived)
    fun getByCategory(cat: Category): Flow<List<Reminder>> = dao.getByCategory(cat.name)
    fun search(query: String): Flow<List<Reminder>> = dao.searchReminders(query)
}
