package com.example.budgetbuddy.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.budgetbuddy.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MetasScreen(navController: NavController) {
    val metasEnProceso = remember { mutableStateListOf<MutableMap<String, Any>>() }
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    LaunchedEffect(currentUser) {
        currentUser?.let {
            FirebaseFirestore.getInstance()
                .collection("users").document(it.uid).collection("metas")
                .get()
                .addOnSuccessListener { result ->
                    procesarMetas(result, metasEnProceso, it.uid)
                }
                .addOnFailureListener {
                }
        }
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.White, Color(0xFFD4B3FF))
                )
            )
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 100.dp)
        ) {
            Text(
                text = "Mis Metas",
                fontSize = 28.sp,
                color = Color(0xFF6A1B9A),
                modifier = Modifier.padding(top = 32.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            metasEnProceso.forEach { meta ->
                val tipo = meta["tipo"]?.toString() ?: "Desconocido"
                val fecha = meta["fecha"]?.toString() ?: "Sin fecha"
                val cantidad = meta["cantidad"]?.toString() ?: "0.0"
                val categoria = meta["categoria"]?.toString() ?: "Desconocida"
                val textoFecha = if (tipo == "ahorro") "Antes de: $fecha" else "Hasta: $fecha"

                var expanded by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .shadow(8.dp, shape = RoundedCornerShape(16.dp))
                        .background(Color(0xFFf2dcfb), shape = RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Categoría: $categoria", fontSize = 18.sp)
                                Text(text = "Tipo: ${tipo.capitalize()}", fontSize = 18.sp)
                                Text(text = "Cantidad: $cantidad €", fontSize = 18.sp)
                                Text(text = textoFecha, fontSize = 18.sp)
                            }

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { expanded = true },
                                contentAlignment = Alignment.TopEnd
                            ) {
                                Text(text = "⋮", fontSize = 20.sp, color = Color.DarkGray)
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Eliminar") },
                                    onClick = {
                                        currentUser?.let {
                                            eliminarMeta(meta["id"].toString(), metasEnProceso, meta, it.uid)
                                        }
                                        expanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Marcar como completada") },
                                    onClick = {
                                        currentUser?.let {
                                            completarMeta(meta["id"].toString(), metasEnProceso, meta, it.uid)
                                        }
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.TopEnd
        ) {
            Image(
                bitmap = ImageBitmap.imageResource(id = R.drawable.iconmenu),
                contentDescription = "Ir al Menú",
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        clip = true
                        shape = RectangleShape
                        translationX = 220f
                    }
                    .clickable { navController.navigate("menu") }
                    .padding(end = 0.dp)
                    .padding(top = 30.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Image(
                bitmap = ImageBitmap.imageResource(id = R.drawable.botonmas),
                contentDescription = "Agregar Meta",
                modifier = Modifier
                    .size(150.dp)
                    .clickable { navController.navigate("agregar_metas_screen") }
                    .padding(bottom = 30.dp)
                    .graphicsLayer {
                        clip = true
                        shape = RectangleShape
                        translationY = 120f
                    }
            )
        }
    }
}

private fun procesarMetas(
    result: QuerySnapshot,
    metasEnProceso: MutableList<MutableMap<String, Any>>,
    userId: String
) {
    val db = FirebaseFirestore.getInstance()
    val currentDate = Calendar.getInstance().time
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    metasEnProceso.clear()
    for (document in result) {
        val meta = document.data.toMutableMap()
        val fechaStr = meta["fecha"]?.toString()
        val fecha = try {
            fechaStr?.let { dateFormat.parse(it) }
        } catch (e: Exception) {
            null
        }
        val estado = when {
            meta["estado"] == "completada" -> "completada"
            fecha != null && fecha.before(currentDate) -> "expirada"
            else -> "en_proceso"
        }

        db.collection("users").document(userId).collection("metas")
            .document(document.id).update("estado", estado)

        if (!meta.containsKey("fechaInicio")) {
            val fechaInicio = dateFormat.format(currentDate)
            db.collection("users").document(userId).collection("metas")
                .document(document.id).update("fechaInicio", fechaInicio)
            meta["fechaInicio"] = fechaInicio
        }

        meta["id"] = document.id

        if (estado == "en_proceso") {
            metasEnProceso.add(meta)
        }
    }
}

private fun eliminarMeta(
    metaId: String,
    metasEnProceso: MutableList<MutableMap<String, Any>>,
    meta: MutableMap<String, Any>,
    userId: String
) {
    FirebaseFirestore.getInstance()
        .collection("users").document(userId)
        .collection("metas").document(metaId).delete()
    metasEnProceso.remove(meta)
}

private fun completarMeta(
    metaId: String,
    metasEnProceso: MutableList<MutableMap<String, Any>>,
    meta: MutableMap<String, Any>,
    userId: String
) {
    FirebaseFirestore.getInstance()
        .collection("users").document(userId)
        .collection("metas").document(metaId)
        .update("estado", "completada")
        .addOnSuccessListener {
            metasEnProceso.remove(meta)
        }
}
