package com.example.budgetbuddy.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun MiCuentaScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val username = remember { mutableStateOf("") }
    val email = currentUser?.email ?: ""
    val newPassword = remember { mutableStateOf("") }
    val currentPassword = remember { mutableStateOf("") }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            FirebaseFirestore.getInstance()
                .collection("users").document(it.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        username.value = document.getString("username") ?: ""
                    }
                }
                .addOnFailureListener { e ->
                }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.White, Color(0xFFD4B3FF))
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Bienvenido/a ${username.value}!",
                fontSize = 28.sp,
                color = Color(0xFF6A1B9A),
                modifier = Modifier.padding(top = 32.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = username.value,
                onValueChange = { username.value = it },
                label = { Text(text = "Nombre de usuario") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(Color(0xFFF1E6FF), shape = RoundedCornerShape(8.dp))
            )

            OutlinedTextField(
                value = email,
                onValueChange = {},
                label = { Text(text = "Correo electrónico") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(Color(0xFFF1E6FF), shape = RoundedCornerShape(8.dp)),
                enabled = false
            )

            OutlinedTextField(
                value = newPassword.value,
                onValueChange = { newPassword.value = it },
                label = { Text(text = "Nueva Contraseña") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(Color(0xFFF1E6FF), shape = RoundedCornerShape(8.dp))
            )

            OutlinedTextField(
                value = currentPassword.value,
                onValueChange = { currentPassword.value = it },
                label = { Text(text = "Contraseña actual") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(Color(0xFFF1E6FF), shape = RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { guardarCambios(username.value, newPassword.value, currentPassword.value) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6A1B9A),
                    contentColor = Color.White
                )
            ) {
                Text(text = "Guardar Cambios", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { eliminarCuenta(navController) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Eliminar Cuenta", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    auth.signOut()
                    navController.navigate("login") {
                        popUpTo("mi_cuenta_screen") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6A1B9A),
                    contentColor = Color.White
                )
            ) {
                Text(text = "Cerrar Sesión", fontSize = 16.sp)
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
                        translationX = 200f
                    }
                    .clickable { navController.navigate("menu") }
                    .padding(end = 0.dp)
                    .padding(top = 30.dp)
            )
        }
    }
}

private fun guardarCambios(username: String, newPassword: String, currentPassword: String) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    currentUser?.let { user ->
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(user.uid).update("username", username)
            .addOnSuccessListener {
            }
            .addOnFailureListener { e ->
            }

        if (newPassword.isNotEmpty() && currentPassword.isNotEmpty()) {
            user.updatePassword(newPassword)
                .addOnSuccessListener {
                }
                .addOnFailureListener { e ->
                }
        }
    }
}

private fun eliminarCuenta(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    currentUser?.let { user ->
        user.delete()
            .addOnSuccessListener {
                FirebaseFirestore.getInstance().collection("users").document(user.uid).delete()
                    .addOnSuccessListener {
                        navController.navigate("login") {
                            popUpTo("mi_cuenta_screen") { inclusive = true }
                        }
                    }
                    .addOnFailureListener { e ->
                    }
            }
            .addOnFailureListener { e ->
            }
    }
}
