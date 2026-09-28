package com.reminder.loanmanager.util

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.reminder.loanmanager.data.AppDatabase
import com.reminder.loanmanager.data.entity.Installment
import com.reminder.loanmanager.data.entity.InstallmentStatus
import com.reminder.loanmanager.data.entity.Loan
import com.reminder.loanmanager.data.entity.Payment
import com.reminder.loanmanager.data.entity.Reminder
import com.reminder.loanmanager.data.entity.ReminderType
import org.json.JSONArray
import org.json.JSONObject

/** JSON backup/restore of the whole database through the Storage Access Framework. */
class BackupHelper(private val context: Context, private val db: AppDatabase) {

    suspend fun export(uri: Uri) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("loans", JSONArray().apply {
            db.loanDao().getAllOnce().forEach { l ->
                put(JSONObject().apply {
                    put("id", l.id); put("title", l.title); put("lenderName", l.lenderName)
                    put("loanType", l.loanType); put("totalAmount", l.totalAmount)
                    put("installmentCount", l.installmentCount)
                    put("startDateEpochDay", l.startDateEpochDay); put("note", l.note)
                    put("isSettled", l.isSettled); put("createdAt", l.createdAtEpochMillis)
                })
            }
        })
        root.put("installments", JSONArray().apply {
            db.installmentDao().getAllOnce().forEach { i ->
                put(JSONObject().apply {
                    put("id", i.id); put("loanId", i.loanId); put("index", i.indexInLoan)
                    put("due", i.dueDateEpochDay); put("amount", i.amount)
                    put("status", i.status.name); put("note", i.note)
                })
            }
        })
        root.put("payments", JSONArray().apply {
            db.paymentDao().getAllOnce().forEach { p ->
                put(JSONObject().apply {
                    put("id", p.id); put("installmentId", p.installmentId)
                    put("date", p.paymentDateEpochDay); put("amount", p.amount); put("note", p.note)
                })
            }
        })
        root.put("reminders", JSONArray().apply {
            db.reminderDao().getAllOnce().forEach { r ->
                put(JSONObject().apply {
                    put("id", r.id)
                    put("installmentId", r.installmentId ?: JSONObject.NULL)
                    put("title", r.title); put("note", r.note)
                    put("trigger", r.triggerAtEpochMillis); put("type", r.type.name)
                    put("enabled", r.isEnabled)
                })
            }
        })
        context.contentResolver.openOutputStream(uri, "wt")?.use {
            it.write(root.toString(2).toByteArray(Charsets.UTF_8))
        } ?: error("cannot open output")
    }

    suspend fun restore(uri: Uri) {
        val text = context.contentResolver.openInputStream(uri)?.use {
            it.readBytes().toString(Charsets.UTF_8)
        } ?: error("cannot open input")
        val root = JSONObject(text)

        val loans = root.getJSONArray("loans").objects().map {
            Loan(
                id = it.getLong("id"), title = it.getString("title"),
                lenderName = it.getString("lenderName"), loanType = it.getString("loanType"),
                totalAmount = it.getLong("totalAmount"),
                installmentCount = it.getInt("installmentCount"),
                startDateEpochDay = it.getLong("startDateEpochDay"),
                note = it.optString("note", ""), isSettled = it.getBoolean("isSettled"),
                createdAtEpochMillis = it.getLong("createdAt")
            )
        }
        val installments = root.getJSONArray("installments").objects().map {
            Installment(
                id = it.getLong("id"), loanId = it.getLong("loanId"),
                indexInLoan = it.getInt("index"), dueDateEpochDay = it.getLong("due"),
                amount = it.getLong("amount"),
                status = InstallmentStatus.valueOf(it.getString("status")),
                note = it.optString("note", "")
            )
        }
        val payments = root.getJSONArray("payments").objects().map {
            Payment(
                id = it.getLong("id"), installmentId = it.getLong("installmentId"),
                paymentDateEpochDay = it.getLong("date"), amount = it.getLong("amount"),
                note = it.optString("note", "")
            )
        }
        val reminders = root.getJSONArray("reminders").objects().map {
            Reminder(
                id = it.getLong("id"),
                installmentId = if (it.isNull("installmentId")) null else it.getLong("installmentId"),
                title = it.getString("title"), note = it.optString("note", ""),
                triggerAtEpochMillis = it.getLong("trigger"),
                type = ReminderType.valueOf(it.getString("type")),
                isEnabled = it.getBoolean("enabled")
            )
        }

        db.clearAllTables()
        db.withTransaction {
            loans.forEach { db.loanDao().insert(it) }
            db.installmentDao().insertAll(installments)
            payments.forEach { db.paymentDao().insert(it) }
            reminders.forEach { db.reminderDao().insert(it) }
        }
    }

    private fun JSONArray.objects(): List<JSONObject> =
        (0 until length()).map { getJSONObject(it) }
}
