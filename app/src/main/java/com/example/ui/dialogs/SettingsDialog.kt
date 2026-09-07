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
import androidx.compose.material.icons.filled.AlternateEmail
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
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Close
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
import com.example.BuildConfig
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
    onToggleShowDecimals: (Boolean) -> Unit,
    onToggleEnableCasa: (Boolean) -> Unit,
    onToggleEnableNegocio: (Boolean) -> Unit,
    onSaveCasaDetails: (PlaceDetails) -> Unit,
    onSaveNegocioDetails: (PlaceDetails) -> Unit,
    onOpenCategoryManager: () -> Unit
) {
    var selectedPlaceTab by remember(currentSettings.enableCasa, currentSettings.enableNegocio) {
        mutableStateOf(if (currentSettings.enableCasa) "Casa" else "Negocio")
    }

    var casaAlias by remember(currentSettings.casaDetails.alias) { mutableStateOf(currentSettings.casaDetails.alias) }
    var casaAddress by remember(currentSettings.casaDetails.address) { mutableStateOf(currentSettings.casaDetails.address) }
    var casaData by remember(currentSettings.casaDetails.additionalData) { mutableStateOf(currentSettings.casaDetails.additionalData) }
    var casaNotes by remember(currentSettings.casaDetails.notes) { mutableStateOf(currentSettings.casaDetails.notes) }

    var negocioAlias by remember(currentSettings.negocioDetails.alias) { mutableStateOf(currentSettings.negocioDetails.alias) }
    var negocioAddress by remember(currentSettings.negocioDetails.address) { mutableStateOf(currentSettings.negocioDetails.address) }
    var negocioData by remember(currentSettings.negocioDetails.additionalData) { mutableStateOf(currentSettings.negocioDetails.additionalData) }
    var negocioNotes by remember(currentSettings.negocioDetails.notes) { mutableStateOf(currentSettings.negocioDetails.notes) }

    // Auto-save helper on dismiss or edit
    val autoSaveAll = {
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
            autoSaveAll()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header with Auto-save indicator
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF2E7D32).copy(alpha = 0.15f),
                                    modifier = Modifier.size(14.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Guardado automático",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            autoSaveAll()
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

                // 1. TEMA Y APARIENCIA
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

                Spacer(modifier = Modifier.height(14.dp))

                // 2. USO DE DECIMALES (ACTIVAR / DESACTIVAR)
                Text(
                    text = "FORMATO DE MONTOS",
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
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleShowDecimals(!currentSettings.showDecimals) }
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
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Numbers,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Uso de Decimales",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (currentSettings.showDecimals) {
                                        "Activado (muestra centavos, ej: 1.250,50)"
                                    } else {
                                        "Desactivado (sin decimales, ej: 1.250)"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = currentSettings.showDecimals,
                            onCheckedChange = { onToggleShowDecimals(it) },
                            modifier = Modifier.testTag("show_decimals_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Live Preview Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "VISTA PREVIA EN VIVO",
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
                            text = "Formato automático con moneda, separador y decimales activos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. ACTIVAR O DESACTIVAR CASA O NEGOCIO
                Text(
                    text = "ÁMBITOS ACTIVOS (CASA / NEGOCIO)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Activa o desactiva Casa o Negocio para usar ambos o uno solo en la app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )

                // Switches to enable/disable Casa and Negocio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Casa Switch Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (currentSettings.enableCasa) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(
                            width = if (currentSettings.enableCasa) 1.5.dp else 1.dp,
                            color = if (currentSettings.enableCasa) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (currentSettings.enableCasa && !currentSettings.enableNegocio) {
                                    // cannot disable both
                                } else {
                                    onToggleEnableCasa(!currentSettings.enableCasa)
                                }
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Switch(
                                    checked = currentSettings.enableCasa,
                                    enabled = !currentSettings.enableCasa || currentSettings.enableNegocio,
                                    onCheckedChange = { onToggleEnableCasa(it) },
                                    modifier = Modifier.testTag("toggle_enable_casa_switch")
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Casa",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (currentSettings.enableCasa) "Activo en la app" else "Desactivado",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Negocio Switch Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (currentSettings.enableNegocio) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(
                            width = if (currentSettings.enableNegocio) 1.5.dp else 1.dp,
                            color = if (currentSettings.enableNegocio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (currentSettings.enableNegocio && !currentSettings.enableCasa) {
                                    // cannot disable both
                                } else {
                                    onToggleEnableNegocio(!currentSettings.enableNegocio)
                                }
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Switch(
                                    checked = currentSettings.enableNegocio,
                                    enabled = !currentSettings.enableNegocio || currentSettings.enableCasa,
                                    onCheckedChange = { onToggleEnableNegocio(it) },
                                    modifier = Modifier.testTag("toggle_enable_negocio_switch")
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Negocio",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (currentSettings.enableNegocio) "Activo en la app" else "Desactivado",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. DATOS DE LOS LUGARES ACTIVOS
                Text(
                    text = "DATOS DEL LUGAR",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Dirección, alias, notas y suministros (se guardan automáticamente mientras escribes).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )

                // Place Tabs Capsule (only if both are enabled)
                if (currentSettings.enableCasa && currentSettings.enableNegocio) {
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
                                    .clickable { selectedPlaceTab = "Casa" }
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
                                    .clickable { selectedPlaceTab = "Negocio" }
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
                }

                // Place Form Card
                val isCasa = if (currentSettings.enableCasa && currentSettings.enableNegocio) {
                    selectedPlaceTab == "Casa"
                } else {
                    currentSettings.enableCasa
                }

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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isCasa) Icons.Default.Home else Icons.Default.Business,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isCasa) "Datos de Casa" else "Datos del Negocio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // 1. Alias
                        OutlinedTextField(
                            value = if (isCasa) casaAlias else negocioAlias,
                            onValueChange = { newVal ->
                                if (isCasa) {
                                    casaAlias = newVal
                                    onSaveCasaDetails(
                                        PlaceDetails(
                                            alias = newVal.trim().ifBlank { "Casa" },
                                            address = casaAddress.trim(),
                                            additionalData = casaData.trim(),
                                            notes = casaNotes.trim()
                                        )
                                    )
                                } else {
                                    negocioAlias = newVal
                                    onSaveNegocioDetails(
                                        PlaceDetails(
                                            alias = newVal.trim().ifBlank { "Negocio" },
                                            address = negocioAddress.trim(),
                                            additionalData = negocioData.trim(),
                                            notes = negocioNotes.trim()
                                        )
                                    )
                                }
                            },
                            label = { Text("Alias del lugar") },
                            placeholder = {
                                Text(if (isCasa) "Ej: Mi Casa, Dpto 402, Playa" else "Ej: Local Centro, Tienda 1, Taller")
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Label, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(if (isCasa) "input_casa_alias" else "input_negocio_alias")
                        )

                        // 2. Dirección
                        OutlinedTextField(
                            value = if (isCasa) casaAddress else negocioAddress,
                            onValueChange = { newVal ->
                                if (isCasa) {
                                    casaAddress = newVal
                                    onSaveCasaDetails(
                                        PlaceDetails(
                                            alias = casaAlias.trim().ifBlank { "Casa" },
                                            address = newVal.trim(),
                                            additionalData = casaData.trim(),
                                            notes = casaNotes.trim()
                                        )
                                    )
                                } else {
                                    negocioAddress = newVal
                                    onSaveNegocioDetails(
                                        PlaceDetails(
                                            alias = negocioAlias.trim().ifBlank { "Negocio" },
                                            address = newVal.trim(),
                                            additionalData = negocioData.trim(),
                                            notes = negocioNotes.trim()
                                        )
                                    )
                                }
                            },
                            label = { Text("Dirección") },
                            placeholder = {
                                Text(if (isCasa) "Ej: Av. Las Palmeras 123" else "Ej: Jr. Comercio 450, Int. 2")
                            },
                            leadingIcon = {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(if (isCasa) "input_casa_address" else "input_negocio_address")
                        )

                        // 3. Datos adicionales
                        OutlinedTextField(
                            value = if (isCasa) casaData else negocioData,
                            onValueChange = { newVal ->
                                if (isCasa) {
                                    casaData = newVal
                                    onSaveCasaDetails(
                                        PlaceDetails(
                                            alias = casaAlias.trim().ifBlank { "Casa" },
                                            address = casaAddress.trim(),
                                            additionalData = newVal.trim(),
                                            notes = casaNotes.trim()
                                        )
                                    )
                                } else {
                                    negocioData = newVal
                                    onSaveNegocioDetails(
                                        PlaceDetails(
                                            alias = negocioAlias.trim().ifBlank { "Negocio" },
                                            address = negocioAddress.trim(),
                                            additionalData = newVal.trim(),
                                            notes = negocioNotes.trim()
                                        )
                                    )
                                }
                            },
                            label = { Text("Datos adicionales (suministro, contrato, RUC)") },
                            placeholder = {
                                Text(if (isCasa) "Ej: Sum. Luz: 104928, Cód. Agua: 5543" else "Ej: RUC: 20123456789, Contrato: 8821")
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            minLines = 1,
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(if (isCasa) "input_casa_data" else "input_negocio_data")
                        )

                        // 4. Notas
                        OutlinedTextField(
                            value = if (isCasa) casaNotes else negocioNotes,
                            onValueChange = { newVal ->
                                if (isCasa) {
                                    casaNotes = newVal
                                    onSaveCasaDetails(
                                        PlaceDetails(
                                            alias = casaAlias.trim().ifBlank { "Casa" },
                                            address = casaAddress.trim(),
                                            additionalData = casaData.trim(),
                                            notes = newVal.trim()
                                        )
                                    )
                                } else {
                                    negocioNotes = newVal
                                    onSaveNegocioDetails(
                                        PlaceDetails(
                                            alias = negocioAlias.trim().ifBlank { "Negocio" },
                                            address = negocioAddress.trim(),
                                            additionalData = negocioData.trim(),
                                            notes = newVal.trim()
                                        )
                                    )
                                }
                            },
                            label = { Text("Notas y observaciones") },
                            placeholder = {
                                Text(if (isCasa) "Ej: Los recibos vencen los días 15 de cada mes" else "Ej: Pagar alquiler antes del 5 vía transferencia")
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            minLines = 2,
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(if (isCasa) "input_casa_notes" else "input_negocio_notes")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 5. SELECCIÓN DE MONEDA
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

                // 6. SEPARACIÓN DE MILES
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
                                        text = DateFormats.formatCurrency(1250.50, currentSettings.currency, separator, currentSettings.showDecimals),
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

                // 7. ADMINISTRAR CATEGORÍAS
                OutlinedButton(
                    onClick = {
                        autoSaveAll()
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
                        text = "Administrar Categorías",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 8. INFORMACIÓN DE LA APLICACIÓN (VERSIÓN DINÁMICA Y AUTOR)
                Text(
                    text = "INFORMACIÓN DE LA APLICACIÓN",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Nombre y versión
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Pagos Pendientes",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Control inteligente de vencimientos y recibos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "v${BuildConfig.VERSION_NAME}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Autor
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AlternateEmail,
                                            contentDescription = "Autor",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Desarrollador / Autor",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "sanchezluys@gmail.com",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
