package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateFormats {

    private val esLocale = Locale.forLanguageTag("es-ES")
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", esLocale)
    private val timeFormat = SimpleDateFormat("hh:mm a", esLocale)
    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", esLocale)

    fun formatDate(millis: Long): String {
        return dateFormat.format(Date(millis))
    }

    fun formatTime(millis: Long): String {
        return timeFormat.format(Date(millis))
    }

    fun formatDateTime(millis: Long): String {
        return dateTimeFormat.format(Date(millis))
    }

    fun formatNumber(
        amount: Double,
        separator: ThousandsSeparator = ThousandsSeparator.COMA,
        showDecimals: Boolean = true
    ): String {
        return try {
            val symbols = DecimalFormatSymbols(Locale.US)
            val pattern: String
            when (separator) {
                ThousandsSeparator.PUNTO -> {
                    symbols.groupingSeparator = '.'
                    symbols.decimalSeparator = ','
                    pattern = if (showDecimals) "#,##0.00" else "#,##0"
                }
                ThousandsSeparator.COMA -> {
                    symbols.groupingSeparator = ','
                    symbols.decimalSeparator = '.'
                    pattern = if (showDecimals) "#,##0.00" else "#,##0"
                }
                ThousandsSeparator.DESACTIVADO -> {
                    symbols.decimalSeparator = '.'
                    pattern = if (showDecimals) "0.00" else "0"
                }
            }
            val df = DecimalFormat(pattern, symbols).apply {
                if (separator == ThousandsSeparator.DESACTIVADO) {
                    isGroupingUsed = false
                }
            }
            df.format(amount)
        } catch (e: Exception) {
            val fmt = if (showDecimals) "%.2f" else "%.0f"
            String.format(Locale.US, fmt, amount)
        }
    }

    fun formatNumber(amount: Double, settings: AppSettings): String {
        return formatNumber(amount, settings.thousandsSeparator, settings.showDecimals)
    }

    fun formatCurrency(
        amount: Double,
        currency: AppCurrency = AppCurrency.SOL,
        separator: ThousandsSeparator = ThousandsSeparator.COMA,
        showDecimals: Boolean = true
    ): String {
        return "${currency.symbol} ${formatNumber(amount, separator, showDecimals)}"
    }

    fun formatCurrency(amount: Double, settings: AppSettings): String {
        return formatCurrency(amount, settings.currency, settings.thousandsSeparator, settings.showDecimals)
    }

    fun formatCurrency(amount: Double): String {
        return formatCurrency(amount, AppCurrency.SOL, ThousandsSeparator.COMA, true)
    }

    /**
     * Parses a formatted amount string into a Double, respecting the selected thousands separator.
     * Supports various input formats such as:
     * - PUNTO: "1.250,50", "1250,50", "1.250", "1250.50"
     * - COMA: "1,250.50", "1250.50", "1,250", "1250,50"
     * - DESACTIVADO: "1250.50", "1250,50"
     */
    fun parseAmount(input: String, separator: ThousandsSeparator = ThousandsSeparator.COMA): Double? {
        val clean = input.trim()
        if (clean.isBlank()) return null
        return try {
            when (separator) {
                ThousandsSeparator.PUNTO -> {
                    if (clean.contains('.') && clean.contains(',')) {
                        // Standard PUNTO format: "1.250,50"
                        clean.replace(".", "").replace(',', '.').toDoubleOrNull()
                    } else if (clean.contains(',')) {
                        // Decimal with comma: "1250,50"
                        clean.replace(',', '.').toDoubleOrNull()
                    } else if (clean.contains('.')) {
                        val parts = clean.split('.')
                        if (parts.size > 2) {
                            clean.replace(".", "").toDoubleOrNull()
                        } else if (parts.size == 2 && parts[1].length == 3 && parts[0].length in 1..3) {
                            clean.replace(".", "").toDoubleOrNull()
                        } else {
                            clean.toDoubleOrNull() ?: clean.replace(".", "").toDoubleOrNull()
                        }
                    } else {
                        clean.toDoubleOrNull()
                    }
                }
                ThousandsSeparator.COMA -> {
                    if (clean.contains(',') && clean.contains('.')) {
                        // Standard COMA format: "1,250.50"
                        clean.replace(",", "").toDoubleOrNull()
                    } else if (clean.contains('.')) {
                        // Decimal with dot: "1250.50"
                        clean.toDoubleOrNull()
                    } else if (clean.contains(',')) {
                        val parts = clean.split(',')
                        if (parts.size > 2) {
                            clean.replace(",", "").toDoubleOrNull()
                        } else if (parts.size == 2 && parts[1].length == 3 && parts[0].length in 1..3) {
                            clean.replace(",", "").toDoubleOrNull()
                        } else {
                            clean.replace(',', '.').toDoubleOrNull()
                        }
                    } else {
                        clean.toDoubleOrNull()
                    }
                }
                ThousandsSeparator.DESACTIVADO -> {
                    clean.replace(',', '.').toDoubleOrNull()
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Builds default alert time at 8:00 AM on the specified due date
     */
    fun getDefaultAlertTimeForDate(dueDateMillis: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dueDateMillis
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    fun isOverdue(dueDateMillis: Long): Boolean {
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return dueDateMillis < startOfToday
    }

    fun isToday(dueDateMillis: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = dueDateMillis }
        val cal2 = Calendar.getInstance()
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun isCurrentMonth(timeMillis: Long): Boolean {
        val target = Calendar.getInstance().apply { timeInMillis = timeMillis }
        val now = Calendar.getInstance()
        return target.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                target.get(Calendar.MONTH) == now.get(Calendar.MONTH)
    }
}
