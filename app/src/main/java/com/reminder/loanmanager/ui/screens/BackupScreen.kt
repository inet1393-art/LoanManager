package com.reminder.loanmanager.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reminder.loanmanager.ui.viewmodel.MainViewModel
import com.reminder.loanmanager.util.PersianDateUtil

@Composable
fun BackupScreen(vm: MainViewModel, onBack: () -> Unit) {
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> if (uri != null) vm.exportBackup(uri) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) vm.importBackup(uri) }

    Scaffold(topBar = { AppTopBar("پشتیبان‌گیری و بازیابی", onBack) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("تمام وام‌ها، اقساط، پرداخت‌ها و یادآورها در یک فایل JSON ذخیره می‌شوند.")
            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                exportLauncher.launch("loan-backup-${PersianDateUtil.today().toString().replace('/', '-')}.json")
            }) { Text("خروجی گرفتن از داده‌ها") }
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = {
                importLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream"))
            }) { Text("بازیابی از فایل پشتیبان") }
            Text("توجه: بازیابی، اطلاعات فعلی برنامه را جایگزین می‌کند.")
        }
    }
}
