package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.MainActivity
import com.example.data.PaymentReminder

object AlarmScheduler {

    private const val TAG = "AlarmScheduler"

    fun scheduleAlarm(context: Context, payment: PaymentReminder) {
        if (payment.isPaid) return

        val now = System.currentTimeMillis()
        val triggerTime = payment.alertTimeMillis

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, PaymentAlarmReceiver::class.java).apply {
            action = PaymentAlarmReceiver.ACTION_PAYMENT_ALARM
            putExtra(PaymentAlarmReceiver.EXTRA_PAYMENT_ID, payment.id)
            putExtra(PaymentAlarmReceiver.EXTRA_TITLE, payment.title)
            putExtra(PaymentAlarmReceiver.EXTRA_AMOUNT, payment.approxAmount)
            putExtra(PaymentAlarmReceiver.EXTRA_CODE, payment.paymentCode)
            putExtra(PaymentAlarmReceiver.EXTRA_PLACE, payment.place)
            putExtra(PaymentAlarmReceiver.EXTRA_CATEGORY, payment.category)
        }

        // If the scheduled trigger time is in the past:
        // If payment is due today, fire immediately so user gets notified for today's payment rather than missing it!
        if (triggerTime <= now) {
            val isDueToday = com.example.util.DateFormats.isToday(payment.dueDateMillis)
            if (isDueToday) {
                Log.d(TAG, "Trigger time $triggerTime is past for payment ${payment.id}, but payment is due today. Firing notification immediately via broadcast.")
                context.sendBroadcast(intent)
                return
            } else {
                Log.d(TAG, "Trigger time $triggerTime is in the past and payment is not due today, skipping schedule.")
                return
            }
        }

        val effectiveTriggerTime = triggerTime

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            payment.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_HIGHLIGHT_PAYMENT_ID", payment.id)
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            payment.id.toInt(),
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // First choice: setAlarmClock provides 100% exact delivery and explicitly wakes device from Doze mode
            val alarmClockInfo = AlarmManager.AlarmClockInfo(effectiveTriggerTime, showPendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            Log.d(TAG, "Scheduled alarm via setAlarmClock for payment ${payment.id} at $effectiveTriggerTime (in ${(effectiveTriggerTime - now) / 1000}s)")
        } catch (e: Exception) {
            Log.w(TAG, "setAlarmClock failed (${e.message}), trying exact/while-idle fallback")
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
                Log.d(TAG, "Scheduled fallback alarm for payment ${payment.id} at $effectiveTriggerTime")
            } catch (ex: Exception) {
                Log.e(TAG, "All exact alarm scheduling failed for payment ${payment.id}: ${ex.message}")
                try {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, effectiveTriggerTime, pendingIntent)
                } catch (ignored: Exception) {
                }
            }
        }
    }

    fun cancelAlarm(context: Context, paymentId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, PaymentAlarmReceiver::class.java).apply {
            action = PaymentAlarmReceiver.ACTION_PAYMENT_ALARM
        }
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
