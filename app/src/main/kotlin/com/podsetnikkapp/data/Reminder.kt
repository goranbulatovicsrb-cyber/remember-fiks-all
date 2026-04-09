package com.podsetnikkapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RepeatType(val label: String) {
    NONE("Jednom"), DAILY("Svaki dan"), WEEKLY("Svake sedmice"),
    MONTHLY("Svakog meseca"), YEARLY("Svake godine"), CUSTOM_DAYS("Odabrani dani")
}
enum class RingType(val label: String) {
    SILENT("Tiho"), VIBRATE("Vibracija"), SOUND("Zvuk"), SOUND_AND_VIBRATE("Zvuk i vibracija")
}
enum class Priority(val label: String, val color: Long) {
    LOW("Niska", 0xFF4CAF50), MEDIUM("Srednja", 0xFF2196F3),
    HIGH("Visoka", 0xFFFF9800), URGENT("Hitno", 0xFFF44336)
}
enum class ReminderColor(val label: String, val hex: Long) {
    PURPLE("Ljubicasta", 0xFF6750A4), BLUE("Plava", 0xFF1565C0),
    GREEN("Zelena", 0xFF2E7D32), ORANGE("Narandzasta", 0xFFE65100),
    RED("Crvena", 0xFFC62828), PINK("Roze", 0xFFAD1457),
    TEAL("Teal", 0xFF00695C), INDIGO("Indigo", 0xFF283593)
}
enum class Category(val label: String, val emoji: String) {
    NONE("Bez kategorije", "📋"), WORK("Posao", "💼"),
    PERSONAL("Licno", "👤"), HEALTH("Zdravlje", "🏥"),
    FAMILY("Porodica", "👨‍👩‍👧"), FINANCE("Finansije", "💰"),
    SPORT("Sport", "⚽"), EDUCATION("Edukacija", "📚"),
    TRAVEL("Putovanje", "✈️"), SHOPPING("Kupovina", "🛒"),
    BIRTHDAY("Rodjendan", "🎂"), HOLIDAY("Praznik", "🎉")
}

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val dateTimeMillis: Long,
    val repeatType: RepeatType = RepeatType.NONE,
    val ringType: RingType = RingType.SOUND_AND_VIBRATE,
    val ringtoneUri: String = "default",
    val priority: Priority = Priority.MEDIUM,
    val colorHex: Long = 0xFF6750A4,
    val isActive: Boolean = true,
    val showOnLockScreen: Boolean = true,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val category: Category = Category.NONE,
    val snoozeDurationMinutes: Int = 10,
    val createdAt: Long = System.currentTimeMillis(),
    val lastTriggeredAt: Long = 0L,
    // Geofence
    val geofenceEnabled: Boolean = false,
    val geofenceLat: Double = 0.0,
    val geofenceLng: Double = 0.0,
    val geofenceRadius: Float = 100f,
    val geofenceAddress: String = "",
    // === NEW: Advanced alarm options ===
    val isProgressiveVolume: Boolean = false,      // zvuk raste tiho -> glasno
    val repeatUntilDismissed: Boolean = false,     // ponavlja dok ne odbaciš
    val repeatIntervalSeconds: Int = 30,           // interval ponavljanja u sek
    val preAlertMinutes: String = "",              // "60,30,10" = upozori 60min, 30min, 10min ranije
    val weekDays: String = "",                     // "1,2,3,4,5" = pon-pet (0=ned,1=pon,...,6=sub)
)
