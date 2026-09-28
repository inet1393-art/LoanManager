package com.reminder.loanmanager

import android.app.Application
import com.reminder.loanmanager.notification.NotificationHelper

class LoanManagerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
    }
}
