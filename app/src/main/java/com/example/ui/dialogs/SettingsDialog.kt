package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.util.AppCurrency
import com.example.util.AppSettings
import com.example.util.DateFormats
import com.example.util.PlaceDetails
import com.example.util.ThousandsSeparator

@Composable
fun SettingsDialog(
    currentSettings: AppSettings,
    onDismiss: () -> Unit,
    onCurrencySelected: (AppCurrency) -> Unit,
    onSeparatorSelected: (ThousandsSeparator) -> Unit,
    onToggleDarkTheme: (Boolean) -> Unit,
    onSaveCasaDetails: (PlaceDetails) -> Unit,
    onSaveNegocioDetails: (PlaceDetails) -> Unit,
    onOpenCategoryManager: () -> Unit
) {
    var selectedPlaceTab by remember { mutableStateOf("Casa") } // "Casa" or "Negocio"

    var casaAlias by remember(currentSettings.casaDetails) { mutableStateOf(currentSettings.casaDetails.alias) }
    var casaAddress by remember(currentSettings.casaDetails) { mutableStateOf(currentSettings.casaDetails.address) }
    var casaData by remember(currentSettings.casaDetails) { mutableStateOf(currentSettings.casaDetails.additionalData) }
    var casaNotes by remember(currentSettings.casaDetails) { mutableStateOf(currentSettings.casaDetails.notes) }

    var negocioAlias by remember(currentSettings.negocioDetails) { mutableStateOf(currentSettings.negocioDetails.alias) }
    var negocioAddress by remember(currentSettings.negocioDetails) { mutableStateOf(currentSettings.negocioDetails.address) }
    var negocioData by remember(currentSettings.negocioDetails) { mutableStateOf(currentSettings.negocioDetails.additionalData) }
    var negocioNotes by remember(currentSettings.negocioDetails) { mutableStateOf(currentSettings.negocioDetails.notes) }

    var savedFeedbackMessage by remember { mutableStateOf<String?>(null) }

    val saveCurrentDetails = {
        onSaveCasaDetails(
            PlaceDetails(
                alias = casaAlias.trim().ifBlank { "Casa" },
                address = casaAddress.trim(),
                additionalData = casaData.trim(),
                notes = casaNotes.trim()
            )
        )
        onSaveNegocioDetails(
            PlaceDetails(
                alias = negocioAlias.trim().ifBlank { "Negocio" },
                address = negocioAddress.trim(),
                additionalData = negocioData.trim(),
                notes = negocioNotes.trim()
            )
        )
    }

    Dialog(
        onDismissRequest = {
            saveCurrentDetails()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Configuración",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tema, moneda, lugares y categorías",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            saveCurrentDetails()
                            onDismiss()
                        },
                        modifier = Modifier.testTag("close_settings_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // TEMA OSCURO (ACTIVAR / DESACTIVAR)
                Text(
                    text = "TEMA Y APARIENCIA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentSettings.isDarkTheme) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleDarkTheme(!currentSettings.isDarkTheme) }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (currentSettings.isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Tema Oscuro",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (currentSettings.isDarkTheme) "Activado (modo nocturno)" else "Desactivado (modo claro)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = currentSettings.isDarkTheme,
                            onCheckedChange = { onToggleDarkTheme(it) },
                            modifier = Modifier.testTag("dark_theme_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Live Preview Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "VISTA PREVIA DE FORMATO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = DateFormats.formatCurrency(1250.50, currentSettings),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Ejemplo con la moneda y separador seleccionados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 1. Datos de Casa y Negocio Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DATOS DE CASA Y NEGOCIO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF49454F)
                    )
                }

                Text(
                    text = "Configura la dirección, alias, datos (RUC/suministro) y notas de cada lugar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF79747E),
                    modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                )

                // Place Tabs Capsule
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Casa Tab
                        val isCasaSelected = selectedPlaceTab == "Casa"
                        Surface(
                            shape = CircleShape,
                            color = if (isCasaSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                            shadowElevation = if (isCasaSelected) 1.5.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .clickable {
                                    selectedPlaceTab = "Casa"
                                    savedFeedbackMessage = null
                                }
                                .testTag("settings_place_tab_casa")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isCasaSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (casaAlias.isNotBlank()) "Casa ($casaAlias)" else "Casa",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isCasaSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCasaSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }

                        // Negocio Tab
                        val isNegocioSelected = selectedPlaceTab == "Negocio"
                        Surface(
                            shape = CircleShape,
                            color = if (isNegocioSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                            shadowElevation = if (isNegocioSelected) 1.5.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .clickable {
                                    selectedPlaceTab = "Negocio"
                                    savedFeedbackMessage = null
                                }
                                .testTag("settings_place_tab_negocio")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isNegocioSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (negocioAlias.isNotBlank()) "Negocio ($negocioAlias)" else "Negocio",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isNegocioSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isNegocioSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Place Form Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val isCasa = selectedPlaceTab == "Casa"

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isCasa) Icons.Default.Home else Icons.Default.Business,
                                contentDescription = null,
                                tint = Color(0xFF6750A4),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isCasa) "Datos de Casa" else "Datos del Negocio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF21005D)
                            )
                        }

                        // 1. Alias
                        OutlinedTextField(
                            value = if (isCasa) casaAlias else negocioAlias,
                            onValueChange = {
                                if (isCasa) casaAlias = it else negocioAlias = it
                            },
                            label = { Text("Alias del lugar") },
                            placeholder = {
                                Text(if (isCasa) "Ej: Mi Casa, Dpto 402, Playa" else "Ej: Local Centro, Tienda 1, Taller")
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Label, contentDescription = null, tint = Color(0xFF6750A4))
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6750A4),
                                focusedLabelColor = Color(0xFF6750A4)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(if (isCasa) "input_casa_alias" else "input_negocio_alias")
                        )

                        // 2. Dirección
                        OutlinedTextField(
                            value = if (isCasa) casaAddress else negocioAddress,
                            onValueChange = {
                                if (isCasa) casaAddress = it else negocioAddress = it
                            },
                            label = { Text("Dirección") },
                            placeholder = {
                                Text(if (isCasa) "Ej: Av. Las Palmeras 123" else "Ej: Jr. Comercio 450, Int. 2")
                            },
                            leadingIcon = {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF6750A4))
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6750A4),
                                focusedLabelColor = Color(0xFF6750A4)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(if (isCasa) "input_casa_address" else "input_negocio_address")
                        )

                        // 3. Datos adicionales
                        OutlinedTextField(
                            value = if (isCasa) casaData else negocioData,
                            onValueChange = {
                                if (isCasa) casaData = it else negocioData = it
                            },
                            label = { Text("Datos adicionales") },
                            placeholder = {
                                Text(if (isCasa) "Ej: Sum. Luz: 104928, Cód. Agua: 5543" else "Ej: RUC: 20123456789, Contrato: 8821")
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF6750A4))
                            },
                            minLines = 1,
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6750A4),
                                focusedLabelColor = Color(0xFF6750A4)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(if (isCasa) "input_casa_data" else "input_negocio_data")
                        )

                        // 4. Notas
                        OutlinedTextField(
                            value = if (isCasa) casaNotes else negocioNotes,
                            onValueChange = {
                                if (isCasa) casaNotes = it else negocioNotes = it
                            },
                            label = { Text("Notas y observaciones") },
                            placeholder = {
                                Text(if (isCasa) "Ej: Los recibos vencen los días 15 de cada mes" else "Ej: Pagar alquiler antes del 5 vía transferencia")
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Notes, contentDescription = null, tint = Color(0xFF6750A4))
                            },
                            minLines = 2,
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6750A4),
                                focusedLabelColor = Color(0xFF6750A4)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(if (isCasa) "input_casa_notes" else "input_negocio_notes")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (savedFeedbackMessage != null) {
                                Text(
                                    text = savedFeedbackMessage!!,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                            Button(
                                onClick = {
                                    saveCurrentDetails()
                                    savedFeedbackMessage = "¡Datos guardados!"
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEADDFF)),
                                modifier = Modifier.testTag("save_place_details_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF21005D),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Guardar cambios de ${if (isCasa) "Casa" else "Negocio"}",
                                    color = Color(0xFF21005D),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Moneda Selection Section
                Text(
                    text = "SELECCIÓN DE MONEDA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppCurrency.values().forEach { currency ->
                        val isSelected = currentSettings.currency == currency
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onCurrencySelected(currency) }
                                .testTag("currency_option_${currency.code.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { onCurrencySelected(currency) },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = currency.displayName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Símbolo: ${currency.symbol}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = currency.code,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. Separador de miles Section
                Text(
                    text = "SEPARACIÓN DE MILES",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThousandsSeparator.values().forEach { separator ->
                        val isSelected = currentSettings.thousandsSeparator == separator
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSeparatorSelected(separator) }
                                .testTag("separator_option_${separator.code.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { onSeparatorSelected(separator) },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = separator.displayName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = when (separator) {
                                                ThousandsSeparator.PUNTO -> "Miles con punto, decimales con coma"
                                                ThousandsSeparator.COMA -> "Miles con coma, decimales con punto"
                                                ThousandsSeparator.DESACTIVADO -> "Sin separador de miles"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "${currentSettings.currency.symbol} ${separator.sampleAmount}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Direct Access to Categories Management
                OutlinedButton(
                    onClick = {
                        saveCurrentDetails()
                        onOpenCategoryManager()
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_open_categories_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Administrar Categorías (Activar/Borrar)",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Done Button
                Button(
                    onClick = {
                        saveCurrentDetails()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_save_and_close_button")
                ) {
                    Text(
                        text = "Guardar y Cerrar",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
