package com.reminder.loanmanager.repository

import com.reminder.loanmanager.data.AppDatabase
import com.reminder.loanmanager.data.entity.AppSettingsEntity
import com.reminder.loanmanager.data.entity.Installment
import com.reminder.loanmanager.data.entity.InstallmentStatus
import com.reminder.loanmanager.data.entity.Loan
import com.reminder.loanmanager.data.entity.Payment
import com.reminder.loanmanager.data.entity.Reminder
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Single entry point for all data access. Keeps ViewModels free of DAO details
 * and centralizes the rules that connect loans, installments, payments and reminders.
 */
class LoanRepository(private val db: AppDatabase) {

    private val loanDao = db.loanDao()
    private val installmentDao = db.installmentDao()
    private val paymentDao = db.paymentDao()
    private val reminderDao = db.reminderDao()
    private val settingsDao = db.appSettingsDao()

    // ---- Loans ----

    fun observeLoans(): Flow<List<Loan>> = loanDao.observeAll()

    fun observeLoan(loanId: Long): Flow<Loan?> = loanDao.observeById(loanId)

    fun observeActiveLoanCount(): Flow<Int> = loanDao.observeActiveLoanCount()

    /**
     * Creates a loan and automatically generates its installments,
     * spaced one Jalali month apart starting from [startDateEpochDay].
     */
    suspend fun createLoanWithInstallments(loan: Loan): Long {
        val loanId = loanDao.insert(loan)
        val perInstallment = loan.totalAmount / loan.installmentCount
        val remainder = loan.totalAmount - (perInstallment * loan.installmentCount)

        val startDate = LocalDate.ofEpochDay(loan.startDateEpochDay)
        val installments = (1..loan.installmentCount).map { index ->
            val dueDate = startDate.plusMonths((index - 1).toLong())
            Installment(
                loanId = loanId,
                indexInLoan = index,
                dueDateEpochDay = dueDate.toEpochDay(),
                amount = if (index == loan.installmentCount) perInstallment + remainder else perInstallment,
                status = InstallmentStatus.UNPAID
            )
        }
        installmentDao.insertAll(installments)
        return loanId
    }

    suspend fun updateLoan(loan: Loan) = loanDao.update(loan)

    suspend fun deleteLoan(loan: Loan) = loanDao.delete(loan)

    suspend fun getLoan(loanId: Long): Loan? = loanDao.getById(loanId)

    suspend fun markLoanSettled(loanId: Long, settled: Boolean) =
        loanDao.setSettled(loanId, settled)

    // ---- Installments ----

    fun observeInstallmentsForLoan(loanId: Long): Flow<List<Installment>> =
        installmentDao.observeForLoan(loanId)

    fun observeAllInstallments(): Flow<List<Installment>> = installmentDao.observeAll()

    fun observeInstallmentsByStatus(status: InstallmentStatus): Flow<List<Installment>> =
        installmentDao.observeByStatus(status)

    fun observeOverdueInstallments(): Flow<List<Installment>> =
        installmentDao.observeOverdue(InstallmentStatus.UNPAID, LocalDate.now().toEpochDay())

    suspend fun getInstallmentsForLoan(loanId: Long): List<Installment> =
        installmentDao.getForLoan(loanId)

    suspend fun getInstallment(id: Long): Installment? = installmentDao.getById(id)

    /**
     * Records a payment for an installment and marks it paid. Also checks
     * whether the parent loan is now fully settled.
     */
    suspend fun payInstallment(installment: Installment, paymentDateEpochDay: Long, note: String = "") {
        paymentDao.insert(
            Payment(
                installmentId = installment.id,
                paymentDateEpochDay = paymentDateEpochDay,
                amount = installment.amount,
                note = note
            )
        )
        installmentDao.setStatus(installment.id, InstallmentStatus.PAID)

        val total = installmentDao.countForLoan(installment.loanId)
        val paid = installmentDao.countForLoanByStatus(installment.loanId, InstallmentStatus.PAID)
        if (total > 0 && total == paid) {
            loanDao.setSettled(installment.loanId, true)
        }
    }

    suspend fun setInstallmentStatus(id: Long, status: InstallmentStatus) =
        installmentDao.setStatus(id, status)

    suspend fun refreshLateStatuses() {
        installmentDao.markLate(LocalDate.now().toEpochDay())
    }

    // ---- Payments ----

    fun observePaymentsForInstallment(installmentId: Long): Flow<List<Payment>> =
        paymentDao.observeForInstallment(installmentId)

    fun observeAllPayments(): Flow<List<Payment>> = paymentDao.observeAll()

    fun observeTotalPaid(): Flow<Long?> = paymentDao.observeTotalPaid()

    // ---- Reminders ----

    fun observeReminders(): Flow<List<Reminder>> = reminderDao.observeAll()

    fun observeRemindersForInstallment(installmentId: Long): Flow<List<Reminder>> =
        reminderDao.observeForInstallment(installmentId)

    suspend fun createReminder(reminder: Reminder): Long = reminderDao.insert(reminder)

    suspend fun updateReminder(reminder: Reminder) = reminderDao.update(reminder)

    suspend fun deleteReminder(reminder: Reminder) = reminderDao.delete(reminder)

    suspend fun getAllEnabledReminders(): List<Reminder> = reminderDao.getAllEnabled()

    suspend fun getReminder(id: Long): Reminder? = reminderDao.getById(id)

    suspend fun getRemindersForInstallment(installmentId: Long): List<Reminder> =
        reminderDao.getForInstallment(installmentId)

    suspend fun getRemindersForLoan(loanId: Long): List<Reminder> = reminderDao.getForLoan(loanId)

    // ---- Reports ----

    fun observeTotalAmount(): Flow<Long?> = installmentDao.observeTotalAmount()

    fun observeTotalByStatus(status: InstallmentStatus): Flow<Long?> =
        installmentDao.observeTotalByStatus(status)

    // ---- Settings ----

    fun observeSettings(): Flow<AppSettingsEntity?> = settingsDao.observe()

    suspend fun getSettings(): AppSettingsEntity =
        settingsDao.get() ?: AppSettingsEntity().also { settingsDao.insert(it) }

    suspend fun updateSettings(settings: AppSettingsEntity) = settingsDao.insert(settings)
}
