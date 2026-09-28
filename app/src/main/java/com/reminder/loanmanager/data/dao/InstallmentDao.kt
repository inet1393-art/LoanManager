package com.reminder.loanmanager.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.reminder.loanmanager.data.entity.Installment
import com.reminder.loanmanager.data.entity.InstallmentStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface InstallmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(installment: Installment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(installments: List<Installment>)

    @Update
    suspend fun update(installment: Installment)

    @Delete
    suspend fun delete(installment: Installment)

    @Query("SELECT * FROM installments WHERE loanId = :loanId ORDER BY indexInLoan ASC")
    fun observeForLoan(loanId: Long): Flow<List<Installment>>

    @Query("SELECT * FROM installments WHERE loanId = :loanId ORDER BY indexInLoan ASC")
    suspend fun getForLoan(loanId: Long): List<Installment>

    @Query("SELECT * FROM installments WHERE id = :id")
    suspend fun getById(id: Long): Installment?

    @Query("SELECT * FROM installments ORDER BY dueDateEpochDay ASC")
    fun observeAll(): Flow<List<Installment>>

    @Query("SELECT * FROM installments WHERE status = :status ORDER BY dueDateEpochDay ASC")
    fun observeByStatus(status: InstallmentStatus): Flow<List<Installment>>

    @Query("SELECT * FROM installments WHERE status = :status AND dueDateEpochDay <= :todayEpochDay ORDER BY dueDateEpochDay ASC")
    fun observeOverdue(status: InstallmentStatus, todayEpochDay: Long): Flow<List<Installment>>

    @Query("UPDATE installments SET status = :status WHERE id = :id")
    suspend fun setStatus(id: Long, status: InstallmentStatus)

    @Query("UPDATE installments SET status = 'LATE' WHERE status = 'UNPAID' AND dueDateEpochDay < :todayEpochDay")
    suspend fun markLate(todayEpochDay: Long)

    @Query("SELECT * FROM installments")
    suspend fun getAllOnce(): List<Installment>

    @Query("SELECT COUNT(*) FROM installments WHERE loanId = :loanId")
    suspend fun countForLoan(loanId: Long): Int

    @Query("SELECT COUNT(*) FROM installments WHERE loanId = :loanId AND status = :status")
    suspend fun countForLoanByStatus(loanId: Long, status: InstallmentStatus): Int

    @Query("SELECT SUM(amount) FROM installments WHERE status = :status")
    fun observeTotalByStatus(status: InstallmentStatus): Flow<Long?>

    @Query("SELECT SUM(amount) FROM installments")
    fun observeTotalAmount(): Flow<Long?>
}
