package com.example.budgetbuddy.ui.screens

import com.google.firebase.firestore.FirebaseFirestore

object Categorias {
    private const val DEFAULT_CATEGORIES = "default_categories"
    private val defaultCategories = listOf("CASA", "COCHE", "COMIDA", "MEDICO")

    fun inicializarCategorias(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val categoriesRef = db.collection("users").document(userId).collection("categorias")

        defaultCategories.forEach { categoria ->
            categoriesRef.document(categoria).set(mapOf("nombre" to categoria))
        }
    }

    fun agregarCategoria(userId: String, nuevaCategoria: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val categoriesRef = db.collection("users").document(userId).collection("categorias")

        categoriesRef.document(nuevaCategoria).set(mapOf("nombre" to nuevaCategoria))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun obtenerCategorias(userId: String, onSuccess: (List<String>) -> Unit, onError: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val categoriesRef = db.collection("users").document(userId).collection("categorias")

        categoriesRef.get()
            .addOnSuccessListener { snapshot ->
                val categorias = snapshot.documents.mapNotNull { it.getString("nombre") }
                onSuccess(categorias)
            }
            .addOnFailureListener { onError(it) }
    }
}
