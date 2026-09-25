package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.PaymentReminder

object AlarmScheduler {

    private const val TAG = "AlarmScheduler"

    fun scheduleAlarm(context: Context, payment: PaymentReminder) {
        if (payment.isPaid) return

        val now = System.currentTimeMillis()
        val triggerTime = payment.alertTimeMillis

        // If the scheduled trigger time is in the past:
        // If payment is due today or already due, schedule for 2 seconds from now
        // so the user gets notified for today's payment rather than missing it completely!
        val effectiveTriggerTime = if (triggerTime <= now) {
            val isDueToday = com.example.util.DateFormats.isToday(payment.dueDateMillis)
            if (isDueToday || payment.dueDateMillis < now) {
                Log.d(TAG, "Trigger time $triggerTime is past for payment ${payment.id}, but payment is due today. Firing notification in 2 seconds.")
                now + 2000L
            } else {
                Log.d(TAG, "Trigger time $triggerTime is in the past, skipping schedule.")
                return
            }
        } else {
            triggerTime
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, PaymentAlarmReceiver::class.java).apply {
            putExtra(PaymentAlarmReceiver.EXTRA_PAYMENT_ID, payment.id)
            putExtra(PaymentAlarmReceiver.EXTRA_TITLE, payment.title)
            putExtra(PaymentAlarmReceiver.EXTRA_AMOUNT, payment.approxAmount)
            putExtra(PaymentAlarmReceiver.EXTRA_CODE, payment.paymentCode)
            putExtra(PaymentAlarmReceiver.EXTRA_PLACE, payment.place)
            putExtra(PaymentAlarmReceiver.EXTRA_CATEGORY, payment.category)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            payment.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        effectiveTriggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        effectiveTriggerTime,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    effectiveTriggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    effectiveTriggerTime,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled alarm for payment ${payment.id} at $effectiveTriggerTime (diff=${effectiveTriggerTime - now}ms)")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to schedule exact alarm: ${e.message}")
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, effectiveTriggerTime, pendingIntent)
            } catch (ex: Exception) {
                Log.e(TAG, "Fallback alarm failed: ${ex.message}")
            }
        }
    }

    fun cancelAlarm(context: Context, paymentId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, PaymentAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            paymentId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled alarm for payment $paymentId")
        }
    }
}
