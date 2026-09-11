package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.CategoryItem

@Composable
fun CategoryManagerDialog(
    categories: List<CategoryItem>,
    onDismiss: () -> Unit,
    onAddCategory: (String) -> Unit,
    onToggleCategoryActive: (CategoryItem, Boolean) -> Unit,
    onDeleteCategory: (CategoryItem) -> Unit
) {
    val context = LocalContext.current
    var newCategoryName by remember { mutableStateOf("") }
    var categoryToDelete by remember { mutableStateOf<CategoryItem?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFCAC4D0)),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                            color = Color(0xFFEADDFF),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = Color(0xFF21005D),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Categorías de Pago",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1B1F)
                            )
                            Text(
                                text = "Activar, desactivar o borrar",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF79747E)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_category_manager")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFF49454F)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Input to add new category
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Nueva categoría") },
                        placeholder = { Text("Ej: Seguros, Colegiatura") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_category_input")
                    )

                    Button(
                        onClick = {
                            val trimmed = newCategoryName.trim()
                            if (trimmed.isNotBlank()) {
                                val alreadyExists = categories.any { it.name.trim().equals(trimmed, ignoreCase = true) }
                                if (alreadyExists) {
                                    Toast.makeText(context, "La categoría '$trimmed' ya existe", Toast.LENGTH_SHORT).show()
                                } else {
                                    onAddCategory(trimmed)
                                    Toast.makeText(context, "Categoría '$trimmed' agregada", Toast.LENGTH_SHORT).show()
                                    newCategoryName = ""
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                        modifier = Modifier
                            .height(56.dp)
                            .testTag("add_category_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LISTADO DE CATEGORÍAS (${categories.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF49454F)
                    )
                    Text(
                        text = "${categories.count { it.isActive }} activas",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF6750A4),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories, key = { it.id }) { cat ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (cat.isActive) Color(0xFFF7F2FA) else Color(0xFFECE6F0).copy(alpha = 0.6f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (cat.isActive) Color(0xFFEADDFF) else Color(0xFFCAC4D0).copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = cat.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (cat.isActive) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (cat.isActive) Color(0xFF1C1B1F) else Color(0xFF79747E)
                                        )
                                        if (cat.isDefault) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFFEADDFF).copy(alpha = 0.6f)
                                            ) {
                                                Text(
                                                    text = "Defecto",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 9.sp,
                                                    color = Color(0xFF21005D),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = if (cat.isActive) "Disponible para recordatorios" else "Desactivada (oculta)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp,
                                        color = if (cat.isActive) Color(0xFF388E3C) else Color(0xFFB3261E)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Activar / Desactivar Switch
                                    Switch(
                                        checked = cat.isActive,
                                        onCheckedChange = { isChecked ->
                                            onToggleCategoryActive(cat, isChecked)
                                            val stateText = if (isChecked) "activada" else "desactivada"
                                            Toast.makeText(context, "${cat.name} $stateText", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF6750A4),
                                            uncheckedThumbColor = Color(0xFF79747E),
                                            uncheckedTrackColor = Color(0xFFE7E0EC)
                                        ),
                                        modifier = Modifier.testTag("toggle_category_${cat.id}")
                                    )

                                    // Borrar Button
                                    IconButton(
                                        onClick = {
                                            categoryToDelete = cat
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("delete_category_${cat.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Borrar ${cat.name}",
                                            tint = Color(0xFFB3261E),
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Listo", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Confirmation dialog before deleting a category
    categoryToDelete?.let { cat ->
        Dialog(onDismissRequest = { categoryToDelete = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "¿Borrar categoría?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1F)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Se eliminará la categoría '${cat.name}'. Los pagos existentes no se borrarán.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF49454F)
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { categoryToDelete = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE7E0EC)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cancelar", color = Color(0xFF1C1B1F))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onDeleteCategory(cat)
                                categoryToDelete = null
                                Toast.makeText(context, "Categoría borrada", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Borrar", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
