package com.example

import com.example.util.AppCurrency
import com.example.util.AppSettings
import com.example.util.DateFormats
import com.example.util.ThousandsSeparator
import com.example.util.VoiceSpeechParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {

    @Test
    fun testCurrencyFormatting() {
        val amount = 1250.50

        // Sol with Coma: S/ 1,250.50
        val solFormatted = DateFormats.formatCurrency(amount, AppCurrency.SOL, ThousandsSeparator.COMA)
        assertTrue(solFormatted.contains("S/"))
        assertTrue(solFormatted.contains("1,250.50") || solFormatted.contains("1.250,50"))

        // Dólar with Punto: $ 1.250,50
        val usdFormatted = DateFormats.formatCurrency(amount, AppCurrency.USD, ThousandsSeparator.PUNTO)
        assertTrue(usdFormatted.contains("$"))

        // COP with Desactivado: $ 1250.50
        val copFormatted = DateFormats.formatCurrency(amount, AppCurrency.COP, ThousandsSeparator.DESACTIVADO)
        assertTrue(copFormatted.contains("$"))
        assertTrue(copFormatted.contains("1250.50"))
    }

    @Test
    fun testIsTodayAndIsOverdue() {
        val now = System.currentTimeMillis()
        assertTrue(DateFormats.isToday(now))
        assertFalse(DateFormats.isOverdue(now))

        // Past date (10 days ago) should be overdue and not today
        val pastCalendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -10)
        }
        assertTrue(DateFormats.isOverdue(pastCalendar.timeInMillis))
        assertFalse(DateFormats.isToday(pastCalendar.timeInMillis))

        // Future date (10 days ahead) should not be overdue
        val futureCalendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 10)
        }
        assertFalse(DateFormats.isOverdue(futureCalendar.timeInMillis))
    }

    @Test
    fun testVoiceSpeechParserBasic() {
        val input = "Recordar pagar recibo de luz 150 soles para casa referencia REC-987"
        val parsed = VoiceSpeechParser.parse(input)

        assertEquals("Casa", parsed.suggestedPlace)
        assertEquals("Servicios", parsed.suggestedCategory)
        assertEquals(150.0, parsed.suggestedAmount ?: 0.0, 0.01)
        assertEquals("REC-987", parsed.suggestedCode)
        assertNotNull(parsed.suggestedTitle)
    }

    @Test
    fun testVoiceSpeechParserBusinessTax() {
        val input = "Pago de impuesto sunat 480 para negocio"
        val parsed = VoiceSpeechParser.parse(input)

        assertEquals("Negocio", parsed.suggestedPlace)
        assertEquals("Impuestos", parsed.suggestedCategory)
        assertEquals(480.0, parsed.suggestedAmount ?: 0.0, 0.01)
    }

    @Test
    fun testAppSettingsDefaults() {
        val settings = AppSettings()
        assertEquals(AppCurrency.SOL, settings.currency)
        assertEquals(ThousandsSeparator.COMA, settings.thousandsSeparator)
        assertFalse(settings.isDarkTheme)
        assertTrue(settings.showDecimals)
        assertTrue(settings.enableCasa)
        assertTrue(settings.enableNegocio)
        assertEquals("Casa", settings.casaDetails.alias)
        assertEquals("Negocio", settings.negocioDetails.alias)
    }

    @Test
    fun testCurrencyFormattingWithoutDecimals() {
        val amount = 1250.75

        // Con decimales
        val withDecimals = DateFormats.formatCurrency(amount, AppCurrency.SOL, ThousandsSeparator.COMA, showDecimals = true)
        assertTrue(withDecimals.contains(".75") || withDecimals.contains(",75"))

        // Sin decimales
        val withoutDecimals = DateFormats.formatCurrency(amount, AppCurrency.SOL, ThousandsSeparator.COMA, showDecimals = false)
        assertFalse(withoutDecimals.contains(".75"))
        assertFalse(withoutDecimals.contains(",75"))
        assertTrue(withoutDecimals.contains("1,251") || withoutDecimals.contains("1.251"))
    }
}

