package com.reminder.loanmanager.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loans")
data class Loan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val lenderName: String,
    val loanType: String,
    val totalAmount: Long,
    val installmentCount: Int,
    val startDateEpochDay: Long,
    val note: String = "",
    val isSettled: Boolean = false,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)
