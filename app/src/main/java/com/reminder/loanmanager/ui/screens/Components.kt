package com.reminder.loanmanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.reminder.loanmanager.data.entity.Installment
import com.reminder.loanmanager.data.entity.InstallmentStatus
import com.reminder.loanmanager.util.PersianDateUtil
import java.time.LocalDate

fun effectiveStatus(i: Installment): InstallmentStatus =
    if (i.status == InstallmentStatus.UNPAID && i.dueDateEpochDay < LocalDate.now().toEpochDay())
        InstallmentStatus.LATE else i.status

fun statusLabel(s: InstallmentStatus): String = when (s) {
    InstallmentStatus.PAID -> "پرداخت‌شده"
    InstallmentStatus.UNPAID -> "پرداخت‌نشده"
    InstallmentStatus.LATE -> "عقب‌افتاده"
    InstallmentStatus.CANCELLED -> "لغوشده"
}

fun statusColor(s: InstallmentStatus): Color = when (s) {
    InstallmentStatus.PAID -> Color(0xFF2E7D5B)
    InstallmentStatus.UNPAID -> Color(0xFFE08A2A)
    InstallmentStatus.LATE -> Color(0xFFD64545)
    InstallmentStatus.CANCELLED -> Color(0xFF888888)
}

/** Converts Persian/Arabic-Indic digits to Latin so numeric input works with any keyboard. */
fun String.latinDigits(): String = map { c ->
    when (c) {
        in '۰'..'۹' -> '0' + (c - '۰')
        in '٠'..'٩' -> '0' + (c - '٠')
        else -> c
    }
}.joinToString("")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(title: String, onBack: (() -> Unit)? = null) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                }
            }
        }
    )
}

@Composable
fun InstallmentRow(
    installment: Installment,
    title: String,
    onPay: (() -> Unit)?
) {
    val status = effectiveStatus(installment)
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("$title - قسط ${installment.indexInLoan}", fontWeight = FontWeight.Bold)
                Text("سررسید: ${PersianDateUtil.format(installment.dueDateEpochDay)}")
                Text(PersianDateUtil.formatAmount(installment.amount))
                Text(statusLabel(status), color = statusColor(status), style = MaterialTheme.typography.labelLarge)
            }
            if (onPay != null && status != InstallmentStatus.PAID) {
                Button(onClick = onPay) { Text("پرداخت شد") }
            }
        }
    }
}
