package com.example.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.PagosApplication
import com.example.R
import com.example.data.AppDatabase
import com.example.data.SettingsRepository
import com.example.util.DateFormats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class PaymentAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "PaymentAlarmReceiver received broadcast with action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    rescheduleAllAlarms(context)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in rescheduleAllAlarms", e)
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }

        if (action == ACTION_TEST_NOTIFICATION) {
            showNotification(
                context = context,
                paymentId = 99999L,
                title = "Alerta de Prueba",
                amount = 125000.0,
                code = "TEST-001",
                place = "Casa",
                category = "Servicios"
            )
            return
        }

        val paymentId = intent.getLongExtra(EXTRA_PAYMENT_ID, -1L)
        if (paymentId == -1L) return

        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Recordatorio de Pago"
        val amount = intent.getDoubleExtra(EXTRA_AMOUNT, 0.0)
        val code = intent.getStringExtra(EXTRA_CODE) ?: ""
        val place = intent.getStringExtra(EXTRA_PLACE) ?: ""
        val category = intent.getStringExtra(EXTRA_CATEGORY) ?: ""

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val payment = db.paymentDao().getPaymentById(paymentId)
                if (payment == null || !payment.isPaid) {
                    showNotification(context, paymentId, title, amount, code, place, category)
                } else {
                    Log.d(TAG, "Payment $paymentId is already paid, skipping notification.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing alarm in background", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(
        context: Context,
        paymentId: Long,
        title: String,
        amount: Double,
        code: String,
        place: String,
        category: String
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_HIGHLIGHT_PAYMENT_ID", paymentId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            paymentId.toInt(),
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val settings = SettingsRepository(context).getSettings()
        val formattedAmount = DateFormats.formatCurrency(amount, settings)
        val contentText = buildString {
            append("Monto: $formattedAmount")
            if (code.isNotBlank()) append(" | Ref: $code")
            if (place.isNotBlank()) append(" ($place)")
        }

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val notification = NotificationCompat.Builder(
            context,
            PagosApplication.CHANNEL_ID_PAYMENT_ALERTS
        )
            .setSmallIcon(R.drawable.ic_notification_bell)
            .setContentTitle("⏰ Pago pendiente hoy: $title")
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$contentText\nCategoría: $category\n¡No olvides registrar tu comprobante una vez realizado el pago!")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(paymentId.toInt(), notification)
        Log.d(TAG, "Notification delivered for payment $paymentId ($title)")
    }

    private fun rescheduleAllAlarms(context: Context) {
        val db = AppDatabase.getInstance(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val payments = db.paymentDao().getAllPayments().firstOrNull() ?: emptyList()
                val now = System.currentTimeMillis()
                payments.filter { !it.isPaid && it.alertTimeMillis > now }.forEach {
                    AlarmScheduler.scheduleAlarm(context, it)
                }
                Log.d(TAG, "Rescheduled ${payments.size} payments after reboot.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed rescheduling alarms", e)
            }
        }
    }

    companion object {
        private const val TAG = "PaymentAlarmReceiver"
        const val EXTRA_PAYMENT_ID = "extra_payment_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_AMOUNT = "extra_amount"
        const val EXTRA_CODE = "extra_code"
        const val EXTRA_PLACE = "extra_place"
        const val EXTRA_CATEGORY = "extra_category"
        const val ACTION_TEST_NOTIFICATION = "com.example.ACTION_TEST_NOTIFICATION"

        fun sendTestNotification(context: Context) {
            val intent = Intent(context, PaymentAlarmReceiver::class.java).apply {
                action = ACTION_TEST_NOTIFICATION
            }
            context.sendBroadcast(intent)
        }
    }
}
