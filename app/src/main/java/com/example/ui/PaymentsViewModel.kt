package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.alarm.AlarmScheduler
import com.example.data.CategoryItem
import com.example.data.PaymentReminder
import com.example.data.PaymentRepository
import com.example.data.SettingsRepository
import com.example.util.AppCurrency
import com.example.util.AppSettings
import com.example.util.PlaceDetails
import com.example.util.ThousandsSeparator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class StatusTab {
    PENDING,
    PAID
}

data class PlaceStats(
    val pendingCount: Int = 0,
    val pendingTotalAmount: Double = 0.0,
    val paidCount: Int = 0,
    val paidTotalAmount: Double = 0.0
)

class PaymentsViewModel(
    private val repository: PaymentRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val selectedPlace = MutableStateFlow("Todos") // "Todos", "Casa", "Negocio"
    val selectedTab = MutableStateFlow(StatusTab.PENDING)
    val selectedCategory = MutableStateFlow<String?>(null)
    val searchQuery = MutableStateFlow("")

    val appSettings: StateFlow<AppSettings> = settingsRepository.settings

    val allCategories: StateFlow<List<CategoryItem>> = repository.allCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeCategories: StateFlow<List<CategoryItem>> = repository.activeCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // For backwards compatibility where `categories` was used
    val categories: StateFlow<List<CategoryItem>> = allCategories

    val allPayments: StateFlow<List<PaymentReminder>> = repository.allPayments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered list based on place, tab, category, search
    val filteredPayments: StateFlow<List<PaymentReminder>> = combine(
        allPayments,
        selectedPlace,
        selectedTab,
        selectedCategory,
        searchQuery
    ) { payments, place, tab, cat, query ->
        payments.filter { payment ->
            val matchesPlace = when (place) {
                "Todos" -> true
                else -> payment.place.equals(place, ignoreCase = true)
            }
            val matchesTab = when (tab) {
                StatusTab.PENDING -> !payment.isPaid
                StatusTab.PAID -> payment.isPaid
            }
            val matchesCategory = cat == null || payment.category == cat
            val matchesQuery = query.isBlank() ||
                payment.title.contains(query, ignoreCase = true) ||
                payment.paymentCode.contains(query, ignoreCase = true) ||
                payment.category.contains(query, ignoreCase = true) ||
                (payment.note?.contains(query, ignoreCase = true) == true)

            matchesPlace && matchesTab && matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Summary stats for Casa, Negocio, and Todos
    val currentPlaceStats: StateFlow<PlaceStats> = combine(
        allPayments,
        selectedPlace
    ) { payments, place ->
        val relevant = payments.filter {
            if (place == "Todos") true else it.place.equals(place, ignoreCase = true)
        }
        val pending = relevant.filter { !it.isPaid }
        val paid = relevant.filter { it.isPaid }
        PlaceStats(
            pendingCount = pending.size,
            pendingTotalAmount = pending.sumOf { it.approxAmount },
            paidCount = paid.size,
            paidTotalAmount = paid.sumOf { it.paidAmount ?: it.approxAmount }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlaceStats()
    )

    init {
        viewModelScope.launch {
            repository.ensureDefaultCategories()
        }
    }

    fun setPlace(place: String) {
        selectedPlace.value = place
    }

    fun setTab(tab: StatusTab) {
        selectedTab.value = tab
    }

    fun setCategory(category: String?) {
        selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun addPayment(
        title: String,
        category: String,
        place: String,
        approxAmount: Double,
        paymentCode: String,
        dueDateMillis: Long,
        alertTimeMillis: Long,
        context: Context
    ) {
        viewModelScope.launch {
            val payment = PaymentReminder(
                title = title.trim(),
                category = category.trim(),
                place = place.trim(),
                approxAmount = approxAmount,
                paymentCode = paymentCode.trim(),
                dueDateMillis = dueDateMillis,
                alertTimeMillis = alertTimeMillis,
                isPaid = false
            )
            val newId = repository.insertPayment(payment)
            val scheduledPayment = payment.copy(id = newId)
            AlarmScheduler.scheduleAlarm(context, scheduledPayment)
        }
    }

    fun markAsPaid(
        payment: PaymentReminder,
        paidAmount: Double,
        paidDateMillis: Long,
        receiptPhotoUri: String?,
        note: String?,
        context: Context
    ) {
        viewModelScope.launch {
            val updated = payment.copy(
                isPaid = true,
                paidAmount = paidAmount,
                paidDateMillis = paidDateMillis,
                receiptPhotoUri = receiptPhotoUri,
                note = note?.trim()
            )
            repository.updatePayment(updated)
            AlarmScheduler.cancelAlarm(context, payment.id)
        }
    }

    fun markAsPending(payment: PaymentReminder, context: Context) {
        viewModelScope.launch {
            val updated = payment.copy(
                isPaid = false,
                paidAmount = null,
                paidDateMillis = null
            )
            repository.updatePayment(updated)
            if (updated.alertTimeMillis > System.currentTimeMillis()) {
                AlarmScheduler.scheduleAlarm(context, updated)
            }
        }
    }

    fun deletePayment(payment: PaymentReminder, context: Context) {
        viewModelScope.launch {
            repository.deletePayment(payment)
            AlarmScheduler.cancelAlarm(context, payment.id)
        }
    }

    fun updateCurrency(currency: AppCurrency) {
        settingsRepository.updateCurrency(currency)
    }

    fun updateThousandsSeparator(separator: ThousandsSeparator) {
        settingsRepository.updateThousandsSeparator(separator)
    }

    fun updateCasaDetails(details: PlaceDetails) {
        settingsRepository.updateCasaDetails(details)
    }

    fun updateNegocioDetails(details: PlaceDetails) {
        settingsRepository.updateNegocioDetails(details)
    }

    fun toggleCategoryActive(category: CategoryItem, active: Boolean) {
        viewModelScope.launch {
            repository.updateCategory(category.copy(isActive = active))
        }
    }

    fun addCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotBlank()) {
            viewModelScope.launch {
                repository.insertCategory(CategoryItem(name = trimmed, isActive = true))
            }
        }
    }

    fun deleteCategory(category: CategoryItem) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun toggleDarkTheme() {
        val current = appSettings.value.isDarkTheme
        settingsRepository.updateDarkTheme(!current)
    }

    fun updateDarkTheme(isDark: Boolean) {
        settingsRepository.updateDarkTheme(isDark)
    }
}

class PaymentsViewModelFactory(
    private val repository: PaymentRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PaymentsViewModel(repository, settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
