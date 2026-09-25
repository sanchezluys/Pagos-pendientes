package com.example

import com.example.util.AppCurrency
import com.example.util.AppSettings
import com.example.util.DateFormats
import com.example.util.ThousandsSeparator
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
    fun testIsCurrentMonth() {
        val now = System.currentTimeMillis()
        assertTrue(DateFormats.isCurrentMonth(now))

        val nextYear = Calendar.getInstance().apply {
            add(Calendar.YEAR, 1)
        }
        assertFalse(DateFormats.isCurrentMonth(nextYear.timeInMillis))
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

    @Test
    fun testParseAmount() {
        // PUNTO mode: dot is thousand separator, comma is decimal
        assertEquals(1250.50, DateFormats.parseAmount("1.250,50", ThousandsSeparator.PUNTO)!!, 0.001)
        assertEquals(1250.50, DateFormats.parseAmount("1250,50", ThousandsSeparator.PUNTO)!!, 0.001)
        assertEquals(1250.0, DateFormats.parseAmount("1.250", ThousandsSeparator.PUNTO)!!, 0.001)
        assertEquals(50000.0, DateFormats.parseAmount("50.000", ThousandsSeparator.PUNTO)!!, 0.001)

        // COMA mode: comma is thousand separator, dot is decimal
        assertEquals(1250.50, DateFormats.parseAmount("1,250.50", ThousandsSeparator.COMA)!!, 0.001)
        assertEquals(1250.50, DateFormats.parseAmount("1250.50", ThousandsSeparator.COMA)!!, 0.001)
        assertEquals(1250.0, DateFormats.parseAmount("1,250", ThousandsSeparator.COMA)!!, 0.001)
        assertEquals(50000.0, DateFormats.parseAmount("50,000", ThousandsSeparator.COMA)!!, 0.001)

        // DESACTIVADO mode
        assertEquals(1250.50, DateFormats.parseAmount("1250.50", ThousandsSeparator.DESACTIVADO)!!, 0.001)
        assertEquals(1250.50, DateFormats.parseAmount("1250,50", ThousandsSeparator.DESACTIVADO)!!, 0.001)
    }

    @Test
    fun testFormatNumber() {
        assertEquals("1.250,50", DateFormats.formatNumber(1250.50, ThousandsSeparator.PUNTO, true))
        assertEquals("1.250", DateFormats.formatNumber(1250.50, ThousandsSeparator.PUNTO, false))
        assertEquals("1,250.50", DateFormats.formatNumber(1250.50, ThousandsSeparator.COMA, true))
        assertEquals("1,250", DateFormats.formatNumber(1250.50, ThousandsSeparator.COMA, false))
        assertEquals("1250.50", DateFormats.formatNumber(1250.50, ThousandsSeparator.DESACTIVADO, true))
        assertEquals("1250", DateFormats.formatNumber(1250.50, ThousandsSeparator.DESACTIVADO, false))
    }
}

