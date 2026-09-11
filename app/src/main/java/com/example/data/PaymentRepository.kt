package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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

    val allCategories: Flow<List<CategoryItem>> = paymentDao.getAllCategories().map { list ->
        list.distinctBy { it.name.trim().lowercase() }
    }

    val activeCategories: Flow<List<CategoryItem>> = paymentDao.getActiveCategories().map { list ->
        list.distinctBy { it.name.trim().lowercase() }
    }

    suspend fun insertCategory(category: CategoryItem): Long {
        val trimmed = category.name.trim()
        val existing = paymentDao.getCategoryByName(trimmed)
        return if (existing != null) {
            if (!existing.isActive && category.isActive) {
                paymentDao.updateCategory(existing.copy(isActive = true))
            }
            existing.id
        } else {
            paymentDao.insertCategory(category.copy(name = trimmed))
        }
    }

    suspend fun updateCategory(category: CategoryItem) =
        paymentDao.updateCategory(category)

    suspend fun deleteCategory(category: CategoryItem) =
        paymentDao.deleteCategory(category)

    suspend fun cleanDuplicateCategories() {
        val all = paymentDao.getAllCategoriesList()
        val seenNames = mutableSetOf<String>()
        for (cat in all) {
            val normalized = cat.name.trim().lowercase()
            if (seenNames.contains(normalized)) {
                // Duplicate category row found in DB, delete it
                paymentDao.deleteCategory(cat)
            } else {
                seenNames.add(normalized)
            }
        }
    }

    suspend fun ensureDefaultCategories() {
        // 1. Clean any existing duplicate categories in database
        cleanDuplicateCategories()

        // 2. Insert missing defaults
        val existing = paymentDao.getAllCategoriesList()
        val existingNames = existing.map { it.name.trim().lowercase() }.toSet()

        val defaults = listOf(
            CategoryItem(name = "Servicios", isDefault = true, isActive = true),
            CategoryItem(name = "Deudas", isDefault = true, isActive = true),
            CategoryItem(name = "Créditos", isDefault = true, isActive = true),
            CategoryItem(name = "Impuestos", isDefault = true, isActive = true),
            CategoryItem(name = "Otros", isDefault = true, isActive = true)
        )
        defaults.forEach { defaultCat ->
            if (!existingNames.contains(defaultCat.name.trim().lowercase())) {
                paymentDao.insertCategory(defaultCat)
            }
        }
    }
}
