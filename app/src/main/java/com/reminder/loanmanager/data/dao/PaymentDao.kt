package com.reminder.loanmanager.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.reminder.loanmanager.data.entity.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: Payment): Long

    @Delete
    suspend fun delete(payment: Payment)

    @Query("SELECT * FROM payments WHERE installmentId = :installmentId ORDER BY paymentDateEpochDay DESC")
    fun observeForInstallment(installmentId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payments ORDER BY paymentDateEpochDay DESC")
    fun observeAll(): Flow<List<Payment>>

    @Query("SELECT * FROM payments")
    suspend fun getAllOnce(): List<Payment>

    @Query("SELECT SUM(amount) FROM payments")
    fun observeTotalPaid(): Flow<Long?>
}
