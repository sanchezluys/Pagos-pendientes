package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_reminders")
data class PaymentReminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String,
    val place: String, // e.g., "Casa", "Negocio"
    val approxAmount: Double,
    val paymentCode: String, // Código / número de pago o referencia
    val dueDateMillis: Long,
    val alertTimeMillis: Long, // Alarm time in millis (default 8:00 AM on due date)
    val isPaid: Boolean = false,
    val paidAmount: Double? = null,
    val paidDateMillis: Long? = null,
    val receiptPhotoUri: String? = null,
    val note: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis()
)
