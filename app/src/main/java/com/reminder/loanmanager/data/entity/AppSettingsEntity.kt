package com.reminder.loanmanager.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val darkMode: Boolean = false,
    val overlayReminderEnabled: Boolean = true,
    val defaultReminderDaysBefore: Int = 3,
    val defaultReminderHour: Int = 10,
    val defaultReminderMinute: Int = 0
)
