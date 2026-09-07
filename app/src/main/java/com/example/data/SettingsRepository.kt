package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.util.AppCurrency
import com.example.util.AppSettings
import com.example.util.PlaceDetails
import com.example.util.ThousandsSeparator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        val currencyCode = prefs.getString(KEY_CURRENCY, AppCurrency.SOL.code) ?: AppCurrency.SOL.code
        val separatorCode = prefs.getString(KEY_SEPARATOR, ThousandsSeparator.COMA.code) ?: ThousandsSeparator.COMA.code

        val currency = AppCurrency.values().find { it.code.equals(currencyCode, ignoreCase = true) } ?: AppCurrency.SOL
        val separator = ThousandsSeparator.values().find { it.code.equals(separatorCode, ignoreCase = true) } ?: ThousandsSeparator.COMA

        val casaDetails = PlaceDetails(
            alias = prefs.getString(KEY_CASA_ALIAS, "Casa") ?: "Casa",
            address = prefs.getString(KEY_CASA_ADDRESS, "") ?: "",
            additionalData = prefs.getString(KEY_CASA_DATA, "") ?: "",
            notes = prefs.getString(KEY_CASA_NOTES, "") ?: ""
        )

        val negocioDetails = PlaceDetails(
            alias = prefs.getString(KEY_NEGOCIO_ALIAS, "Negocio") ?: "Negocio",
            address = prefs.getString(KEY_NEGOCIO_ADDRESS, "") ?: "",
            additionalData = prefs.getString(KEY_NEGOCIO_DATA, "") ?: "",
            notes = prefs.getString(KEY_NEGOCIO_NOTES, "") ?: ""
        )

        val isDarkTheme = prefs.getBoolean(KEY_DARK_THEME, false)
        val showDecimals = prefs.getBoolean(KEY_SHOW_DECIMALS, true)
        val enableCasa = prefs.getBoolean(KEY_ENABLE_CASA, true)
        val enableNegocio = prefs.getBoolean(KEY_ENABLE_NEGOCIO, true)

        return AppSettings(
            currency = currency,
            thousandsSeparator = separator,
            isDarkTheme = isDarkTheme,
            showDecimals = showDecimals,
            enableCasa = enableCasa,
            enableNegocio = enableNegocio,
            casaDetails = casaDetails,
            negocioDetails = negocioDetails
        )
    }

    fun updateDarkTheme(isDark: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_THEME, isDark).apply()
        _settings.value = _settings.value.copy(isDarkTheme = isDark)
    }

    fun updateShowDecimals(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_DECIMALS, enabled).apply()
        _settings.value = _settings.value.copy(showDecimals = enabled)
    }

    fun updateEnableCasa(enabled: Boolean) {
        // Prevent disabling both
        if (!enabled && !_settings.value.enableNegocio) return
        prefs.edit().putBoolean(KEY_ENABLE_CASA, enabled).apply()
        _settings.value = _settings.value.copy(enableCasa = enabled)
    }

    fun updateEnableNegocio(enabled: Boolean) {
        // Prevent disabling both
        if (!enabled && !_settings.value.enableCasa) return
        prefs.edit().putBoolean(KEY_ENABLE_NEGOCIO, enabled).apply()
        _settings.value = _settings.value.copy(enableNegocio = enabled)
    }

    fun updateCurrency(currency: AppCurrency) {
        prefs.edit().putString(KEY_CURRENCY, currency.code).apply()
        _settings.value = _settings.value.copy(currency = currency)
    }

    fun updateThousandsSeparator(separator: ThousandsSeparator) {
        prefs.edit().putString(KEY_SEPARATOR, separator.code).apply()
        _settings.value = _settings.value.copy(thousandsSeparator = separator)
    }

    fun updateCasaDetails(details: PlaceDetails) {
        prefs.edit()
            .putString(KEY_CASA_ALIAS, details.alias)
            .putString(KEY_CASA_ADDRESS, details.address)
            .putString(KEY_CASA_DATA, details.additionalData)
            .putString(KEY_CASA_NOTES, details.notes)
            .apply()
        _settings.value = _settings.value.copy(casaDetails = details)
    }

    fun updateNegocioDetails(details: PlaceDetails) {
        prefs.edit()
            .putString(KEY_NEGOCIO_ALIAS, details.alias)
            .putString(KEY_NEGOCIO_ADDRESS, details.address)
            .putString(KEY_NEGOCIO_DATA, details.additionalData)
            .putString(KEY_NEGOCIO_NOTES, details.notes)
            .apply()
        _settings.value = _settings.value.copy(negocioDetails = details)
    }

    companion object {
        private const val KEY_CURRENCY = "pref_currency"
        private const val KEY_SEPARATOR = "pref_thousands_separator"
        private const val KEY_DARK_THEME = "pref_dark_theme"
        private const val KEY_SHOW_DECIMALS = "pref_show_decimals"
        private const val KEY_ENABLE_CASA = "pref_enable_casa"
        private const val KEY_ENABLE_NEGOCIO = "pref_enable_negocio"

        private const val KEY_CASA_ALIAS = "pref_casa_alias"
        private const val KEY_CASA_ADDRESS = "pref_casa_address"
        private const val KEY_CASA_DATA = "pref_casa_data"
        private const val KEY_CASA_NOTES = "pref_casa_notes"

        private const val KEY_NEGOCIO_ALIAS = "pref_negocio_alias"
        private const val KEY_NEGOCIO_ADDRESS = "pref_negocio_address"
        private const val KEY_NEGOCIO_DATA = "pref_negocio_data"
        private const val KEY_NEGOCIO_NOTES = "pref_negocio_notes"
    }
}
