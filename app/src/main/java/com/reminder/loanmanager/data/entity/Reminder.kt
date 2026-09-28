package com.reminder.loanmanager.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ReminderType {
    OVERDUE,
    UPCOMING,
    CUSTOM
}

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = Installment::class,
            parentColumns = ["id"],
            childColumns = ["installmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("installmentId")]
)
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val installmentId: Long?,
    val title: String,
    val note: String = "",
    val triggerAtEpochMillis: Long,
    val type: ReminderType = ReminderType.CUSTOM,
    val isEnabled: Boolean = true
)
