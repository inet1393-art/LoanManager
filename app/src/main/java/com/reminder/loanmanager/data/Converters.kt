package com.reminder.loanmanager.data

import androidx.room.TypeConverter
import com.reminder.loanmanager.data.entity.InstallmentStatus
import com.reminder.loanmanager.data.entity.ReminderType

class Converters {

    @TypeConverter
    fun fromInstallmentStatus(value: InstallmentStatus): String = value.name

    @TypeConverter
    fun toInstallmentStatus(value: String): InstallmentStatus =
        InstallmentStatus.valueOf(value)

    @TypeConverter
    fun fromReminderType(value: ReminderType): String = value.name

    @TypeConverter
    fun toReminderType(value: String): ReminderType =
        ReminderType.valueOf(value)
}
