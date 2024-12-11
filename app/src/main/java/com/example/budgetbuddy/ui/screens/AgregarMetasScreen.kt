package com.example.budgetbuddy.ui.screens

import AgregarCategoriaDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AgregarMetasScreen(navController: NavController) {
    var categoria by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("gasto") }
    var cantidad by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val categorias = remember { mutableStateListOf("CASA", "COCHE", "COMIDA", "MEDICO") }
    val expanded = remember { mutableStateOf(false) }
    val showAddCategoryDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    LaunchedEffect(userId) {
        userId?.let {
            Categorias.obtenerCategorias(it, { categoriasList ->
                categorias.clear()
                categorias.addAll(listOf("CASA", "COCHE", "COMIDA", "MEDICO"))
                categorias.addAll(categoriasList)
            }, { error ->
                Toast.makeText(context, "Error al cargar categorías: ${error.message}", Toast.LENGTH_SHORT).show()
            })
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.White, Color(0xFFD4B3FF))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Agregar Nueva Meta",
                fontSize = 24.sp,
                color = Color(0xFF6A1B9A)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Box {
                TextField(
                    value = categoria,
                    onValueChange = {},
                    label = { Text("Categoría") },
                    trailingIcon = {
                        IconButton(onClick = { expanded.value = !expanded.value }) {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                        }
                    },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
                    categorias.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                categoria = item
                                expanded.value = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Agregar Categoría...") },
                        onClick = {
                            showAddCategoryDialog.value = true
                            expanded.value = false
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { tipo = "gasto" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (tipo == "gasto") Color(0xFF6A1B9A) else Color.Gray,
                        contentColor = Color.White
                    )
                ) {
                    Text("Gasto")
                }

                Button(
                    onClick = { tipo = "ahorro" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (tipo == "ahorro") Color(0xFF6A1B9A) else Color.Gray,
                        contentColor = Color.White
                    )
                ) {
                    Text("Ahorro")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (tipo == "gasto") {
                    if (cantidad.isNotBlank() && categoria.isNotBlank() && fecha.isNotBlank()) {
                        "Meta: No puedo gastar más de $cantidad€ en la categoría de $categoria antes del $fecha."
                    } else {
                        "Meta: Completa todos los campos para ver tu meta."
                    }
                } else {
                    if (cantidad.isNotBlank() && categoria.isNotBlank() && fecha.isNotBlank()) {
                        "Meta: Tengo que ahorrar $cantidad€ antes del $fecha en la categoría $categoria."
                    } else {
                        "Meta: Completa todos los campos para ver tu meta."
                    }
                },
                fontSize = 16.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            AniadirCategoria("Cantidad (€)", cantidad) { cantidad = it }
            Spacer(modifier = Modifier.height(8.dp))

            AniadirCategoria("Fecha (dd/MM/yyyy)", fecha) { fecha = it }
            Spacer(modifier = Modifier.height(16.dp))

            errorMessage.value?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Cancelar", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (validarCampos(categoria, tipo, cantidad, fecha, dateFormat, errorMessage)) {
                            guardarMeta(categoria, tipo, cantidad, fecha, navController)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6A1B9A),
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Guardar", fontSize = 16.sp)
                }
            }
        }
    }

    if (showAddCategoryDialog.value) {
        Dialog(onDismissRequest = { showAddCategoryDialog.value = false }) {
            Surface(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                AgregarCategoriaDialog(onDismiss = {
                    showAddCategoryDialog.value = false
                    userId?.let {
                        Categorias.obtenerCategorias(it, { categoriasList ->
                            categorias.clear()
                            categorias.addAll(listOf("CASA", "COCHE", "COMIDA", "MEDICO"))
                            categorias.addAll(categoriasList)
                        }, { error ->
                            Toast.makeText(context, "Error al actualizar categorías: ${error.message}", Toast.LENGTH_SHORT).show()
                        })
                    }
                })
            }
        }
    }
}


@Composable
fun AniadirCategoria(
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp))
            .padding(16.dp),
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            }
        }
    )
}

fun validarCampos(
    categoria: String,
    tipo: String,
    cantidadStr: String,
    fecha: String,
    dateFormat: SimpleDateFormat,
    errorMessage: MutableState<String?>
): Boolean {
    errorMessage.value = null

    if (categoria.isEmpty() || tipo.isEmpty() || cantidadStr.isEmpty() || fecha.isEmpty()) {
        errorMessage.value = "Por favor, completa todos los campos."
        return false
    }

    if (tipo.lowercase() !in listOf("ahorro", "gasto")) {
        errorMessage.value = "El tipo debe ser 'ahorro' o 'gasto'."
        return false
    }

    val cantidad = cantidadStr.toDoubleOrNull()
    if (cantidad == null || cantidad <= 0) {
        errorMessage.value = "La cantidad debe ser un número mayor a 0."
        return false
    }

    try {
        dateFormat.parse(fecha)
    } catch (e: Exception) {
        errorMessage.value = "La fecha debe estar en el formato dd/MM/yyyy."
        return false
    }

    return true
}
fun guardarMeta(
    categoria: String,
    tipo: String,
    cantidadStr: String,
    fecha: String,
    navController: NavController
) {
    val cantidad = cantidadStr.toDoubleOrNull() ?: return
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val currentDate = Calendar.getInstance().time
    val meta = hashMapOf(
        "categoria" to categoria,
        "tipo" to tipo,
        "cantidad" to cantidad,
        "fecha" to fecha,
        "fechaInicio" to SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(currentDate),
        "progreso" to 0.0
    )

    db.collection("users").document(userId).collection("metas").add(meta)
        .addOnSuccessListener {
            navController.popBackStack()
        }
        .addOnFailureListener {
        }
}
