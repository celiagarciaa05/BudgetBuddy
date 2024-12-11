package com.example.budgetbuddy

import InicioScreen
import SplashScreen
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.budgetbuddy.ui.screens.*
import com.example.budgetbuddy.ui.theme.BudgetBuddyTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BudgetBuddyTheme {
                val navController = rememberNavController()

                // Configuración del NavHost
                NavHost(navController = navController, startDestination = "splash") {
                    // Splash Screen
                    composable("splash") {
                        SplashScreen {
                            val currentUser = auth.currentUser
                            if (currentUser != null) {
                                navController.navigate("login") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            } else {
                                navController.navigate("login") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        }
                    }

                    // Pantalla de Inicio
                    composable("inicio") {
                        InicioScreen(navController = navController)
                    }

                    // Pantalla de Login
                    composable("login") {
                        LoginScreen(
                            onLogin = { email, password -> loginUser(email, password, navController) },
                            onSignUpClick = { navController.navigate("register") },
                            onGoogleSignInClick = { onGoogleSignInClick() }
                        )
                    }

                    // Pantalla de Registro
                    composable("register") {
                        RegisterScreen(
                            onRegister = { username, email, password, confirmPassword ->
                                registerUser(username, email, password, confirmPassword, navController)
                            },
                            onLoginClick = { navController.navigate("login") },
                            onGoogleSignInClick = { onGoogleSignInClick() }
                        )
                    }

                    // Menú Principal
                    composable("menu") {
                        MenuScreen(
                            onInicioClick = { navController.navigate("inicio") },
                            onMiCuentaClick = { navController.navigate("mi_cuenta_screen") },
                            onMetasDeAhorroClick = { navController.navigate("metas_screen") },
                            onGestionDeMetasClick = { navController.navigate("gestion_metas_screen") },
                            onGastosClick = { navController.navigate("gastos_screen") },
                            onAhorrosClick = { navController.navigate("ahorros_screen") }
                        )
                    }

                    // Rutas adicionales
                    composable("metas_screen") { MetasScreen(navController = navController) }
                    composable("gestion_metas_screen") { GestionMetasScreen(navController = navController) }
                    composable("gastos_screen") { GastosScreen(navController = navController) }
                    composable("ahorros_screen") { AhorrosScreen(navController = navController) }
                    composable("agregar_metas_screen") { AgregarMetasScreen(navController = navController) }
                    composable("mi_cuenta_screen") { MiCuentaScreen(navController = navController) }
                }
            }
        }
    }

    // Función para manejar Google Sign-In
    private fun onGoogleSignInClick() {
        Toast.makeText(this, "Función de Google Sign-In no implementada.", Toast.LENGTH_SHORT).show()
    }

    // Función para iniciar sesión con Firebase
    private fun loginUser(email: String, password: String, navController: NavController) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    navController.navigate("inicio") {
                        popUpTo("login") { inclusive = true }
                    }
                } else {
                    val errorMessage = task.exception?.localizedMessage ?: "Error desconocido"
                    Toast.makeText(this, "Error de inicio de sesión: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Función para registrar un usuario con Firebase
    private fun registerUser(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        navController: NavController
    ) {
        if (password != confirmPassword) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userData = hashMapOf(
                        "username" to username,
                        "email" to email,
                        "money" to 0.0 // Valor inicial para el balance
                    )

                    user?.uid?.let { uid ->
                        val db = FirebaseFirestore.getInstance()
                        val userRef = db.collection("users").document(uid)

                        // Guardar datos del usuario
                        userRef.set(userData)
                            .addOnSuccessListener {
                                // Inicializar categorías predeterminadas
                                inicializarCategorias(uid) { success ->
                                    if (success) {
                                        Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()
                                        navController.navigate("inicio") {
                                            popUpTo("register") { inclusive = true }
                                        }
                                    } else {
                                        Toast.makeText(this, "Error al inicializar categorías", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al guardar los datos: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                    } ?: run {
                        Toast.makeText(this, "No se pudo obtener el UID del usuario", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorMessage = task.exception?.localizedMessage ?: "Error desconocido"
                    Toast.makeText(this, "Error al registrar el usuario: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun inicializarCategorias(userId: String, onComplete: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val categoriasRef = db.collection("users").document(userId).collection("categorias")

        val categoriasPorDefecto = listOf("CASA", "COCHE", "COMIDA", "MEDICO")
        val batch = db.batch()

        categoriasPorDefecto.forEach { categoria ->
            val docRef = categoriasRef.document(categoria)
            batch.set(docRef, mapOf("nombre" to categoria))
        }

        batch.commit()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

}
