package com.example.data

import kotlinx.coroutines.flow.Flow

class PaymentRepository(private val paymentDao: PaymentDao) {

    val allPayments: Flow<List<PaymentReminder>> = paymentDao.getAllPayments()

    fun getPaymentsByPlace(place: String): Flow<List<PaymentReminder>> =
        paymentDao.getPaymentsByPlace(place)

    suspend fun getPaymentById(id: Long): PaymentReminder? =
        paymentDao.getPaymentById(id)

    suspend fun insertPayment(payment: PaymentReminder): Long =
        paymentDao.insertPayment(payment)

    suspend fun updatePayment(payment: PaymentReminder) =
        paymentDao.updatePayment(payment)

    suspend fun deletePayment(payment: PaymentReminder) =
        paymentDao.deletePayment(payment)

    val allCategories: Flow<List<CategoryItem>> = paymentDao.getAllCategories()
    val activeCategories: Flow<List<CategoryItem>> = paymentDao.getActiveCategories()

    suspend fun insertCategory(category: CategoryItem): Long =
        paymentDao.insertCategory(category)

    suspend fun updateCategory(category: CategoryItem) =
        paymentDao.updateCategory(category)

    suspend fun deleteCategory(category: CategoryItem) =
        paymentDao.deleteCategory(category)

    suspend fun ensureDefaultCategories() {
        if (paymentDao.getCategoryCount() == 0) {
            val defaults = listOf(
                CategoryItem(name = "Servicios", isDefault = true, isActive = true),
                CategoryItem(name = "Deudas", isDefault = true, isActive = true),
                CategoryItem(name = "Créditos", isDefault = true, isActive = true),
                CategoryItem(name = "Impuestos", isDefault = true, isActive = true),
                CategoryItem(name = "Otros", isDefault = true, isActive = true)
            )
            defaults.forEach { paymentDao.insertCategory(it) }
        }
    }
}
