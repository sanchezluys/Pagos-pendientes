package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.filled.Settings
import com.example.data.CategoryItem
import com.example.data.PaymentReminder
import com.example.ui.PaymentsViewModel
import com.example.ui.StatusTab
import com.example.ui.components.PaymentCard
import com.example.ui.dialogs.CategoryManagerDialog
import com.example.ui.dialogs.NewReminderDialog
import com.example.ui.dialogs.ReceiptViewDialog
import com.example.ui.dialogs.RegisterPaymentDialog
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPending
import com.example.util.AppSettings
import com.example.util.DateFormats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPaymentsScreen(
    viewModel: PaymentsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    val selectedPlace by viewModel.selectedPlace.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val activeCategories by viewModel.activeCategories.collectAsStateWithLifecycle()
    val payments by viewModel.filteredPayments.collectAsStateWithLifecycle()
    val stats by viewModel.currentPlaceStats.collectAsStateWithLifecycle()

    var showNewReminderDialog by remember { mutableStateOf(false) }
    var showCategoryManagerDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var paymentToRegister by remember { mutableStateOf<PaymentReminder?>(null) }
    var paymentToViewReceipt by remember { mutableStateOf<PaymentReminder?>(null) }
    var paymentToDelete by remember { mutableStateOf<PaymentReminder?>(null) }

    // Runtime Permission Request for Notifications & Camera
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Mis Pagos Pendientes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.5).sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Control centralizado",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { showCategoryManagerDialog = true }
                            .testTag("open_category_manager_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = "Administrar categorías",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { showSettingsDialog = true }
                            .testTag("open_settings_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Configuración",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewReminderDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary) },
                text = { Text("Nuevo Pago", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimary) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.testTag("add_payment_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Place Selector Capsule Navigation: Casa, Negocio, Todos
            val places = remember(appSettings.enableCasa, appSettings.enableNegocio) {
                if (appSettings.enableCasa && appSettings.enableNegocio) {
                    listOf("Todos", "Casa", "Negocio")
                } else if (appSettings.enableCasa) {
                    listOf("Casa")
                } else {
                    listOf("Negocio")
                }
            }

            LaunchedEffect(places) {
                if (!places.contains(selectedPlace)) {
                    viewModel.setPlace(places.first())
                }
            }

            // Tarjeta Principal Unificada con el Ámbito Activo (Altura optimizada y compacta)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("hero_total_card"),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    // 1. Selector de Ámbito Activo integrado dentro de la cabecera de la tarjeta
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            places.forEach { place ->
                                val isSelected = selectedPlace.equals(place, ignoreCase = true)
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                                    shadowElevation = if (isSelected) 1.dp else 0.dp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(CircleShape)
                                        .clickable { viewModel.setPlace(place) }
                                        .testTag("place_tab_${place.lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val (icon, label) = when (place) {
                                            "Casa" -> Icons.Default.Home to (appSettings.casaDetails.alias.ifBlank { "Casa" })
                                            "Negocio" -> Icons.Default.Business to (appSettings.negocioDetails.alias.ifBlank { "Negocio" })
                                            else -> Icons.Default.AccountBalanceWallet to "Todos"
                                        }
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Fila Principal: Total Pendiente a la izquierda y badges resumidos a la derecha
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            val filterDisplay = when (selectedFilter) {
                                "THIS_MONTH" -> "ESTE MES"
                                "ALL" -> "TODAS"
                                else -> selectedFilter.uppercase()
                            }
                            Text(
                                text = "TOTAL PENDIENTE • $filterDisplay",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = DateFormats.formatCurrency(stats.pendingTotalAmount, appSettings),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                            ) {
                                Text(
                                    text = "${stats.pendingCount} por pagar",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            if (stats.paidCount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = StatusPaid.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${stats.paidCount} pagados",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusPaid,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 3. Información Completa del Ámbito Activo (Dirección, Datos, Notas en formato compacto)
                    val activePlaceInfo = when (selectedPlace) {
                        "Casa" -> appSettings.casaDetails
                        "Negocio" -> appSettings.negocioDetails
                        else -> null
                    }

                    if (activePlaceInfo != null && (activePlaceInfo.address.isNotBlank() || activePlaceInfo.additionalData.isNotBlank() || activePlaceInfo.notes.isNotBlank())) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                if (activePlaceInfo.address.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = activePlaceInfo.address,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                val extraDetails = listOfNotNull(
                                    activePlaceInfo.additionalData.takeIf { it.isNotBlank() },
                                    activePlaceInfo.notes.takeIf { it.isNotBlank() }
                                ).joinToString(" • ")

                                if (extraDetails.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = extraDetails,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Status Tabs: Pendientes vs Historial de Pagos
            TabRow(
                selectedTabIndex = if (selectedTab == StatusTab.PENDING) 0 else 1,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(
                            tabPositions[if (selectedTab == StatusTab.PENDING) 0 else 1]
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = selectedTab == StatusTab.PENDING,
                    onClick = { viewModel.setTab(StatusTab.PENDING) },
                    text = {
                        Text(
                            text = "Pendientes (${stats.pendingCount})",
                            fontWeight = if (selectedTab == StatusTab.PENDING) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == StatusTab.PENDING) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.testTag("tab_pending")
                )
                Tab(
                    selected = selectedTab == StatusTab.PAID,
                    onClick = { viewModel.setTab(StatusTab.PAID) },
                    text = {
                        Text(
                            text = "Historial Pagados (${stats.paidCount})",
                            fontWeight = if (selectedTab == StatusTab.PAID) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == StatusTab.PAID) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.testTag("tab_paid")
                )
            }

            // Main Filter Row: Este mes (default), Todos, Categories
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. "Este mes" (default)
                item {
                    FilterChip(
                        selected = selectedFilter == "THIS_MONTH",
                        onClick = { viewModel.setFilter("THIS_MONTH") },
                        label = { Text("Este mes") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("filter_chip_this_month")
                    )
                }

                // 2. "Todos"
                item {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { viewModel.setFilter("ALL") },
                        label = { Text("Todos") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("filter_chip_all")
                    )
                }

                // 3. Categories
                items(activeCategories, key = { it.id }) { cat ->
                    FilterChip(
                        selected = selectedFilter == cat.name,
                        onClick = {
                            viewModel.setFilter(if (selectedFilter == cat.name) "THIS_MONTH" else cat.name)
                        },
                        label = { Text(cat.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("filter_chip_${cat.name.lowercase()}")
                    )
                }
            }

            // Section header matching Clean Minimalism
            Text(
                text = if (selectedTab == StatusTab.PENDING) "PRÓXIMOS VENCIMIENTOS" else "HISTORIAL DE PAGOS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            // Payment items list or Empty state
            if (payments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (selectedTab == StatusTab.PENDING) Icons.Default.Paid else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (selectedTab == StatusTab.PENDING) MaterialTheme.colorScheme.primary else StatusPaid,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (selectedTab == StatusTab.PENDING) {
                                if (searchQuery.isNotBlank()) "No se encontraron pagos pendientes" else "¡No tienes pagos pendientes!"
                            } else {
                                "Sin historial de pagos realizados aún"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (selectedTab == StatusTab.PENDING) {
                                "Registra un recordatorio con fecha, código, alarma y lugar ($selectedPlace)."
                            } else {
                                "Cuando registres el pago con comprobante y fecha, aparecerán aquí ordenados."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(payments, key = { it.id }) { payment ->
                        PaymentCard(
                            payment = payment,
                            settings = appSettings,
                            onRegisterPaymentClick = { paymentToRegister = it },
                            onViewReceiptClick = { paymentToViewReceipt = it },
                            onDeleteClick = { paymentToDelete = it },
                            onMarkPendingClick = { viewModel.markAsPending(it, context) }
                        )
                    }
                }
            }
        }
    }

    // New Reminder Dialog (Supports Occasional & Recurring)
    if (showNewReminderDialog) {
        NewReminderDialog(
            categories = activeCategories,
            initialPlace = if (selectedPlace == "Negocio") "Negocio" else "Casa",
            appSettings = appSettings,
            onDismiss = { showNewReminderDialog = false },
            onSaveOccasional = { title, category, place, approxAmount, paymentCode, dueDateMillis, alertTimeMillis, iconName, colorHex ->
                viewModel.addPayment(
                    title = title,
                    category = category,
                    place = place,
                    approxAmount = approxAmount,
                    paymentCode = paymentCode,
                    dueDateMillis = dueDateMillis,
                    alertTimeMillis = alertTimeMillis,
                    context = context,
                    iconName = iconName,
                    colorHex = colorHex
                )
                showNewReminderDialog = false
            },
            onSaveRecurring = { title, category, place, approxAmount, paymentCode, dayOfMonth, startDateMillis, endDateMillis, alertHour, alertMinute, iconName, colorHex ->
                viewModel.addRecurringPayments(
                    title = title,
                    category = category,
                    place = place,
                    approxAmount = approxAmount,
                    paymentCode = paymentCode,
                    dayOfMonth = dayOfMonth,
                    startDateMillis = startDateMillis,
                    endDateMillis = endDateMillis,
                    alertHour = alertHour,
                    alertMinute = alertMinute,
                    context = context,
                    iconName = iconName,
                    colorHex = colorHex
                )
                showNewReminderDialog = false
            },
            onOpenCategoryManager = {
                showCategoryManagerDialog = true
            }
        )
    }

    // Register Payment Dialog
    if (paymentToRegister != null) {
        RegisterPaymentDialog(
            payment = paymentToRegister!!,
            settings = appSettings,
            onDismiss = { paymentToRegister = null },
            onConfirmPayment = { paidAmount, paidDateMillis, receiptPhotoPath, note ->
                viewModel.markAsPaid(
                    payment = paymentToRegister!!,
                    paidAmount = paidAmount,
                    paidDateMillis = paidDateMillis,
                    receiptPhotoUri = receiptPhotoPath,
                    note = note,
                    context = context
                )
                paymentToRegister = null
            }
        )
    }

    // View Receipt Dialog
    if (paymentToViewReceipt != null) {
        ReceiptViewDialog(
            payment = paymentToViewReceipt!!,
            settings = appSettings,
            onDismiss = { paymentToViewReceipt = null }
        )
    }

    // Category Manager Dialog
    if (showCategoryManagerDialog) {
        CategoryManagerDialog(
            categories = allCategories,
            onDismiss = { showCategoryManagerDialog = false },
            onAddCategory = { viewModel.addCategory(it) },
            onToggleCategoryActive = { cat, active -> viewModel.toggleCategoryActive(cat, active) },
            onDeleteCategory = { viewModel.deleteCategory(it) }
        )
    }

    // Settings Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            currentSettings = appSettings,
            onDismiss = { showSettingsDialog = false },
            onCurrencySelected = { viewModel.updateCurrency(it) },
            onSeparatorSelected = { viewModel.updateThousandsSeparator(it) },
            onToggleDarkTheme = { viewModel.updateDarkTheme(it) },
            onToggleShowDecimals = { viewModel.updateShowDecimals(it) },
            onToggleEnableCasa = { viewModel.updateEnableCasa(it) },
            onToggleEnableNegocio = { viewModel.updateEnableNegocio(it) },
            onSaveCasaDetails = { viewModel.updateCasaDetails(it) },
            onSaveNegocioDetails = { viewModel.updateNegocioDetails(it) },
            onOpenCategoryManager = {
                showSettingsDialog = false
                showCategoryManagerDialog = true
            }
        )
    }

    // Delete Confirmation Dialog
    if (paymentToDelete != null) {
        AlertDialog(
            onDismissRequest = { paymentToDelete = null },
            title = { Text("¿Eliminar recordatorio?") },
            text = { Text("Se eliminará '${paymentToDelete?.title}'. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePayment(paymentToDelete!!, context)
                        paymentToDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_payment")
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { paymentToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
