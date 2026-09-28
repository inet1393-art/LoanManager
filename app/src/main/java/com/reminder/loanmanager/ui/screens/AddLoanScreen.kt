package com.reminder.loanmanager.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.reminder.loanmanager.ui.viewmodel.MainViewModel
import com.reminder.loanmanager.util.PersianDateUtil

private val loanTypes = listOf("بانکی", "قرض‌الحسنه", "شخصی", "خرید اقساطی")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLoanScreen(vm: MainViewModel, onDone: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var lender by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(loanTypes[0]) }
    var amount by remember { mutableStateOf("") }
    var count by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(PersianDateUtil.today().toString()) }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { AppTopBar("افزودن وام", onDone) }) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(title, { title = it }, label = { Text("عنوان وام") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(lender, { lender = it }, label = { Text("نام طرف/موسسه") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Text("نوع وام")
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                loanTypes.forEach { t ->
                    FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t) })
                }
            }
            OutlinedTextField(
                amount, { amount = it.latinDigits().filter { c -> c.isDigit() } },
                label = { Text("مبلغ کل (تومان)") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                count, { count = it.latinDigits().filter { c -> c.isDigit() } },
                label = { Text("تعداد اقساط") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                startDate, { startDate = it }, label = { Text("تاریخ اولین قسط (مثال 1405/07/10)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(note, { note = it }, label = { Text("توضیحات") }, modifier = Modifier.fillMaxWidth())

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val total = amount.toLongOrNull()
                    val n = count.toIntOrNull()
                    val epoch = PersianDateUtil.parseToEpochDay(startDate)
                    error = when {
                        title.isBlank() -> "عنوان وام را وارد کنید"
                        total == null || total <= 0 -> "مبلغ نامعتبر است"
                        n == null || n <= 0 || n > 600 -> "تعداد اقساط نامعتبر است"
                        epoch == null -> "تاریخ نامعتبر است"
                        else -> null
                    }
                    if (error == null) {
                        vm.addLoan(title.trim(), lender.trim(), type, total!!, n!!, epoch!!, note.trim())
                        onDone()
                    }
                }
            ) { Text("ذخیره") }
        }
    }
}
