package com.example.budgetbuddy.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.budgetbuddy.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AhorrosScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val ahorrosState = remember { mutableStateOf(emptyList<Triple<String, Double, String>>()) }

    // Obtener las transacciones del usuario desde Firestore
    LaunchedEffect(currentUser) {
        currentUser?.let {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(it.uid)
                .collection("transacciones")
                .whereEqualTo("tipo", "ahorro")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val ahorros = querySnapshot.documents.mapNotNull { doc ->
                        val categoria = doc.getString("categoria") ?: return@mapNotNull null
                        val cantidad = doc.getDouble("cantidad") ?: return@mapNotNull null
                        val descripcion = doc.getString("descripcion") ?: "Sin descripción"
                        Triple(categoria, cantidad, descripcion)
                    }
                    ahorrosState.value = ahorros
                }
        }
    }

    // UI de AhorrosScreen
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.White, Color(0xFFD4B3FF))
                    )
                )
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Tus Ahorros",
                fontSize = 24.sp,
                color = Color(0xFF6A1B9A),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (ahorrosState.value.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    ahorrosState.value.forEach { ahorro ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .shadow(4.dp, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = ahorro.first,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6A1B9A)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Cantidad: €${ahorro.second}",
                                    fontSize = 18.sp,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Descripción: ${ahorro.third}",
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "No hay ahorros disponibles.",
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            }
        }

        // Botón de Menú
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
                    .clickable {
                        navController.navigate("menu")
                    }
                    .padding(end = 0.dp, top = 30.dp)
            )
        }
    }
}
