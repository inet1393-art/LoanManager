package com.reminder.loanmanager.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.reminder.loanmanager.data.AppDatabase
import com.reminder.loanmanager.data.entity.AppSettingsEntity
import com.reminder.loanmanager.data.entity.Installment
import com.reminder.loanmanager.data.entity.Loan
import com.reminder.loanmanager.data.entity.Payment
import com.reminder.loanmanager.data.entity.Reminder
import com.reminder.loanmanager.data.entity.ReminderType
import com.reminder.loanmanager.notification.AlarmScheduler
import com.reminder.loanmanager.repository.LoanRepository
import com.reminder.loanmanager.util.BackupHelper
import com.reminder.loanmanager.util.PersianDateUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val ctx = app.applicationContext
    private val db = AppDatabase.getInstance(ctx)
    private val repo = LoanRepository(db)
    private val backup = BackupHelper(ctx, db)

    private fun <T> kotlinx.coroutines.flow.Flow<T>.state(initial: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initial)

    val loans: StateFlow<List<Loan>> = repo.observeLoans().state(emptyList())
    val installments: StateFlow<List<Installment>> = repo.observeAllInstallments().state(emptyList())
    val payments: StateFlow<List<Payment>> = repo.observeAllPayments().state(emptyList())
    val reminders: StateFlow<List<Reminder>> = repo.observeReminders().state(emptyList())
    val settings: StateFlow<AppSettingsEntity> =
        repo.observeSettings().map { it ?: AppSettingsEntity() }.state(AppSettingsEntity())

    val message = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getSettings()
            repo.refreshLateStatuses()
        }
    }

    fun consumeMessage() { message.value = null }

    fun installmentsForLoan(loanId: Long) = repo.observeInstallmentsForLoan(loanId)

    fun addLoan(
        title: String, lender: String, type: String, total: Long,
        count: Int, startEpochDay: Long, note: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val loanId = repo.createLoanWithInstallments(
                Loan(
                    title = title, lenderName = lender, loanType = type,
                    totalAmount = total, installmentCount = count,
                    startDateEpochDay = startEpochDay, note = note
                )
            )
            val s = repo.getSettings()
            repo.getInstallmentsForLoan(loanId).forEach { inst ->
                val day = LocalDate.ofEpochDay(inst.dueDateEpochDay)
                    .minusDays(s.defaultReminderDaysBefore.toLong())
                val trigger = day.atTime(s.defaultReminderHour, s.defaultReminderMinute)
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val reminder = Reminder(
                    installmentId = inst.id,
                    title = "قسط ${inst.indexInLoan} وام $title",
                    note = "مبلغ ${PersianDateUtil.formatAmount(inst.amount)} - سررسید ${PersianDateUtil.format(inst.dueDateEpochDay)}",
                    triggerAtEpochMillis = trigger,
                    type = ReminderType.UPCOMING
                )
                val id = repo.createReminder(reminder)
                AlarmScheduler.schedule(ctx, reminder.copy(id = id))
            }
        }
    }

    fun deleteLoan(loan: Loan) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getRemindersForLoan(loan.id).forEach { AlarmScheduler.cancel(ctx, it) }
            repo.deleteLoan(loan)
        }
    }

    fun payInstallment(installment: Installment) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.payInstallment(installment, LocalDate.now().toEpochDay())
            repo.getRemindersForInstallment(installment.id).forEach {
                AlarmScheduler.cancel(ctx, it)
                repo.deleteReminder(it)
            }
        }
    }

    fun addCustomReminder(title: String, note: String, triggerMillis: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val r = Reminder(
                installmentId = null, title = title, note = note,
                triggerAtEpochMillis = triggerMillis, type = ReminderType.CUSTOM
            )
            val id = repo.createReminder(r)
            AlarmScheduler.schedule(ctx, r.copy(id = id))
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            AlarmScheduler.cancel(ctx, reminder)
            repo.deleteReminder(reminder)
        }
    }

    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = reminder.copy(isEnabled = !reminder.isEnabled)
            repo.updateReminder(updated)
            if (updated.isEnabled) AlarmScheduler.schedule(ctx, updated)
            else AlarmScheduler.cancel(ctx, updated)
        }
    }

    fun updateSettings(newSettings: AppSettingsEntity) {
        viewModelScope.launch(Dispatchers.IO) { repo.updateSettings(newSettings) }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            message.value = try {
                backup.export(uri); "پشتیبان با موفقیت ذخیره شد"
            } catch (e: Exception) {
                "خطا در پشتیبان‌گیری"
            }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            message.value = try {
                backup.restore(uri)
                repo.getSettings()
                repo.getAllEnabledReminders().forEach { AlarmScheduler.schedule(ctx, it) }
                "بازیابی با موفقیت انجام شد"
            } catch (e: Exception) {
                "فایل پشتیبان نامعتبر است"
            }
        }
    }
}
