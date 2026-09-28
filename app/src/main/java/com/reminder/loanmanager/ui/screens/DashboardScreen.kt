package com.reminder.loanmanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
fun DashboardScreen(vm: MainViewModel, onOpenInstallments: () -> Unit) {
    val loans by vm.loans.collectAsStateWithLifecycle()
    val installments by vm.installments.collectAsStateWithLifecycle()
    val loanTitles = loans.associate { it.id to it.title }

    val remaining = installments.filter { effectiveStatus(it) != InstallmentStatus.PAID }.sumOf { it.amount }
    val paid = installments.filter { it.status == InstallmentStatus.PAID }.sumOf { it.amount }
    val lateCount = installments.count { effectiveStatus(it) == InstallmentStatus.LATE }
    val upcoming = installments
        .filter { effectiveStatus(it) != InstallmentStatus.PAID }
        .sortedBy { it.dueDateEpochDay }
        .take(5)

    Scaffold(topBar = { AppTopBar("داشبورد") }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("امروز: ${PersianDateUtil.today()}", style = MaterialTheme.typography.titleMedium)
            }
            item {
                SummaryCard("مجموع باقی‌مانده", PersianDateUtil.formatAmount(remaining))
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard("پرداخت‌شده", PersianDateUtil.formatAmount(paid), Modifier.weight(1f))
                    SummaryCard("اقساط عقب‌افتاده", "$lateCount", Modifier.weight(1f))
                }
            }
            item {
                Text("اقساط پیش رو", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            if (upcoming.isEmpty()) {
                item { Text("قسط بازی وجود ندارد.") }
            }
            items(upcoming, key = { it.id }) { inst ->
                InstallmentRow(inst, loanTitles[inst.loanId] ?: "وام", onPay = { vm.payInstallment(inst) })
            }
            item {
                Spacer(Modifier.height(4.dp))
                Button(onClick = onOpenInstallments, modifier = Modifier.fillMaxWidth()) {
                    Text("مشاهده همه اقساط")
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, modifier: Modifier = Modifier.fillMaxWidth()) {
    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}
