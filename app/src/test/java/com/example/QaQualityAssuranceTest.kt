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
}
