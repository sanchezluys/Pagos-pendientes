package com.example.util

data class ParsedVoiceReminder(
    val rawText: String,
    val suggestedTitle: String,
    val suggestedAmount: Double?,
    val suggestedCode: String?,
    val suggestedPlace: String?,
    val suggestedCategory: String?
)

object VoiceSpeechParser {

    fun parse(text: String): ParsedVoiceReminder {
        val clean = text.trim()
        val lower = clean.lowercase()

        // Extract amount: looking for numbers like "150", "150.50", "$ 120", "s/ 80"
        val amountRegex = Regex("""(?:s/\.?|\$|soles|dolares|dólares|monto|de)?\s*([0-9]+(?:[.,][0-9]{1,2})?)""")
        val numberRegex = Regex("""\b([0-9]+(?:[.,][0-9]{1,2})?)\b""")

        var extractedAmount: Double? = null
        val numberMatches = numberRegex.findAll(lower).toList()
        for (match in numberMatches) {
            val numStr = match.groupValues[1].replace(',', '.')
            val parsed = numStr.toDoubleOrNull()
            // If the number looks like a reasonable amount (e.g. <= 100000) and not an 8+ digit reference
            if (parsed != null && numStr.length <= 6 && parsed > 0.0) {
                extractedAmount = parsed
                break
            }
        }

        // Extract place
        val suggestedPlace = when {
            lower.contains("negocio") || lower.contains("trabajo") || lower.contains("tienda") || lower.contains("oficina") -> "Negocio"
            lower.contains("casa") || lower.contains("hogar") || lower.contains("depa") -> "Casa"
            else -> null
        }

        // Extract category
        val suggestedCategory = when {
            lower.contains("tarjeta") || lower.contains("credito") || lower.contains("crédito") || lower.contains("prestamo") || lower.contains("préstamo") -> "Créditos"
            lower.contains("deuda") || lower.contains("debo") || lower.contains("prestado") -> "Deudas"
            lower.contains("luz") || lower.contains("agua") || lower.contains("gas") || lower.contains("internet") || lower.contains("telefono") || lower.contains("teléfono") || lower.contains("servicio") || lower.contains("celular") || lower.contains("cable") -> "Servicios"
            lower.contains("impuesto") || lower.contains("sunat") || lower.contains("arbitrios") || lower.contains("tributo") -> "Impuestos"
            else -> null
        }

        // Extract reference code if words like "referencia", "codigo", "código", "recibo" are followed by digits
        val refRegex = Regex("""(?:referencia|código|codigo|recibo|numero|número)\s*[:#]?\s*([A-Za-z0-9-]+)""")
        val refMatch = refRegex.find(lower)
        val suggestedCode = refMatch?.groupValues?.get(1)?.uppercase()

        // Clean title: remove "recordar", "pagar", "anotar", etc.
        var titleCandidate = clean
            .replace(Regex("""(?i)^(por favor\s+)?(recordar\s+)?(pagar\s+)?(pago\s+de\s+)?(debo\s+pagar\s+)?(anotar\s+)?"""), "")
            .trim()

        if (titleCandidate.length > 50) {
            titleCandidate = titleCandidate.take(50)
        }
        if (titleCandidate.isBlank()) {
            titleCandidate = clean
        }

        return ParsedVoiceReminder(
            rawText = clean,
            suggestedTitle = titleCandidate.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            suggestedAmount = extractedAmount,
            suggestedCode = suggestedCode,
            suggestedPlace = suggestedPlace,
            suggestedCategory = suggestedCategory
        )
    }
}
