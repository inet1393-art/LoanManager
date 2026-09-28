package com.reminder.loanmanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import com.reminder.loanmanager.ui.viewmodel.MainViewModel
import com.reminder.loanmanager.util.PersianDateUtil
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun AddReminderScreen(vm: MainViewModel, onDone: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(PersianDateUtil.today().toString()) }
    var time by remember { mutableStateOf("10:00") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { AppTopBar("افزودن یادآور", onDone) }) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(title, { title = it }, label = { Text("عنوان") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(note, { note = it }, label = { Text("توضیحات") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(date, { date = it }, label = { Text("تاریخ (مثال 1405/07/15)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(time, { time = it }, label = { Text("ساعت (مثال 10:30)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                val epoch = PersianDateUtil.parseToEpochDay(date)
                val parts = time.latinDigits().split(":")
                val h = parts.getOrNull(0)?.trim()?.toIntOrNull()
                val m = parts.getOrNull(1)?.trim()?.toIntOrNull()
                val millis = if (epoch != null && h != null && m != null && h in 0..23 && m in 0..59) {
                    LocalDate.ofEpochDay(epoch).atTime(h, m)
                        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                } else null
                error = when {
                    title.isBlank() -> "عنوان را وارد کنید"
                    millis == null -> "تاریخ یا ساعت نامعتبر است"
                    millis <= System.currentTimeMillis() -> "زمان باید در آینده باشد"
                    else -> null
                }
                if (error == null && millis != null) {
                    vm.addCustomReminder(title.trim(), note.trim(), millis)
                    onDone()
                }
            }) { Text("ذخیره") }
        }
    }
}
