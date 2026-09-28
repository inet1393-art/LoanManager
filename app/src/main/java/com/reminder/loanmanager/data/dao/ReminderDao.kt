package com.reminder.loanmanager.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.reminder.loanmanager.data.entity.Reminder
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: Reminder): Long

    @Update
    suspend fun update(reminder: Reminder)

    @Delete
    suspend fun delete(reminder: Reminder)

    @Query("SELECT * FROM reminders ORDER BY triggerAtEpochMillis ASC")
    fun observeAll(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: Long): Reminder?

    @Query("SELECT * FROM reminders WHERE isEnabled = 1 ORDER BY triggerAtEpochMillis ASC")
    suspend fun getAllEnabled(): List<Reminder>

    @Query("SELECT * FROM reminders")
    suspend fun getAllOnce(): List<Reminder>

    @Query("SELECT * FROM reminders WHERE installmentId = :installmentId")
    suspend fun getForInstallment(installmentId: Long): List<Reminder>

    @Query("SELECT r.* FROM reminders r INNER JOIN installments i ON r.installmentId = i.id WHERE i.loanId = :loanId")
    suspend fun getForLoan(loanId: Long): List<Reminder>

    @Query("SELECT * FROM reminders WHERE installmentId = :installmentId")
    fun observeForInstallment(installmentId: Long): Flow<List<Reminder>>
}
