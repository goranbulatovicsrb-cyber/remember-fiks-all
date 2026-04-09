package com.podsetnikkapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE isArchived = 0 ORDER BY dateTimeMillis ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isActive = 1 AND isArchived = 0 ORDER BY dateTimeMillis ASC")
    fun getActiveReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isPinned = 1 AND isArchived = 0")
    fun getPinnedReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isFavorite = 1 AND isArchived = 0")
    fun getFavoriteReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isArchived = 1 ORDER BY dateTimeMillis DESC")
    fun getArchivedReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE category = :cat AND isArchived = 0")
    fun getByCategory(cat: String): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): Reminder?

    @Query("SELECT * FROM reminders WHERE isActive = 1 AND isArchived = 0 AND dateTimeMillis > :now ORDER BY dateTimeMillis ASC")
    suspend fun getUpcomingReminders(now: Long = System.currentTimeMillis()): List<Reminder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)

    @Query("UPDATE reminders SET isActive = :isActive WHERE id = :id")
    suspend fun updateActiveStatus(id: Long, isActive: Boolean)

    @Query("UPDATE reminders SET isPinned = :isPinned WHERE id = :id")
    suspend fun updatePinnedStatus(id: Long, isPinned: Boolean)

    @Query("UPDATE reminders SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFav: Boolean)

    @Query("UPDATE reminders SET isArchived = :archived WHERE id = :id")
    suspend fun updateArchivedStatus(id: Long, archived: Boolean)

    @Query("SELECT * FROM reminders WHERE isArchived = 0 AND (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')")
    fun searchReminders(query: String): Flow<List<Reminder>>
}
