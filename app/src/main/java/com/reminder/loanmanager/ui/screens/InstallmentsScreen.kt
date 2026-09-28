package com.reminder.loanmanager.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reminder.loanmanager.data.entity.InstallmentStatus
import com.reminder.loanmanager.ui.viewmodel.MainViewModel

@Composable
fun InstallmentsScreen(vm: MainViewModel, onBack: () -> Unit) {
    val loans by vm.loans.collectAsStateWithLifecycle()
    val installments by vm.installments.collectAsStateWithLifecycle()
    val titles = loans.associate { it.id to it.title }
    var filter by remember { mutableStateOf<InstallmentStatus?>(null) }

    val shown = installments.filter { filter == null || effectiveStatus(it) == filter }

    Scaffold(topBar = { AppTopBar("اقساط", onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(filter == null, { filter = null }, label = { Text("همه") })
                FilterChip(filter == InstallmentStatus.UNPAID, { filter = InstallmentStatus.UNPAID }, label = { Text("پرداخت‌نشده") })
                FilterChip(filter == InstallmentStatus.LATE, { filter = InstallmentStatus.LATE }, label = { Text("عقب‌افتاده") })
                FilterChip(filter == InstallmentStatus.PAID, { filter = InstallmentStatus.PAID }, label = { Text("پرداخت‌شده") })
            }
            if (shown.isEmpty()) Text("موردی یافت نشد.")
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(shown, key = { it.id }) { inst ->
                    InstallmentRow(inst, titles[inst.loanId] ?: "وام", onPay = { vm.payInstallment(inst) })
                }
            }
        }
    }
}
