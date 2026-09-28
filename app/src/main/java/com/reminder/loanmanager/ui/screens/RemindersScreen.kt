package com.reminder.loanmanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reminder.loanmanager.ui.viewmodel.MainViewModel
import com.reminder.loanmanager.util.PersianDateUtil
import java.time.Instant
import java.time.ZoneId

@Composable
fun RemindersScreen(vm: MainViewModel, onAdd: () -> Unit) {
    val reminders by vm.reminders.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { AppTopBar("یادآورها") },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) { Icon(Icons.Default.Add, contentDescription = "افزودن یادآور") }
        }
    ) { padding ->
        if (reminders.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(padding).padding(24.dp)) { Text("یادآوری ثبت نشده است.") }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(reminders, key = { it.id }) { r ->
                    val dt = Instant.ofEpochMilli(r.triggerAtEpochMillis).atZone(ZoneId.systemDefault())
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(r.title, fontWeight = FontWeight.Bold)
                                Text("${PersianDateUtil.format(dt.toLocalDate().toEpochDay())} - ساعت %02d:%02d".format(dt.hour, dt.minute))
                                if (r.note.isNotBlank()) Text(r.note)
                            }
                            Switch(checked = r.isEnabled, onCheckedChange = { vm.toggleReminder(r) })
                            IconButton(onClick = { vm.deleteReminder(r) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف")
                            }
                        }
                    }
                }
            }
        }
    }
}
