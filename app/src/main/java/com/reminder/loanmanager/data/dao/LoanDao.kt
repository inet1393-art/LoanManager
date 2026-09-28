package com.reminder.loanmanager.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.reminder.loanmanager.data.entity.Loan
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(loan: Loan): Long

    @Update
    suspend fun update(loan: Loan)

    @Delete
    suspend fun delete(loan: Loan)

    @Query("SELECT * FROM loans ORDER BY createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<Loan>>

    @Query("SELECT * FROM loans WHERE id = :loanId")
    fun observeById(loanId: Long): Flow<Loan?>

    @Query("SELECT * FROM loans")
    suspend fun getAllOnce(): List<Loan>

    @Query("SELECT * FROM loans WHERE id = :loanId")
    suspend fun getById(loanId: Long): Loan?

    @Query("SELECT COUNT(*) FROM loans WHERE isSettled = 0")
    fun observeActiveLoanCount(): Flow<Int>

    @Query("UPDATE loans SET isSettled = :settled WHERE id = :loanId")
    suspend fun setSettled(loanId: Long, settled: Boolean)
}
