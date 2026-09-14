package com.example.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class PaymentIconOption(
    val id: String,
    val name: String,
    val icon: ImageVector
)

data class PaymentColorOption(
    val hex: String,
    val name: String,
    val color: Color
)

object PaymentVisuals {

    val ICONS = listOf(
        PaymentIconOption("ReceiptLong", "Factura", Icons.Default.ReceiptLong),
        PaymentIconOption("Bolt", "Luz", Icons.Default.Bolt),
        PaymentIconOption("WaterDrop", "Agua", Icons.Default.WaterDrop),
        PaymentIconOption("LocalFireDepartment", "Gas", Icons.Default.LocalFireDepartment),
        PaymentIconOption("Wifi", "Internet", Icons.Default.Wifi),
        PaymentIconOption("CreditCard", "Tarjeta", Icons.Default.CreditCard),
        PaymentIconOption("Home", "Vivienda", Icons.Default.Home),
        PaymentIconOption("Business", "Negocio", Icons.Default.Business),
        PaymentIconOption("DirectionsCar", "Vehículo", Icons.Default.DirectionsCar),
        PaymentIconOption("ShoppingCart", "Mercado", Icons.Default.ShoppingCart),
        PaymentIconOption("Tv", "Streaming", Icons.Default.Tv),
        PaymentIconOption("Smartphone", "Celular", Icons.Default.Smartphone),
        PaymentIconOption("School", "Educación", Icons.Default.School),
        PaymentIconOption("LocalHospital", "Salud", Icons.Default.LocalHospital),
        PaymentIconOption("FitnessCenter", "Gimnasio", Icons.Default.FitnessCenter),
        PaymentIconOption("Shield", "Seguro", Icons.Default.Shield),
        PaymentIconOption("AccountBalance", "Banco", Icons.Default.AccountBalance),
        PaymentIconOption("Paid", "Efectivo", Icons.Default.Paid)
    )

    val COLORS = listOf(
        PaymentColorOption("#6750A4", "Púrpura", Color(0xFF6750A4)),
        PaymentColorOption("#2563EB", "Azul", Color(0xFF2563EB)),
        PaymentColorOption("#006874", "Teal", Color(0xFF006874)),
        PaymentColorOption("#16A34A", "Verde", Color(0xFF16A34A)),
        PaymentColorOption("#D97706", "Ámbar", Color(0xFFD97706)),
        PaymentColorOption("#EA580C", "Naranja", Color(0xFFEA580C)),
        PaymentColorOption("#DC2626", "Rojo", Color(0xFFDC2626)),
        PaymentColorOption("#DB2777", "Rosa", Color(0xFFDB2777)),
        PaymentColorOption("#0D9488", "Cyan", Color(0xFF0D9488)),
        PaymentColorOption("#475569", "Pizarra", Color(0xFF475569))
    )

    fun getIcon(iconId: String?, category: String): ImageVector {
        if (!iconId.isNullOrBlank()) {
            ICONS.find { it.id.equals(iconId, ignoreCase = true) }?.let { return it.icon }
        }
        val lower = category.lowercase()
        return when {
            lower.contains("luz") || lower.contains("electric") -> Icons.Default.Bolt
            lower.contains("agua") -> Icons.Default.WaterDrop
            lower.contains("gas") -> Icons.Default.LocalFireDepartment
            lower.contains("internet") || lower.contains("wifi") || lower.contains("celular") || lower.contains("tel") -> Icons.Default.Wifi
            lower.contains("credito") || lower.contains("tarjeta") || lower.contains("prestamo") || lower.contains("banco") -> Icons.Default.CreditCard
            lower.contains("alquiler") || lower.contains("renta") || lower.contains("casa") -> Icons.Default.Home
            lower.contains("negocio") -> Icons.Default.Business
            lower.contains("seguro") -> Icons.Default.Shield
            else -> Icons.Default.ReceiptLong
        }
    }

    fun getColor(colorHex: String?, defaultColor: Color): Color {
        if (colorHex.isNullOrBlank()) return defaultColor
        return try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            defaultColor
        }
    }
}
