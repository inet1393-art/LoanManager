package com.reminder.loanmanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reminder.loanmanager.data.entity.InstallmentStatus
import com.reminder.loanmanager.ui.viewmodel.MainViewModel
import com.reminder.loanmanager.util.PersianDateUtil

@Composable
fun ReportsScreen(vm: MainViewModel) {
    val loans by vm.loans.collectAsStateWithLifecycle()
    val installments by vm.installments.collectAsStateWithLifecycle()
    val payments by vm.payments.collectAsStateWithLifecycle()

    val total = installments.sumOf { it.amount }
    val paid = installments.filter { it.status == InstallmentStatus.PAID }.sumOf { it.amount }
    val late = installments.filter { effectiveStatus(it) == InstallmentStatus.LATE }.sumOf { it.amount }
    val remaining = total - paid
    val progress = if (total == 0L) 0f else paid / total.toFloat()

    Scaffold(topBar = { AppTopBar("گزارش‌ها") }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("خلاصه کل", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("تعداد وام‌ها: ${loans.size} (تسویه‌شده: ${loans.count { it.isSettled }})")
                        Text("مجموع اقساط: ${PersianDateUtil.formatAmount(total)}")
                        Text("پرداخت‌شده: ${PersianDateUtil.formatAmount(paid)}")
                        Text("باقی‌مانده: ${PersianDateUtil.formatAmount(remaining)}")
                        Text("عقب‌افتاده: ${PersianDateUtil.formatAmount(late)}")
                        Text("تعداد پرداخت‌های ثبت‌شده: ${payments.size}")
                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
            item { Text("به تفکیک وام", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(loans, key = { it.id }) { loan ->
                val mine = installments.filter { it.loanId == loan.id }
                val mineTotal = mine.sumOf { it.amount }
                val minePaid = mine.filter { it.status == InstallmentStatus.PAID }.sumOf { it.amount }
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(loan.title, fontWeight = FontWeight.Bold)
                        Text("پرداخت‌شده ${PersianDateUtil.formatAmount(minePaid)} از ${PersianDateUtil.formatAmount(mineTotal)}")
                        LinearProgressIndicator(
                            progress = { if (mineTotal == 0L) 0f else minePaid / mineTotal.toFloat() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
