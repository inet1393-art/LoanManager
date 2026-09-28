package com.reminder.loanmanager.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.reminder.loanmanager.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(AlarmScheduler.EXTRA_ID, 0L)
        val title = intent.getStringExtra(AlarmScheduler.EXTRA_TITLE).orEmpty()
        val note = intent.getStringExtra(AlarmScheduler.EXTRA_NOTE).orEmpty()

        NotificationHelper.createChannel(context)
        NotificationHelper.show(context, id.toInt(), title, note.ifBlank { title })

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = AppDatabase.getInstance(context).appSettingsDao().get()
                val overlayOn = settings?.overlayReminderEnabled ?: true
                if (overlayOn && Settings.canDrawOverlays(context)) {
                    val svc = Intent(context, OverlayService::class.java).apply {
                        putExtra(AlarmScheduler.EXTRA_TITLE, title)
                        putExtra(AlarmScheduler.EXTRA_NOTE, note)
                    }
                    context.startForegroundService(svc)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
