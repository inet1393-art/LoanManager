package com.reminder.loanmanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reminder.loanmanager.ui.viewmodel.MainViewModel
import com.reminder.loanmanager.util.PersianDateUtil

@Composable
fun LoanDetailScreen(vm: MainViewModel, loanId: Long, onBack: () -> Unit) {
    val loans by vm.loans.collectAsStateWithLifecycle()
    val loan = loans.firstOrNull { it.id == loanId }
    val installmentsFlow = remember(loanId) { vm.installmentsForLoan(loanId) }
    val installments by installmentsFlow.collectAsStateWithLifecycle(emptyList())
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(topBar = { AppTopBar(loan?.title ?: "جزئیات وام", onBack) }) { padding ->
        if (loan == null) {
            Text("وام پیدا نشد.", Modifier.padding(padding).padding(16.dp))
            return@Scaffold
        }
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(loan.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("طرف/موسسه: ${loan.lenderName}")
                        Text("نوع: ${loan.loanType}")
                        Text("مبلغ کل: ${PersianDateUtil.formatAmount(loan.totalAmount)}")
                        Text("تعداد اقساط: ${loan.installmentCount}")
                        Text("شروع: ${PersianDateUtil.format(loan.startDateEpochDay)}")
                        if (loan.note.isNotBlank()) Text("توضیحات: ${loan.note}")
                        if (loan.isSettled) Text("این وام تسویه شده است.", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            items(installments, key = { it.id }) { inst ->
                InstallmentRow(inst, loan.title, onPay = { vm.payInstallment(inst) })
            }
            item {
                Button(
                    onClick = { confirmDelete = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("حذف وام") }
            }
        }
    }

    if (confirmDelete && loan != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("حذف وام") },
            text = { Text("این وام همراه با اقساط و یادآورهایش حذف می‌شود. ادامه می‌دهید؟") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    vm.deleteLoan(loan)
                    onBack()
                }) { Text("حذف") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("انصراف") } }
        )
    }
}
