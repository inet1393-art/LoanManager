package com.reminder.loanmanager.ui.screens

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reminder.loanmanager.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(vm: MainViewModel, onOpenBackup: () -> Unit) {
    val ctx = LocalContext.current
    val settings by vm.settings.collectAsStateWithLifecycle()

    var overlayGranted by remember { mutableStateOf(Settings.canDrawOverlays(ctx)) }
    var exactGranted by remember { mutableStateOf(canExactAlarm(ctx)) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        overlayGranted = Settings.canDrawOverlays(ctx)
        exactGranted = canExactAlarm(ctx)
    }

    Scaffold(topBar = { AppTopBar("تنظیمات") }) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("حالت تاریک")
                    Switch(settings.darkMode, { vm.updateSettings(settings.copy(darkMode = it)) })
                }
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("نمایش یادآور روی سایر برنامه‌ها")
                        Switch(settings.overlayReminderEnabled, { vm.updateSettings(settings.copy(overlayReminderEnabled = it)) })
                    }
                    if (!overlayGranted) {
                        Text("برای این قابلیت باید دسترسی نمایش روی برنامه‌ها داده شود.", style = MaterialTheme.typography.bodySmall)
                        Button(onClick = {
                            ctx.startActivity(
                                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${ctx.packageName}"))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }) { Text("اعطای دسترسی") }
                    } else {
                        Text("دسترسی داده شده است.", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= 31 && !exactGranted) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("برای دقیق بودن زمان یادآورها، دسترسی «آلارم و یادآور» را فعال کنید.")
                        Button(onClick = {
                            ctx.startActivity(
                                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${ctx.packageName}"))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }) { Text("اعطای دسترسی") }
                    }
                }
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("یادآور پیش‌فرض اقساط (برای وام‌های جدید)", fontWeight = FontWeight.Bold)
                    Stepper("چند روز قبل از سررسید", settings.defaultReminderDaysBefore, 0, 30) {
                        vm.updateSettings(settings.copy(defaultReminderDaysBefore = it))
                    }
                    Stepper("ساعت یادآوری", settings.defaultReminderHour, 0, 23) {
                        vm.updateSettings(settings.copy(defaultReminderHour = it))
                    }
                }
            }

            OutlinedButton(onClick = onOpenBackup, modifier = Modifier.fillMaxWidth()) {
                Text("پشتیبان‌گیری و بازیابی")
            }
        }
    }
}

@Composable
private fun Stepper(label: String, value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { if (value > min) onChange(value - 1) }) { Text("-") }
            Text("$value")
            OutlinedButton(onClick = { if (value < max) onChange(value + 1) }) { Text("+") }
        }
    }
}

private fun canExactAlarm(ctx: Context): Boolean {
    if (Build.VERSION.SDK_INT < 31) return true
    return (ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()
}
