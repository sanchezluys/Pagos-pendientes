package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {

    @Query("SELECT * FROM payment_reminders ORDER BY dueDateMillis ASC")
    fun getAllPayments(): Flow<List<PaymentReminder>>

    @Query("SELECT * FROM payment_reminders WHERE place = :place ORDER BY dueDateMillis ASC")
    fun getPaymentsByPlace(place: String): Flow<List<PaymentReminder>>

    @Query("SELECT * FROM payment_reminders WHERE id = :id LIMIT 1")
    suspend fun getPaymentById(id: Long): PaymentReminder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentReminder): Long

    @Update
    suspend fun updatePayment(payment: PaymentReminder)

    @Delete
    suspend fun deletePayment(payment: PaymentReminder)

    @Query("SELECT * FROM categories ORDER BY id ASC")
    suspend fun getAllCategoriesList(): List<CategoryItem>

    @Query("SELECT * FROM categories WHERE LOWER(TRIM(name)) = LOWER(TRIM(:name)) LIMIT 1")
    suspend fun getCategoryByName(name: String): CategoryItem?

    @Query("SELECT * FROM categories ORDER BY id ASC")
    fun getAllCategories(): Flow<List<CategoryItem>>

    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY id ASC")
    fun getActiveCategories(): Flow<List<CategoryItem>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: CategoryItem): Long

    @Update
    suspend fun updateCategory(category: CategoryItem)

    @Delete
    suspend fun deleteCategory(category: CategoryItem)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int
}
