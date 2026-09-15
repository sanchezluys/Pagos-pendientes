package com.example.util

enum class AppCurrency(
    val code: String,
    val countryName: String,
    val flagEmoji: String,
    val displayName: String,
    val symbol: String
) {
    // Argentina
    ARS(code = "ARS", countryName = "Argentina", flagEmoji = "🇦🇷", displayName = "Peso argentino (ARS)", symbol = "$"),
    // Bolivia
    BOB(code = "BOB", countryName = "Bolivia", flagEmoji = "🇧🇴", displayName = "Boliviano (BOB)", symbol = "Bs."),
    // Brasil
    BRL(code = "BRL", countryName = "Brasil", flagEmoji = "🇧🇷", displayName = "Real brasileño (BRL)", symbol = "R$"),
    // Chile
    CLP(code = "CLP", countryName = "Chile", flagEmoji = "🇨🇱", displayName = "Peso chileno (CLP)", symbol = "$"),
    // Colombia
    COP(code = "COP", countryName = "Colombia", flagEmoji = "🇨🇴", displayName = "Peso colombiano (COP)", symbol = "$"),
    // Costa Rica
    CRC(code = "CRC", countryName = "Costa Rica", flagEmoji = "🇨🇷", displayName = "Colón costarricense (CRC)", symbol = "₡"),
    // Ecuador
    USD_EC(code = "USD", countryName = "Ecuador", flagEmoji = "🇪🇨", displayName = "Dólar estadounidense (USD)", symbol = "$"),
    // El Salvador
    USD_SV(code = "USD", countryName = "El Salvador", flagEmoji = "🇸🇻", displayName = "Dólar estadounidense (USD)", symbol = "$"),
    // Estados Unidos
    USD(code = "USD", countryName = "Estados Unidos", flagEmoji = "🇺🇸", displayName = "Dólar estadounidense (USD)", symbol = "$"),
    // Guatemala
    GTQ(code = "GTQ", countryName = "Guatemala", flagEmoji = "🇬🇹", displayName = "Quetzal guatemalteco (GTQ)", symbol = "Q"),
    // Honduras
    HNL(code = "HNL", countryName = "Honduras", flagEmoji = "🇭🇳", displayName = "Lempira hondureño (HNL)", symbol = "L"),
    // México
    MXN(code = "MXN", countryName = "México", flagEmoji = "🇲🇽", displayName = "Peso mexicano (MXN)", symbol = "$"),
    // Panamá
    PAB(code = "PAB", countryName = "Panamá", flagEmoji = "🇵🇦", displayName = "Balboa / Dólar (PAB)", symbol = "$"),
    // Paraguay
    PYG(code = "PYG", countryName = "Paraguay", flagEmoji = "🇵🇾", displayName = "Guaraní paraguayo (PYG)", symbol = "₲"),
    // Perú
    SOL(code = "SOL", countryName = "Perú", flagEmoji = "🇵🇪", displayName = "Sol peruano (SOL)", symbol = "S/."),
    // República Dominicana
    DOP(code = "DOP", countryName = "República Dominicana", flagEmoji = "🇩🇴", displayName = "Peso dominicano (DOP)", symbol = "RD$"),
    // Uruguay
    UYU(code = "UYU", countryName = "Uruguay", flagEmoji = "🇺🇾", displayName = "Peso uruguayo (UYU)", symbol = "$")
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

