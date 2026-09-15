package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.CategoryItem
import com.example.data.PaymentReminder
import com.example.data.PaymentRepository
import com.example.util.AppCurrency
import com.example.util.DateFormats
import com.example.util.ThousandsSeparator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QaQualityAssuranceTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: PaymentRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = PaymentRepository(db.paymentDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun qaTest_currencyCopFormatting() {
        // 1. Verify COP currency properties
        assertEquals("COP", AppCurrency.COP.code)
        assertEquals("$", AppCurrency.COP.symbol)

        // 2. Format with Punto separator: $ 50.000,00 or $ 50.000
        val formattedPunto = DateFormats.formatCurrency(50000.0, AppCurrency.COP, ThousandsSeparator.PUNTO)
        assertTrue("Formatted COP should contain $ symbol", formattedPunto.contains("$"))
        assertTrue("Formatted COP should contain 50.000", formattedPunto.contains("50.000"))

        // 3. Format with Coma separator: $ 125,000
        val formattedComa = DateFormats.formatCurrency(125000.0, AppCurrency.COP, ThousandsSeparator.COMA)
        assertTrue("Formatted COP should contain $ symbol", formattedComa.contains("$"))
        assertTrue("Formatted COP should contain 125,000", formattedComa.contains("125,000"))

        // 4. Format without decimals: $ 125,000 (no decimal separator)
        val formattedNoDecimals = DateFormats.formatCurrency(125000.0, AppCurrency.COP, ThousandsSeparator.COMA, showDecimals = false)
        assertEquals("$ 125,000", formattedNoDecimals)
    }

    @Test
    fun qaTest_categoryDuplicationPrevention() = runBlocking {
        // 1. Initial defaults
        repository.ensureDefaultCategories()
        val initialCategories = repository.allCategories.first()
        val uniqueInitialCount = initialCategories.map { it.name.lowercase().trim() }.distinct().size
        assertEquals("All default categories should be unique", uniqueInitialCount, initialCategories.size)

        // 2. Calling ensureDefaultCategories again should NOT add duplicates
        repository.ensureDefaultCategories()
        val afterSecondEnsure = repository.allCategories.first()
        assertEquals("Calling ensureDefaultCategories twice should not create duplicates", initialCategories.size, afterSecondEnsure.size)

        // 3. Inserting existing category (case-insensitive) should NOT create duplicate row
        val insertResultId = repository.insertCategory(CategoryItem(name = "servicios", isActive = true))
        assertTrue("Should return a valid category ID", insertResultId > 0)
        val afterManualInsert = repository.allCategories.first()
        val serviciosCount = afterManualInsert.count { it.name.trim().equals("servicios", ignoreCase = true) }
        assertEquals("Should have exactly 1 category with name servicios", 1, serviciosCount)

        // 4. Test cleanDuplicateCategories removes any existing duplicates in raw table
        val dao = db.paymentDao()
        dao.insertCategory(CategoryItem(name = "DuplicadoTest", isActive = true))
        dao.insertCategory(CategoryItem(name = "duplicadotest", isActive = true))
        dao.insertCategory(CategoryItem(name = "DUPLICADOTEST", isActive = true))

        repository.cleanDuplicateCategories()
        val cleanedList = dao.getAllCategoriesList().filter { it.name.trim().equals("duplicadotest", ignoreCase = true) }
        assertEquals("Duplicates must be cleaned up to exactly 1 record", 1, cleanedList.size)
    }

    @Test
    fun qaTest_recurringPaymentDayGeneration() = runBlocking {
        // Test configuring recurrence on the 26th
        val targetDay = 26
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, targetDay)
        }

        val reminder = PaymentReminder(
            title = "Plan de Celular",
            category = "Servicios",
            place = "Casa",
            approxAmount = 65000.0,
            paymentCode = "CEL-7788",
            dueDateMillis = cal.timeInMillis,
            alertTimeMillis = cal.timeInMillis
        )

        val id = repository.insertPayment(reminder)
        assertTrue(id > 0)

        val retrieved = repository.getPaymentById(id)
        assertNotNull(retrieved)
        assertEquals("CEL-7788", retrieved?.paymentCode)
        assertEquals(65000.0, retrieved?.approxAmount ?: 0.0, 0.01)

        val retrievedCal = Calendar.getInstance().apply {
            timeInMillis = retrieved!!.dueDateMillis
        }
        assertEquals(26, retrievedCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun qaTest_paymentReminderLifecycle() = runBlocking {
        val reminder = PaymentReminder(
            title = "Internet Fibra Óptica",
            category = "Servicios",
            place = "Casa",
            approxAmount = 89900.0,
            paymentCode = "FIBRA-443",
            dueDateMillis = System.currentTimeMillis() + 86400000L,
            alertTimeMillis = System.currentTimeMillis() + 3600000L
        )

        val id = repository.insertPayment(reminder)
        val all = repository.allPayments.first()
        val inserted = all.find { it.id == id }
        assertNotNull("Inserted payment should exist in all payments", inserted)
        assertFalse("New payment should be unpaid", inserted!!.isPaid)

        // Register payment as paid
        val paid = inserted.copy(
            isPaid = true,
            paidAmount = 89900.0,
            paidDateMillis = System.currentTimeMillis(),
            receiptPhotoUri = "/path/receipt.jpg"
        )
        repository.updatePayment(paid)

        val allAfter = repository.allPayments.first()
        val updated = allAfter.find { it.id == id }
        assertNotNull(updated)
        assertTrue("Payment should be marked as paid", updated!!.isPaid)
        assertEquals(89900.0, updated.paidAmount ?: 0.0, 0.01)
        assertEquals("/path/receipt.jpg", updated.receiptPhotoUri)
    }

    @Test
    fun qaTest_paymentVisualsAndCustomIconColor() = runBlocking {
        // 1. Insert payment with custom icon and color
        val reminder = PaymentReminder(
            title = "Alquiler Local",
            category = "Alquiler",
            place = "Negocio",
            approxAmount = 1500000.0,
            dueDateMillis = System.currentTimeMillis() + 86400000L,
            alertTimeMillis = System.currentTimeMillis(),
            iconName = "Storefront",
            colorHex = "#2E7D32"
        )
        val id = repository.insertPayment(reminder)
        val retrieved = repository.getPaymentById(id)
        assertNotNull(retrieved)
        assertEquals("Storefront", retrieved?.iconName)
        assertEquals("#2E7D32", retrieved?.colorHex)

        // 2. Test PaymentVisuals resolution
        val parsedColor = com.example.util.PaymentVisuals.getColor("#2E7D32", androidx.compose.ui.graphics.Color.Black)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF2E7D32), parsedColor)

        // Invalid hex falls back safely
        val fallbackColor = com.example.util.PaymentVisuals.getColor("invalid", androidx.compose.ui.graphics.Color.Red)
        assertEquals(androidx.compose.ui.graphics.Color.Red, fallbackColor)

        // Null hex falls back safely
        val nullColor = com.example.util.PaymentVisuals.getColor(null, androidx.compose.ui.graphics.Color.Blue)
        assertEquals(androidx.compose.ui.graphics.Color.Blue, nullColor)
    }

    @Test
    fun qaTest_filterAndTotalCalculationLogic() {
        val nowCal = Calendar.getInstance()
        val currentMonthMillis = nowCal.timeInMillis

        val nextYearCal = Calendar.getInstance().apply { add(Calendar.YEAR, 1) }
        val nextYearMillis = nextYearCal.timeInMillis

        val paymentThisMonthServices = PaymentReminder(
            id = 1,
            title = "Luz",
            category = "Servicios",
            place = "Casa",
            approxAmount = 100.0,
            dueDateMillis = currentMonthMillis,
            alertTimeMillis = currentMonthMillis
        )
        val paymentThisMonthRent = PaymentReminder(
            id = 2,
            title = "Alquiler",
            category = "Alquiler",
            place = "Casa",
            approxAmount = 500.0,
            dueDateMillis = currentMonthMillis,
            alertTimeMillis = currentMonthMillis
        )
        val paymentNextYearServices = PaymentReminder(
            id = 3,
            title = "Agua Anual",
            category = "Servicios",
            place = "Casa",
            approxAmount = 250.0,
            dueDateMillis = nextYearMillis,
            alertTimeMillis = nextYearMillis
        )

        val list = listOf(paymentThisMonthServices, paymentThisMonthRent, paymentNextYearServices)

        // 1. Filter "THIS_MONTH"
        val thisMonthFiltered = list.filter { DateFormats.isCurrentMonth(it.dueDateMillis) }
        assertEquals(2, thisMonthFiltered.size)
        val thisMonthTotal = thisMonthFiltered.sumOf { it.approxAmount }
        assertEquals(600.0, thisMonthTotal, 0.01)

        // 2. Filter "ALL"
        val allTotal = list.sumOf { it.approxAmount }
        assertEquals(850.0, allTotal, 0.01)

        // 3. Filter category "Servicios"
        val servicesFiltered = list.filter { it.category.equals("Servicios", ignoreCase = true) }
        assertEquals(2, servicesFiltered.size)
        val servicesTotal = servicesFiltered.sumOf { it.approxAmount }
        assertEquals(350.0, servicesTotal, 0.01)
    }

    @Test
    fun qaTest_recurringDayClamping() {
        // Test day clamping logic
        val day31 = 31
        val febCal = Calendar.getInstance().apply {
            set(2026, Calendar.FEBRUARY, 1)
        }
        val maxDaysFeb = febCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        assertTrue(maxDaysFeb in 28..29)
        val clampedFebDay = minOf(day31, maxDaysFeb)
        assertEquals(maxDaysFeb, clampedFebDay)

        val aprCal = Calendar.getInstance().apply {
            set(2026, Calendar.APRIL, 1)
        }
        val maxDaysApr = aprCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        assertEquals(30, maxDaysApr)
        val clampedAprDay = minOf(day31, maxDaysApr)
        assertEquals(30, clampedAprDay)
    }

    @Test
    fun qaTest_requestedCountriesAndCurrencies() {
        val currencies = AppCurrency.entries

        // Verify requested countries exist with flags and correct symbols
        val mx = currencies.find { it.countryName.contains("México", ignoreCase = true) }
        assertNotNull(mx)
        assertEquals("MXN", mx?.code)
        assertEquals("🇲🇽", mx?.flagEmoji)
        assertEquals("$", mx?.symbol)

        val gt = currencies.find { it.countryName.contains("Guatemala", ignoreCase = true) }
        assertNotNull(gt)
        assertEquals("GTQ", gt?.code)
        assertEquals("🇬🇹", gt?.flagEmoji)
        assertEquals("Q", gt?.symbol)

        val sv = currencies.find { it.countryName.contains("El Salvador", ignoreCase = true) }
        assertNotNull(sv)
        assertEquals("🇸🇻", sv?.flagEmoji)
        assertEquals("$", sv?.symbol)

        val cr = currencies.find { it.countryName.contains("Costa Rica", ignoreCase = true) }
        assertNotNull(cr)
        assertEquals("CRC", cr?.code)
        assertEquals("🇨🇷", cr?.flagEmoji)
        assertEquals("₡", cr?.symbol)

        val pa = currencies.find { it.countryName.contains("Panamá", ignoreCase = true) }
        assertNotNull(pa)
        assertEquals("🇵🇦", pa?.flagEmoji)

        val hn = currencies.find { it.countryName.contains("Honduras", ignoreCase = true) }
        assertNotNull(hn)
        assertEquals("HNL", hn?.code)
        assertEquals("🇭🇳", hn?.flagEmoji)
        assertEquals("L", hn?.symbol)

        val ec = currencies.find { it.countryName.contains("Ecuador", ignoreCase = true) }
        assertNotNull(ec)
        assertEquals("🇪🇨", ec?.flagEmoji)
        assertEquals("$", ec?.symbol)

        val br = currencies.find { it.countryName.contains("Brasil", ignoreCase = true) }
        assertNotNull(br)
        assertEquals("BRL", br?.code)
        assertEquals("🇧🇷", br?.flagEmoji)
        assertEquals("R$", br?.symbol)

        val cl = currencies.find { it.countryName.contains("Chile", ignoreCase = true) }
        assertNotNull(cl)
        assertEquals("CLP", cl?.code)
        assertEquals("🇨🇱", cl?.flagEmoji)
        assertEquals("$", cl?.symbol)

        val ar = currencies.find { it.countryName.contains("Argentina", ignoreCase = true) }
        assertNotNull(ar)
        assertEquals("ARS", ar?.code)
        assertEquals("🇦🇷", ar?.flagEmoji)
        assertEquals("$", ar?.symbol)

        val py = currencies.find { it.countryName.contains("Paraguay", ignoreCase = true) }
        assertNotNull(py)
        assertEquals("PYG", py?.code)
        assertEquals("🇵🇾", py?.flagEmoji)
        assertEquals("₲", py?.symbol)

        val uy = currencies.find { it.countryName.contains("Uruguay", ignoreCase = true) }
        assertNotNull(uy)
        assertEquals("UYU", uy?.code)
        assertEquals("🇺🇾", uy?.flagEmoji)

        val bo = currencies.find { it.countryName.contains("Bolivia", ignoreCase = true) }
        assertNotNull(bo)
        assertEquals("BOB", bo?.code)
        assertEquals("🇧🇴", bo?.flagEmoji)
        assertEquals("Bs.", bo?.symbol)

        val doRep = currencies.find { it.countryName.contains("Dominicana", ignoreCase = true) }
        assertNotNull(doRep)
        assertEquals("DOP", doRep?.code)
        assertEquals("🇩🇴", doRep?.flagEmoji)
        assertEquals("RD$", doRep?.symbol)

        // Verify formatting with symbol
        val formattedBrazil = DateFormats.formatCurrency(150.0, br!!, ThousandsSeparator.COMA, true)
        assertEquals("R$ 150.00", formattedBrazil)

        val formattedCostaRica = DateFormats.formatCurrency(2500.0, cr!!, ThousandsSeparator.PUNTO, false)
        assertEquals("₡ 2.500", formattedCostaRica)
    }
}
