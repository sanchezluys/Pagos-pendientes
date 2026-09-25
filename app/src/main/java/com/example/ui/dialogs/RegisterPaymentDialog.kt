package com.example.ui.dialogs

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import com.example.data.PaymentReminder
import com.example.ui.theme.StatusPaid
import com.example.util.AppSettings
import com.example.util.DateFormats
import com.example.util.ImageUtils
import java.io.File
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

private data class DifferentialData(
    val bgColor: androidx.compose.ui.graphics.Color,
    val borderColor: androidx.compose.ui.graphics.Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconTint: androidx.compose.ui.graphics.Color,
    val title: String,
    val subtitle: String
)

@Composable
fun RegisterPaymentDialog(
    payment: PaymentReminder,
    settings: AppSettings = AppSettings(),
    onDismiss: () -> Unit,
    onConfirmPayment: (
        paidAmount: Double,
        paidDateMillis: Long,
        receiptPhotoPath: String?,
        note: String?
    ) -> Unit
) {
    val context = LocalContext.current

    var paidAmountValue by remember {
        mutableStateOf(
            if (payment.approxAmount > 0) {
                val formatted = DateFormats.formatNumber(payment.approxAmount, settings.thousandsSeparator, settings.showDecimals)
                TextFieldValue(text = formatted, selection = TextRange(formatted.length))
            } else {
                TextFieldValue("")
            }
        )
    }

    val todayCal = Calendar.getInstance()
    var paidYear by remember { mutableIntStateOf(todayCal.get(Calendar.YEAR)) }
    var paidMonth by remember { mutableIntStateOf(todayCal.get(Calendar.MONTH)) }
    var paidDay by remember { mutableIntStateOf(todayCal.get(Calendar.DAY_OF_MONTH)) }

    val calculatedPaidDateMillis = remember(paidYear, paidMonth, paidDay) {
        Calendar.getInstance().apply {
            set(paidYear, paidMonth, paidDay, todayCal.get(Calendar.HOUR_OF_DAY), todayCal.get(Calendar.MINUTE))
        }.timeInMillis
    }

    var receiptPhotoPath by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf("") }

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Camera Capture Launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            coroutineScope.launch {
                val savedPath = ImageUtils.saveUriToAppStorage(context, tempCameraUri!!)
                if (savedPath != null) {
                    receiptPhotoPath = savedPath
                    Toast.makeText(context, "Foto de comprobante guardada", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Gallery Picker Launcher (Zero-permission Android Photo Picker)
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val savedPath = ImageUtils.saveUriToAppStorage(context, uri)
                if (savedPath != null) {
                    receiptPhotoPath = savedPath
                    Toast.makeText(context, "Comprobante cargado correctamente", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun launchCamera() {
        try {
            val uri = ImageUtils.createTempPictureUri(context)
            tempCameraUri = uri
            takePictureLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(context, "Error al abrir la cámara: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
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
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusPaid,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Registrar Pago",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${payment.title} (${payment.place})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_register_payment_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. ETIQUETA: MONTO A PAGAR CONFIGURADO
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("configured_amount_label_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "MONTO A PAGAR CONFIGURADO",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Pendiente",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = DateFormats.formatCurrency(payment.approxAmount, settings),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (payment.approxAmount > 0) {
                                val expectedFormatted = DateFormats.formatNumber(payment.approxAmount, settings.thousandsSeparator, settings.showDecimals)
                                val isSame = paidAmountValue.text == expectedFormatted
                                if (!isSame) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                paidAmountValue = TextFieldValue(
                                                    text = expectedFormatted,
                                                    selection = TextRange(expectedFormatted.length)
                                                )
                                            }
                                            .testTag("use_configured_amount_button")
                                    ) {
                                        Text(
                                            text = "Usar monto configurado",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. MONTO PAGADO INPUT (con formateo automático de decimales y miles)
                OutlinedTextField(
                    value = paidAmountValue,
                    onValueChange = { newVal ->
                        paidAmountValue = DateFormats.formatLiveAmountTextFieldValue(
                            previousValue = paidAmountValue,
                            newValue = newVal,
                            separator = settings.thousandsSeparator,
                            showDecimals = settings.showDecimals
                        )
                    },
                    label = { Text("Monto pagado (${settings.currency.symbol})") },
                    placeholder = { Text(DateFormats.formatNumber(1250.50, settings.thousandsSeparator, settings.showDecimals)) },
                    prefix = {
                        Text(
                            text = "${settings.currency.symbol} ",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (paidAmountValue.text.isNotEmpty()) {
                            IconButton(
                                onClick = { paidAmountValue = TextFieldValue("") },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpiar monto",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    supportingText = {
                        Text("Formato automático de miles (${settings.thousandsSeparator.displayName})")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("paid_amount_input")
                )

                // 3. VALOR DIFERENCIAL (Pendiente vs Pagado realmente)
                val parsedPaid = remember(paidAmountValue.text, settings.thousandsSeparator) {
                    DateFormats.parseAmount(paidAmountValue.text, settings.thousandsSeparator) ?: 0.0
                }
                val configured = payment.approxAmount

                if (parsedPaid > 0.0 && configured > 0.0) {
                    val diff = parsedPaid - configured
                    val isExact = abs(diff) < 0.005

                    val diffInfo = when {
                        isExact -> {
                            val cBg = StatusPaid.copy(alpha = 0.12f)
                            val bCol = StatusPaid.copy(alpha = 0.35f)
                            val title = "Monto exacto: ${DateFormats.formatCurrency(parsedPaid, settings)}"
                            val sub = "El monto pagado coincide exactamente con lo configurado (Diferencia: ${settings.currency.symbol} 0.00)."
                            DifferentialData(cBg, bCol, Icons.Default.CheckCircle, StatusPaid, title, sub)
                        }
                        diff < 0 -> {
                            val absDiff = abs(diff)
                            val cBg = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
                            val bCol = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
                            val title = "Diferencial: -${DateFormats.formatCurrency(absDiff, settings)}"
                            val sub = "Estás pagando ${DateFormats.formatCurrency(absDiff, settings)} menos que el monto configurado."
                            DifferentialData(cBg, bCol, Icons.Default.Paid, MaterialTheme.colorScheme.tertiary, title, sub)
                        }
                        else -> {
                            val cBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            val bCol = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            val title = "Diferencial: +${DateFormats.formatCurrency(diff, settings)}"
                            val sub = "Estás pagando ${DateFormats.formatCurrency(diff, settings)} más que el monto configurado (posible recargo o mora)."
                            DifferentialData(cBg, bCol, Icons.Default.Paid, MaterialTheme.colorScheme.primary, title, sub)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = diffInfo.bgColor,
                        border = BorderStroke(1.dp, diffInfo.borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("payment_differential_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = diffInfo.icon,
                                contentDescription = null,
                                tint = diffInfo.iconTint,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = diffInfo.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = diffInfo.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Date
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    paidYear = y
                                    paidMonth = m
                                    paidDay = d
                                },
                                paidYear,
                                paidMonth,
                                paidDay
                            ).show()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Fecha de pago",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = DateFormats.formatDate(calculatedPaidDateMillis),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Receipt Photo Section
                Text(
                    text = "Foto del Comprobante de Pago",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (receiptPhotoPath == null) {
                    // Two buttons: Camera and Gallery
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { launchCamera() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("camera_receipt_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Tomar foto",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tomar foto")
                        }

                        OutlinedButton(
                            onClick = {
                                pickImageLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("gallery_receipt_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Galería",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("De galería")
                        }
                    }
                } else {
                    // Preview of attached receipt
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(File(receiptPhotoPath!!))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Comprobante de pago",
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Delete / Retake photo button
                        IconButton(
                            onClick = { receiptPhotoPath = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Quitar foto",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Optional Note Input
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Nota adicional (opcional)") },
                    placeholder = { Text("Ej: Pagado por transferencia BCP, ref...") },
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_note_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val parsedAmount = DateFormats.parseAmount(paidAmountValue.text, settings.thousandsSeparator)
                            if (parsedAmount == null || parsedAmount <= 0) {
                                Toast.makeText(context, "Ingresa un monto válido", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            onConfirmPayment(
                                parsedAmount,
                                calculatedPaidDateMillis,
                                receiptPhotoPath,
                                note.ifBlank { null }
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusPaid
                        ),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("confirm_register_payment_button")
                    ) {
                        Text("Guardar Pago", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
