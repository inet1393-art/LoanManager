package com.reminder.loanmanager.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class InstallmentStatus {
    PAID,
    UNPAID,
    LATE,
    CANCELLED
}

@Entity(
    tableName = "installments",
    foreignKeys = [
        ForeignKey(
            entity = Loan::class,
            parentColumns = ["id"],
            childColumns = ["loanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("loanId")]
)
data class Installment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val loanId: Long,
    val indexInLoan: Int,
    val dueDateEpochDay: Long,
    val amount: Long,
    val status: InstallmentStatus = InstallmentStatus.UNPAID,
    val note: String = ""
)
