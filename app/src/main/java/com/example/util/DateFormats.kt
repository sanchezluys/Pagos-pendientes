package com.example.util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
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
     * - PUNTO: "1.250,50", "1250,50", "1.250", "1250.50", "1.250,"
     * - COMA: "1,250.50", "1250.50", "1,250", "1250,50", "1,250."
     * - DESACTIVADO: "1250.50", "1250,50"
     */
    fun parseAmount(input: String, separator: ThousandsSeparator = ThousandsSeparator.COMA): Double? {
        var clean = input.trim()
        if (clean.isBlank()) return null
        clean = clean.trimEnd('.', ',')
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
     * Formats raw user input live as they type, applying thousands separators and decimal rules
     * according to the user's settings.
     */
    fun formatLiveAmountInput(
        previousFormatted: String,
        newRawInput: String,
        separator: ThousandsSeparator = ThousandsSeparator.COMA,
        showDecimals: Boolean = true
    ): String {
        val trimmed = newRawInput.trim()
        if (trimmed.isEmpty()) return ""

        val groupingChar: Char? = when (separator) {
            ThousandsSeparator.PUNTO -> '.'
            ThousandsSeparator.COMA -> ','
            ThousandsSeparator.DESACTIVADO -> null
        }
        val decimalChar: Char = when (separator) {
            ThousandsSeparator.PUNTO -> ','
            ThousandsSeparator.COMA, ThousandsSeparator.DESACTIVADO -> '.'
        }

        // Did the user delete a grouping separator? (e.g. from "1,250" to "1250")
        var rawText = trimmed
        if (groupingChar != null &&
            previousFormatted.length == rawText.length + 1 &&
            previousFormatted.count { it == groupingChar } > rawText.count { it == groupingChar } &&
            previousFormatted.count { it.isDigit() } == rawText.count { it.isDigit() }
        ) {
            var mismatchIndex = 0
            while (mismatchIndex < rawText.length && mismatchIndex < previousFormatted.length &&
                rawText[mismatchIndex] == previousFormatted[mismatchIndex]
            ) {
                mismatchIndex++
            }
            if (mismatchIndex < previousFormatted.length && previousFormatted[mismatchIndex] == groupingChar) {
                if (mismatchIndex > 0) {
                    val sb = StringBuilder(rawText)
                    sb.deleteCharAt(mismatchIndex - 1)
                    rawText = sb.toString()
                }
            }
        }

        // Check if decimals are enabled and whether a decimal point was entered
        val hasDecimal: Boolean
        val intSubstring: String
        val decSubstring: String

        if (!showDecimals) {
            hasDecimal = false
            intSubstring = rawText
            decSubstring = ""
        } else {
            when (separator) {
                ThousandsSeparator.PUNTO -> {
                    if (rawText.contains(',')) {
                        hasDecimal = true
                        val idx = rawText.indexOf(',')
                        intSubstring = rawText.substring(0, idx)
                        decSubstring = rawText.substring(idx + 1)
                    } else if (rawText.endsWith('.')) {
                        hasDecimal = true
                        intSubstring = rawText.dropLast(1)
                        decSubstring = ""
                    } else if (rawText.contains('.') && !previousFormatted.contains('.')) {
                        hasDecimal = true
                        val idx = rawText.indexOf('.')
                        intSubstring = rawText.substring(0, idx)
                        decSubstring = rawText.substring(idx + 1)
                    } else {
                        hasDecimal = false
                        intSubstring = rawText
                        decSubstring = ""
                    }
                }
                ThousandsSeparator.COMA -> {
                    if (rawText.contains('.')) {
                        hasDecimal = true
                        val idx = rawText.indexOf('.')
                        intSubstring = rawText.substring(0, idx)
                        decSubstring = rawText.substring(idx + 1)
                    } else if (rawText.endsWith(',')) {
                        hasDecimal = true
                        intSubstring = rawText.dropLast(1)
                        decSubstring = ""
                    } else if (rawText.contains(',') && !previousFormatted.contains(',')) {
                        hasDecimal = true
                        val idx = rawText.indexOf(',')
                        intSubstring = rawText.substring(0, idx)
                        decSubstring = rawText.substring(idx + 1)
                    } else {
                        hasDecimal = false
                        intSubstring = rawText
                        decSubstring = ""
                    }
                }
                ThousandsSeparator.DESACTIVADO -> {
                    if (rawText.contains('.')) {
                        hasDecimal = true
                        val idx = rawText.indexOf('.')
                        intSubstring = rawText.substring(0, idx)
                        decSubstring = rawText.substring(idx + 1)
                    } else if (rawText.contains(',')) {
                        hasDecimal = true
                        val idx = rawText.indexOf(',')
                        intSubstring = rawText.substring(0, idx)
                        decSubstring = rawText.substring(idx + 1)
                    } else {
                        hasDecimal = false
                        intSubstring = rawText
                        decSubstring = ""
                    }
                }
            }
        }

        // Clean integer digits
        var intDigits = intSubstring.filter { it.isDigit() }
        if (intDigits.length > 1) {
            intDigits = intDigits.trimStart('0')
            if (intDigits.isEmpty()) intDigits = "0"
        }
        if (intDigits.isEmpty() && hasDecimal) {
            intDigits = "0"
        }
        intDigits = intDigits.take(12)

        if (intDigits.isEmpty() && !hasDecimal) {
            return ""
        }

        // Format integer part with thousands separator
        val formattedInt = if (groupingChar != null && intDigits.isNotEmpty()) {
            val sb = StringBuilder()
            val len = intDigits.length
            for (i in 0 until len) {
                sb.append(intDigits[i])
                val remaining = len - 1 - i
                if (remaining > 0 && remaining % 3 == 0) {
                    sb.append(groupingChar)
                }
            }
            sb.toString()
        } else {
            intDigits
        }

        return if (hasDecimal) {
            val decDigits = decSubstring.filter { it.isDigit() }.take(2)
            "$formattedInt$decimalChar$decDigits"
        } else {
            formattedInt
        }
    }

    /**
     * Formats a TextFieldValue live as user types, preserving cursor position appropriately.
     */
    fun formatLiveAmountTextFieldValue(
        previousValue: TextFieldValue,
        newValue: TextFieldValue,
        separator: ThousandsSeparator = ThousandsSeparator.COMA,
        showDecimals: Boolean = true
    ): TextFieldValue {
        val formattedText = formatLiveAmountInput(
            previousFormatted = previousValue.text,
            newRawInput = newValue.text,
            separator = separator,
            showDecimals = showDecimals
        )

        val oldCursorPos = newValue.selection.end
        val oldText = newValue.text
        val newCursorPos = if (oldCursorPos >= oldText.length) {
            formattedText.length
        } else {
            var count = 0
            for (i in 0 until minOf(oldCursorPos, oldText.length)) {
                val c = oldText[i]
                if (c.isDigit() || c == '.' || c == ',') {
                    count++
                }
            }
            var newPos = 0
            var matched = 0
            while (newPos < formattedText.length && matched < count) {
                val c = formattedText[newPos]
                if (c.isDigit() || c == '.' || c == ',') {
                    matched++
                }
                newPos++
            }
            newPos.coerceIn(0, formattedText.length)
        }

        return TextFieldValue(
            text = formattedText,
            selection = TextRange(newCursorPos)
        )
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
