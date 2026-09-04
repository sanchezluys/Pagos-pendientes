package com.example.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.PagosApplication
import com.example.R
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class PaymentAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule pending alarms after reboot
            rescheduleAllAlarms(context)
            return
        }

        val paymentId = intent.getLongExtra(EXTRA_PAYMENT_ID, -1L)
        if (paymentId == -1L) return

        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Recordatorio de Pago"
        val amount = intent.getDoubleExtra(EXTRA_AMOUNT, 0.0)
        val code = intent.getStringExtra(EXTRA_CODE) ?: ""
        val place = intent.getStringExtra(EXTRA_PLACE) ?: ""
        val category = intent.getStringExtra(EXTRA_CATEGORY) ?: ""

        // Double check payment is still pending in local database
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(context)
            val payment = db.paymentDao().getPaymentById(paymentId)
            if (payment != null && !payment.isPaid) {
                showNotification(context, paymentId, title, amount, code, place, category)
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
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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

        val formattedAmount = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(amount)
        val contentText = buildString {
            append("Monto: $formattedAmount")
            if (code.isNotBlank()) append(" | Ref: $code")
            if (place.isNotBlank()) append(" ($place)")
        }

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(
            context,
            PagosApplication.CHANNEL_ID_PAYMENT_ALERTS
        )
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⏰ Pago pendiente hoy: $title")
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$contentText\nCategoría: $category\n¡No olvides registrar tu comprobante una vez realizado el pago!")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(paymentId.toInt(), notification)
    }

    private fun rescheduleAllAlarms(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(context)
            val payments = db.paymentDao().getAllPayments().firstOrNull() ?: emptyList()
            val now = System.currentTimeMillis()
            payments.filter { !it.isPaid && it.alertTimeMillis > now }.forEach {
                AlarmScheduler.scheduleAlarm(context, it)
            }
        }
    }

    companion object {
        const val EXTRA_PAYMENT_ID = "extra_payment_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_AMOUNT = "extra_amount"
        const val EXTRA_CODE = "extra_code"
        const val EXTRA_PLACE = "extra_place"
        const val EXTRA_CATEGORY = "extra_category"
    }
}
