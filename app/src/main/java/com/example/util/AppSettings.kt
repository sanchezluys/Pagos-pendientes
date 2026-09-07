package com.example.util

enum class AppCurrency(val code: String, val displayName: String, val symbol: String) {
    USD(code = "USD", displayName = "Dólar estadounidense (USD)", symbol = "$"),
    COL(code = "COL", displayName = "Peso colombiano (COL)", symbol = "COL$"),
    SOL(code = "SOL", displayName = "Sol peruano (SOL)", symbol = "S/.")
}

enum class ThousandsSeparator(
    val code: String,
    val displayName: String,
    val sampleAmount: String
) {
    PUNTO(code = "PUNTO", displayName = "Punto (.)", sampleAmount = "1.250,50"),
    COMA(code = "COMA", displayName = "Coma (,)", sampleAmount = "1,250.50"),
    DESACTIVADO(code = "DESACTIVADO", displayName = "Desactivado", sampleAmount = "1250.50")
}

data class PlaceDetails(
    val alias: String = "",
    val address: String = "",
    val additionalData: String = "",
    val notes: String = ""
)

data class AppSettings(
    val currency: AppCurrency = AppCurrency.SOL,
    val thousandsSeparator: ThousandsSeparator = ThousandsSeparator.COMA,
    val isDarkTheme: Boolean = false,
    val showDecimals: Boolean = true,
    val enableCasa: Boolean = true,
    val enableNegocio: Boolean = true,
    val casaDetails: PlaceDetails = PlaceDetails(alias = "Casa"),
    val negocioDetails: PlaceDetails = PlaceDetails(alias = "Negocio")
)

