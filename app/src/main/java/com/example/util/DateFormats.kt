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

    fun formatCurrency(
        amount: Double,
        currency: AppCurrency = AppCurrency.SOL,
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
            "${currency.symbol} ${df.format(amount)}"
        } catch (e: Exception) {
            val fmt = if (showDecimals) "%.2f" else "%.0f"
            "${currency.symbol} ${String.format(Locale.US, fmt, amount)}"
        }
    }

    fun formatCurrency(amount: Double, settings: AppSettings): String {
        return formatCurrency(amount, settings.currency, settings.thousandsSeparator, settings.showDecimals)
    }

    fun formatCurrency(amount: Double): String {
        return formatCurrency(amount, AppCurrency.SOL, ThousandsSeparator.COMA, true)
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
}
