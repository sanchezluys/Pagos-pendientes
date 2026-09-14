package com.example.ui.dialogs

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.CategoryItem
import com.example.util.AppSettings
import com.example.util.DateFormats
import com.example.util.PaymentVisuals
import java.util.Calendar

enum class PaymentFrequencyType {
    OCCASIONAL,
    RECURRING
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewReminderDialog(
    categories: List<CategoryItem>,
    initialPlace: String,
    appSettings: AppSettings,
    onDismiss: () -> Unit,
    onSaveOccasional: (
        title: String,
        category: String,
        place: String,
        approxAmount: Double,
        paymentCode: String,
        dueDateMillis: Long,
        alertTimeMillis: Long,
        iconName: String?,
        colorHex: String?
    ) -> Unit,
    onSaveRecurring: (
        title: String,
        category: String,
        place: String,
        approxAmount: Double,
        paymentCode: String,
        dayOfMonth: Int,
        startDateMillis: Long,
        endDateMillis: Long,
        alertHour: Int,
        alertMinute: Int,
        iconName: String?,
        colorHex: String?
    ) -> Unit,
    onOpenCategoryManager: () -> Unit
) {
    val context = LocalContext.current

    var frequencyType by remember { mutableStateOf(PaymentFrequencyType.OCCASIONAL) }

    var title by remember { mutableStateOf("") }
    var approxAmountText by remember { mutableStateOf("") }
    var paymentCode by remember { mutableStateOf("") }

    // Selected place respecting active places
    var selectedPlace by remember(initialPlace, appSettings.enableCasa, appSettings.enableNegocio) {
        val initial = if (initialPlace == "Negocio" && appSettings.enableNegocio) {
            "Negocio"
        } else if (appSettings.enableCasa) {
            "Casa"
        } else {
            "Negocio"
        }
        mutableStateOf(initial)
    }

    // Category
    var selectedCategory by remember {
        mutableStateOf(categories.firstOrNull { it.isActive }?.name ?: categories.firstOrNull()?.name ?: "Servicios")
    }

    // Custom Icon and Color
    var selectedIconId by remember { mutableStateOf("ReceiptLong") }
    var selectedColorHex by remember { mutableStateOf("#6750A4") }

    // Reference today
    val todayCal = Calendar.getInstance()

    // 1. OCCASIONAL PAYMENT STATE (Future date required)
    var dueYear by remember { mutableIntStateOf(todayCal.get(Calendar.YEAR)) }
    var dueMonth by remember { mutableIntStateOf(todayCal.get(Calendar.MONTH)) }
    var dueDay by remember { mutableIntStateOf(todayCal.get(Calendar.DAY_OF_MONTH)) }

    // Alert Time (Default: 8:00 AM)
    var alertHour by remember { mutableIntStateOf(8) }
    var alertMinute by remember { mutableIntStateOf(0) }

    val calculatedDueDateMillis = remember(dueYear, dueMonth, dueDay) {
        Calendar.getInstance().apply {
            set(dueYear, dueMonth, dueDay, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    val calculatedAlertTimeMillis = remember(dueYear, dueMonth, dueDay, alertHour, alertMinute) {
        Calendar.getInstance().apply {
            set(dueYear, dueMonth, dueDay, alertHour, alertMinute, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    // 2. RECURRING PAYMENT STATE
    var recurrenceDayOfMonth by remember { mutableIntStateOf(todayCal.get(Calendar.DAY_OF_MONTH).coerceIn(1, 31)) }
    var recurrenceDayText by remember { mutableStateOf(recurrenceDayOfMonth.toString()) }
    var fullYearPreset by remember { mutableStateOf(true) }

    fun commitDay() {
        val num = recurrenceDayText.toIntOrNull()
        val clamped = when {
            num == null || num < 1 -> 1
            num > 31 -> 31
            else -> num
        }
        recurrenceDayOfMonth = clamped
        recurrenceDayText = clamped.toString()
    }

    fun updateRecurrenceDay(day: Int) {
        val clamped = day.coerceIn(1, 31)
        recurrenceDayOfMonth = clamped
        recurrenceDayText = clamped.toString()
    }

    // Start Date (Defaults to today)
    var startYear by remember { mutableIntStateOf(todayCal.get(Calendar.YEAR)) }
    var startMonth by remember { mutableIntStateOf(todayCal.get(Calendar.MONTH)) }
    var startDay by remember { mutableIntStateOf(todayCal.get(Calendar.DAY_OF_MONTH)) }

    // End Date (Defaults to end of year or 1 year ahead)
    val defaultEndCal = Calendar.getInstance().apply {
        set(Calendar.MONTH, Calendar.DECEMBER)
        set(Calendar.DAY_OF_MONTH, 31)
    }
    var endYear by remember { mutableIntStateOf(defaultEndCal.get(Calendar.YEAR)) }
    var endMonth by remember { mutableIntStateOf(defaultEndCal.get(Calendar.MONTH)) }
    var endDay by remember { mutableIntStateOf(defaultEndCal.get(Calendar.DAY_OF_MONTH)) }

    val startDateMillis = remember(startYear, startMonth, startDay) {
        Calendar.getInstance().apply {
            set(startYear, startMonth, startDay, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val endDateMillis = remember(endYear, endMonth, endDay) {
        Calendar.getInstance().apply {
            set(endYear, endMonth, endDay, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    // Calculate how many months / payments will be generated
    val estimatedPaymentsCount by remember(startDateMillis, endDateMillis, recurrenceDayOfMonth) {
        derivedStateOf {
            var count = 0
            val curr = Calendar.getInstance().apply {
                timeInMillis = startDateMillis
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val end = Calendar.getInstance().apply {
                timeInMillis = endDateMillis
                set(Calendar.DAY_OF_MONTH, 1)
            }
            while (!curr.after(end)) {
                val maxDay = curr.getActualMaximum(Calendar.DAY_OF_MONTH)
                val day = minOf(recurrenceDayOfMonth, maxDay)
                val payCal = Calendar.getInstance().apply {
                    set(curr.get(Calendar.YEAR), curr.get(Calendar.MONTH), day, 23, 59, 59)
                }
                if (payCal.timeInMillis >= startDateMillis && payCal.timeInMillis <= endDateMillis) {
                    count++
                }
                curr.add(Calendar.MONTH, 1)
            }
            count
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Nuevo Recordatorio",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Configura pagos ocasionales o recurrentes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_new_reminder_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // FREQUENCY SELECTOR: Ocasional vs Recurrente
                Text(
                    text = "TIPO DE PAGO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Occasional Button
                        val isOccasional = frequencyType == PaymentFrequencyType.OCCASIONAL
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isOccasional) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shadowElevation = if (isOccasional) 2.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { frequencyType = PaymentFrequencyType.OCCASIONAL }
                                .testTag("type_occasional_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (isOccasional) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ocasional",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOccasional) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Recurring Button
                        val isRecurring = frequencyType == PaymentFrequencyType.RECURRING
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isRecurring) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shadowElevation = if (isRecurring) 2.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { frequencyType = PaymentFrequencyType.RECURRING }
                                .testTag("type_recurring_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EventRepeat,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (isRecurring) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Recurrente",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRecurring) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Place Selection (respecting enabled places)
                if (appSettings.enableCasa && appSettings.enableNegocio) {
                    Text(
                        text = "Lugar",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Casa", "Negocio").forEach { place ->
                            val isSelected = selectedPlace.equals(place, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedPlace = place }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (place == "Casa") Icons.Default.Home else Icons.Default.Business,
                                        contentDescription = place,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (place == "Casa" && appSettings.casaDetails.alias.isNotBlank()) appSettings.casaDetails.alias else if (place == "Negocio" && appSettings.negocioDetails.alias.isNotBlank()) appSettings.negocioDetails.alias else place,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nombre del pago (ej. Luz, Préstamo, Alquiler)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_payment_title_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // ICON & COLOR SELECTOR
                val activeColor = PaymentVisuals.getColor(selectedColorHex, MaterialTheme.colorScheme.primary)
                val activeIcon = PaymentVisuals.getIcon(selectedIconId, selectedCategory)

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ÍCONO Y COLOR",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Preview Badge
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = activeColor.copy(alpha = 0.15f),
                                border = BorderStroke(1.5.dp, activeColor),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = activeIcon,
                                        contentDescription = null,
                                        tint = activeColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Color Row
                        Text(
                            text = "Color:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PaymentVisuals.COLORS.forEach { colorOpt ->
                                val isSelected = selectedColorHex.equals(colorOpt.hex, ignoreCase = true)
                                Surface(
                                    shape = CircleShape,
                                    color = colorOpt.color,
                                    border = if (isSelected) BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface) else null,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .clickable { selectedColorHex = colorOpt.hex }
                                ) {
                                    if (isSelected) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = colorOpt.name,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Icon Selector
                        Text(
                            text = "Ícono:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PaymentVisuals.ICONS.forEach { iconOpt ->
                                val isSelected = selectedIconId == iconOpt.id
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) activeColor else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) activeColor else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { selectedIconId = iconOpt.id }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = iconOpt.icon,
                                            contentDescription = iconOpt.name,
                                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Categoría",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "+ Nueva categoría",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onOpenCategoryManager() }
                            .padding(4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.filter { it.isActive }.distinctBy { it.name.trim().lowercase() }.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat.name,
                            onClick = { selectedCategory = cat.name },
                            label = { Text(cat.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amount (separate line)
                OutlinedTextField(
                    value = approxAmountText,
                    onValueChange = { approxAmountText = it },
                    label = { Text("Monto aprox. (${appSettings.currency.symbol})") },
                    placeholder = { Text("0.00") },
                    prefix = {
                        Text(
                            text = "${appSettings.currency.symbol} ",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_payment_amount_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Reference Code (separate line)
                OutlinedTextField(
                    value = paymentCode,
                    onValueChange = { paymentCode = it },
                    label = { Text("Código / Ref. de pago (opcional)") },
                    placeholder = { Text("Ej: 489201") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_payment_code_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // DYNAMIC SECTION: OCASIONAL vs RECURRENTE
                if (frequencyType == PaymentFrequencyType.OCCASIONAL) {
                    // OCASIONAL: Fecha Límite (debe ser hoy o a futuro)
                    Text(
                        text = "FECHA LÍMITE Y ALERTA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Date Picker
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        DatePickerDialog(
                                            context,
                                            { _, y, m, d ->
                                                dueYear = y
                                                dueMonth = m
                                                dueDay = d
                                            },
                                            dueYear,
                                            dueMonth,
                                            dueDay
                                        ).show()
                                    }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Fecha de vencimiento",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = DateFormats.formatDate(calculatedDueDateMillis),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                Text(
                                    text = "Cambiar",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Time Picker
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                alertHour = hour
                                                alertMinute = minute
                                            },
                                            alertHour,
                                            alertMinute,
                                            true
                                        ).show()
                                    }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Alarm,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Hora de la alarma",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = DateFormats.formatTime(calculatedAlertTimeMillis),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                Text(
                                    text = "Cambiar",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                } else {
                    // RECURRENTE: Día fijo del mes + Rango de meses
                    Text(
                        text = "PROGRAMACIÓN DE PAGOS MENSUALES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // 1. Selector del día de pago del mes
                            Column {
                                Text(
                                    text = "Día límite de pago cada mes",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Decrement button [-]
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                val current = recurrenceDayText.toIntOrNull() ?: recurrenceDayOfMonth
                                                updateRecurrenceDay(current - 1)
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "Restar día",
                                                modifier = Modifier.size(20.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Editable Day Text Field (validates on blur / focus loss)
                                    OutlinedTextField(
                                        value = recurrenceDayText,
                                        onValueChange = { input ->
                                            val digits = input.filter { it.isDigit() }.take(2)
                                            recurrenceDayText = digits
                                            val num = digits.toIntOrNull()
                                            if (num != null && num in 1..31) {
                                                recurrenceDayOfMonth = num
                                            }
                                        },
                                        label = { Text("Día") },
                                        placeholder = { Text("1-31") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .width(88.dp)
                                            .onFocusChanged { focusState ->
                                                if (!focusState.isFocused) {
                                                    commitDay()
                                                }
                                            }
                                            .testTag("recurrence_day_input")
                                    )

                                    // Increment button [+]
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                val current = recurrenceDayText.toIntOrNull() ?: recurrenceDayOfMonth
                                                updateRecurrenceDay(current + 1)
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Sumar día",
                                                modifier = Modifier.size(20.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Día $recurrenceDayOfMonth de cada mes",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = if (recurrenceDayOfMonth in 29..31) "(En meses más cortos, vence el último día)" else "(Se repetirá en esa fecha)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Quick Day Chips including 26
                                Text(
                                    text = "Días habituales:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf(1, 5, 10, 15, 20, 25, 26, 28, 30, 31).forEach { dayOption ->
                                        val isSelected = recurrenceDayOfMonth == dayOption
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(
                                                1.dp,
                                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                            ),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { updateRecurrenceDay(dayOption) }
                                        ) {
                                            Text(
                                                text = if (dayOption == 31) "31 (Fin de mes)" else "$dayOption",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // 2. Período: Todo el año vs Rango personalizado
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Programar resto del año",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Hasta diciembre de este año",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = fullYearPreset,
                                    onCheckedChange = { checked ->
                                        fullYearPreset = checked
                                        if (checked) {
                                            endYear = todayCal.get(Calendar.YEAR)
                                            endMonth = Calendar.DECEMBER
                                            endDay = 31
                                        }
                                    }
                                )
                            }

                            // Rango de fechas
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Desde
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            DatePickerDialog(
                                                context,
                                                { _, y, m, d ->
                                                    startYear = y
                                                    startMonth = m
                                                    startDay = d
                                                },
                                                startYear,
                                                startMonth,
                                                startDay
                                            ).show()
                                        }
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "Desde",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = DateFormats.formatDate(startDateMillis),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                // Hasta
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            DatePickerDialog(
                                                context,
                                                { _, y, m, d ->
                                                    endYear = y
                                                    endMonth = m
                                                    endDay = d
                                                    fullYearPreset = false
                                                },
                                                endYear,
                                                endMonth,
                                                endDay
                                            ).show()
                                        }
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "Hasta",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = DateFormats.formatDate(endDateMillis),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            // Hora de alerta
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                alertHour = hour
                                                alertMinute = minute
                                            },
                                            alertHour,
                                            alertMinute,
                                            true
                                        ).show()
                                    }
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Alarm,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Hora de notificación: ${String.format("%02d:%02d", alertHour, alertMinute)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "Cambiar",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Summary of generated payments
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Repeat,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Se generarán $estimatedPaymentsCount pagos automáticos programados (el día $recurrenceDayOfMonth de cada mes).",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                Toast.makeText(context, "Ingresa el nombre del pago", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val parsedAmount = approxAmountText.replace(',', '.').toDoubleOrNull() ?: 0.0

                            if (frequencyType == PaymentFrequencyType.OCCASIONAL) {
                                // Validate future date
                                val midnightToday = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }.timeInMillis

                                if (calculatedDueDateMillis < midnightToday) {
                                    Toast.makeText(context, "La fecha debe ser hoy o una fecha a futuro", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                onSaveOccasional(
                                    title,
                                    selectedCategory,
                                    selectedPlace,
                                    parsedAmount,
                                    paymentCode,
                                    calculatedDueDateMillis,
                                    calculatedAlertTimeMillis,
                                    selectedIconId,
                                    selectedColorHex
                                )
                            } else {
                                // Recurring
                                commitDay()
                                if (endDateMillis < startDateMillis) {
                                    Toast.makeText(context, "La fecha hasta debe ser posterior a la fecha desde", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (estimatedPaymentsCount <= 0) {
                                    Toast.makeText(context, "No hay pagos comprendidos en el período seleccionado", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                onSaveRecurring(
                                    title,
                                    selectedCategory,
                                    selectedPlace,
                                    parsedAmount,
                                    paymentCode,
                                    recurrenceDayOfMonth,
                                    startDateMillis,
                                    endDateMillis,
                                    alertHour,
                                    alertMinute,
                                    selectedIconId,
                                    selectedColorHex
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("save_reminder_button")
                    ) {
                        Text(
                            text = if (frequencyType == PaymentFrequencyType.OCCASIONAL) "Crear Recordatorio" else "Generar Recurrentes",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
