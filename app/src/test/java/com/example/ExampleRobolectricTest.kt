package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.CategoryItem
import com.example.data.PaymentReminder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Pagos Pendientes", appName)
    }

    @Test
    fun testPaymentReminderInsertAndQuery() = runBlocking {
        val dao = db.paymentDao()

        val payment = PaymentReminder(
            title = "Recibo de Agua",
            category = "Servicios",
            place = "Casa",
            approxAmount = 75.50,
            paymentCode = "AGUA-1234",
            dueDateMillis = System.currentTimeMillis() + 86400000L,
            alertTimeMillis = System.currentTimeMillis() + 3600000L
        )

        val id = dao.insertPayment(payment)
        assertTrue(id > 0)

        val retrieved = dao.getPaymentById(id)
        assertNotNull(retrieved)
        assertEquals("Recibo de Agua", retrieved?.title)
        assertEquals("Casa", retrieved?.place)
        assertEquals(75.50, retrieved?.approxAmount ?: 0.0, 0.01)

        // Test mark as paid
        val paidPayment = retrieved!!.copy(
            isPaid = true,
            paidAmount = 75.50,
            paidDateMillis = System.currentTimeMillis(),
            receiptPhotoUri = "/data/user/0/receipt.jpg"
        )
        dao.updatePayment(paidPayment)

        val updated = dao.getPaymentById(id)
        assertTrue(updated?.isPaid == true)
        assertEquals("/data/user/0/receipt.jpg", updated?.receiptPhotoUri)
    }

    @Test
    fun testCategoryManagement() = runBlocking {
        val dao = db.paymentDao()

        val cat1 = CategoryItem(name = "Servicios", isDefault = true, isActive = true)
        val cat2 = CategoryItem(name = "Inversiones", isDefault = false, isActive = false)

        dao.insertCategory(cat1)
        dao.insertCategory(cat2)

        val allCategories = dao.getAllCategories().first()
        assertEquals(2, allCategories.size)

        val activeCategories = dao.getActiveCategories().first()
        assertEquals(1, activeCategories.size)
        assertEquals("Servicios", activeCategories[0].name)
    }
}

