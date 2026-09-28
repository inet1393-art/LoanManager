package com.reminder.loanmanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.reminder.loanmanager.data.dao.AppSettingsDao
import com.reminder.loanmanager.data.dao.InstallmentDao
import com.reminder.loanmanager.data.dao.LoanDao
import com.reminder.loanmanager.data.dao.PaymentDao
import com.reminder.loanmanager.data.dao.ReminderDao
import com.reminder.loanmanager.data.entity.AppSettingsEntity
import com.reminder.loanmanager.data.entity.Installment
import com.reminder.loanmanager.data.entity.Loan
import com.reminder.loanmanager.data.entity.Payment
import com.reminder.loanmanager.data.entity.Reminder

@Database(
    entities = [
        Loan::class,
        Installment::class,
        Payment::class,
        Reminder::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun loanDao(): LoanDao
    abstract fun installmentDao(): InstallmentDao
    abstract fun paymentDao(): PaymentDao
    abstract fun reminderDao(): ReminderDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "loan_manager.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
