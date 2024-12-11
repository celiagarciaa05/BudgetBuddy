package com.example.budgetbuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle

@Composable
fun MenuScreen(
    onInicioClick: () -> Unit,
    onMiCuentaClick: () -> Unit,
    onMetasDeAhorroClick: () -> Unit,
    onGestionDeMetasClick: () -> Unit,
    onGastosClick: () -> Unit,
    onAhorrosClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color.White, Color(0xFFD4B3FF))
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Botón de Inicio
        EstiloBoton(
            text = "Inicio",
            onClick = onInicioClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de Mi Cuenta
        EstiloBoton(
            text = "Mi Cuenta",
            onClick = onMiCuentaClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de Metas de Ahorro
        EstiloBoton(
            text = "Metas De Ahorro",
            onClick = onMetasDeAhorroClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de Gestión de Metas
        EstiloBoton(
            text = "Gestión De Metas",
            onClick = onGestionDeMetasClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de Gastos
        EstiloBoton(
            text = "Gastos",
            onClick = onGastosClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de Ahorros
        EstiloBoton(
            text = "Ahorros",
            onClick = onAhorrosClick
        )
    }
}

@Composable
fun EstiloBoton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .padding(horizontal = 24.dp)
            .shadow(8.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6A1B9A),
            contentColor = Color.White
        )
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 18.sp,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.4f),
                    offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                    blurRadius = 4f
                )
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMenuScreen() {
    MenuScreen(
        onInicioClick = {},
        onMiCuentaClick = {},
        onMetasDeAhorroClick = {},
        onGestionDeMetasClick = {},
        onGastosClick = {},
        onAhorrosClick = {}
    )
}
