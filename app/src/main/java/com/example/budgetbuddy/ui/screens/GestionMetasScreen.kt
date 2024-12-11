package com.example.budgetbuddy.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun GestionMetasScreen(navController: NavController) {
    val metasCompletadas = remember { mutableStateListOf<Meta>() }
    val metasExpiradas = remember { mutableStateListOf<Meta>() }
    var selectedTabIndex by remember { mutableStateOf(0) }

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    LaunchedEffect(currentUser) {
        currentUser?.let {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(it.uid)
                .collection("metas")
                .get()
                .addOnSuccessListener { result ->
                    procesarMetas(result, metasCompletadas, metasExpiradas)
                }
                .addOnFailureListener {
                }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.White, Color(0xFFD4B3FF))
                    )
                )
        ) {
            // Tab para cambiar entre completadas y expiradas
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 }
                ) {
                    Text("Completadas", modifier = Modifier.padding(16.dp))
                }
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 }
                ) {
                    Text("Expiradas", modifier = Modifier.padding(16.dp))
                }
            }

            // Lista de metas según la pestaña seleccionada
            when (selectedTabIndex) {
                0 -> MetasList(metas = metasCompletadas)
                1 -> MetasList(metas = metasExpiradas)
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
                        translationX = 150f
                    }
                    .clickable { navController.navigate("menu") }
                    .padding(end = 0.dp, top = 30.dp)
            )
        }
    }
}

@Composable
fun MetasList(metas: List<Meta>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(metas) { meta ->
            MetaItem(meta)
        }
    }
}

@Composable
fun MetaItem(meta: Meta) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD1C4E9))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Categoría: ${meta.categoria}", fontSize = 18.sp, color = Color.Black)
            Text(text = "Tipo: ${meta.tipo}", fontSize = 16.sp, color = Color.Gray)
            Text(text = "Cantidad: €${meta.cantidad}", fontSize = 16.sp, color = Color.Black)
            Text(text = "Fecha: ${meta.fecha}", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

fun procesarMetas(
    result: QuerySnapshot,
    metasCompletadas: MutableList<Meta>,
    metasExpiradas: MutableList<Meta>
) {
    val currentDate = Calendar.getInstance().time
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    metasCompletadas.clear()
    metasExpiradas.clear()

    for (document in result) {
        val estado = document.getString("estado") ?: "desconocido"
        val categoria = document.getString("categoria") ?: "Sin Categoría"
        val tipo = document.getString("tipo") ?: "desconocido"
        val cantidad = document.getDouble("cantidad") ?: 0.0
        val fechaStr = document.getString("fecha") ?: "01/01/2000"
        val fecha = try {
            dateFormat.parse(fechaStr)
        } catch (e: Exception) {
            null
        }

        val meta = Meta(categoria, tipo, cantidad, fechaStr, estado)

        when (estado) {
            "completada" -> metasCompletadas.add(meta)
            "expirada" -> metasExpiradas.add(meta)
            else -> {
                // Si la fecha está vencida, marcar como expirada
                if (fecha != null && fecha.before(currentDate)) {
                    metasExpiradas.add(meta)
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
                        .collection("metas")
                        .document(document.id)
                        .update("estado", "expirada")
                }
            }
        }
    }
}

data class Meta(
    val categoria: String,
    val tipo: String,
    val cantidad: Double,
    val fecha: String,
    val estado: String
)
