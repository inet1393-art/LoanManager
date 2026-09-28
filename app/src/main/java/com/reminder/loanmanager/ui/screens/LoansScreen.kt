package com.reminder.loanmanager.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
fun LoansScreen(vm: MainViewModel, onAdd: () -> Unit, onOpen: (Long) -> Unit) {
    val loans by vm.loans.collectAsStateWithLifecycle()
    val installments by vm.installments.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { AppTopBar("وام‌ها") },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) { Icon(Icons.Default.Add, contentDescription = "افزودن وام") }
        }
    ) { padding ->
        if (loans.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                Text("هنوز وامی ثبت نکرده‌اید. با دکمه + یک وام اضافه کنید.")
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(loans, key = { it.id }) { loan ->
                    val mine = installments.filter { it.loanId == loan.id }
                    val paidCount = mine.count { it.status == InstallmentStatus.PAID }
                    val progress = if (mine.isEmpty()) 0f else paidCount / mine.size.toFloat()
                    Card(Modifier.fillMaxWidth().clickable { onOpen(loan.id) }) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(loan.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("${loan.lenderName} - ${loan.loanType}")
                            Text("مبلغ: ${PersianDateUtil.formatAmount(loan.totalAmount)}")
                            Text("پرداخت‌شده: $paidCount از ${mine.size} قسط")
                            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                            if (loan.isSettled) Text("تسویه‌شده", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
