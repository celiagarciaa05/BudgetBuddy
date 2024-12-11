
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.budgetbuddy.ui.screens.Categorias
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

data class Meta(
    val categoria: String,
    val tipo: String,
    val cantidad: Double,
    val fecha: String,
    val fechaInicio: String,
    var progreso: Double = 0.0,
    var estado: String = "en_proceso"
)

@Composable
fun InicioScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val transactionsState = remember { mutableStateOf(emptyList<Triple<String, Double, String>>()) }
    val metasState = remember { mutableStateOf(emptyList<Meta>()) }
    val showAddTransactionDialog = remember { mutableStateOf(false) }
    val moneyState = remember { mutableStateOf(0.0) }
    val context = LocalContext.current

    LaunchedEffect(currentUser) {
        currentUser?.let {
            val firestore = FirebaseFirestore.getInstance()
            //Cogemos el id actual del usuario y ejcutamos el bloque si todo va bien
            firestore.collection("users").document(it.uid).get().addOnSuccessListener { document ->
                moneyState.value = document.getDouble("money") ?: 0.0
            }
            firestore.collection("users").document(it.uid).collection("transacciones")
                //obtenemos todos los documentos dentro de transacciones
                .get().addOnSuccessListener { querySnapshot ->
                    //los procesa y los convierte en una lista
                    val transactions = querySnapshot.documents.mapNotNull { doc ->
                        //si algun campo es nulo se excluye esa transaccion en la lista
                        val categoria = doc.getString("categoria") ?: return@mapNotNull null
                        val cantidad = doc.getDouble("cantidad") ?: return@mapNotNull null
                        val tipo = doc.getString("tipo") ?: return@mapNotNull null
                        Triple(categoria, cantidad, tipo)
                    }
                    transactionsState.value = transactions
                }

            firestore.collection("users")
                .document(it.uid)
                .collection("metas")
                //Configura un Listener que es lo que hara que se muestren los cambios en tiempo real
                //snapshot recibe los datos actualizados y si no es nulo los actualiza
                .addSnapshotListener { snapshot, _ ->
                    snapshot?.let {
                        val metas = snapshot.documents.mapNotNull { doc ->
                            val categoria = doc.getString("categoria") ?: return@mapNotNull null
                            val tipo = doc.getString("tipo") ?: return@mapNotNull null
                            val cantidad = doc.getDouble("cantidad") ?: return@mapNotNull null
                            val fecha = doc.getString("fecha") ?: return@mapNotNull null

                            val fechaInicioStr = doc.get("fechaInicio")?.let {
                                when (it) {
                                    is String -> it
                                    is com.google.firebase.Timestamp -> SimpleDateFormat(
                                        "dd/MM/yyyy",
                                        Locale.getDefault()
                                    ).format(it.toDate())

                                    else -> "01/01/2000"
                                }
                            } ?: "01/01/2000"

                            val progreso = doc.getDouble("progreso") ?: 0.0
                            val estado = doc.getString("estado") ?: "en_proceso"
                            if (tipo == "ahorro" && estado == "en_proceso") {
                                Meta(categoria, tipo, cantidad, fecha, fechaInicioStr, progreso, estado)
                            } else {
                                null
                            }
                        }
                        metasState.value = metas
                    }
                }

        } ?: Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFFE8D9FF), Color(0xFFD4B3FF)),
                    radius = 1000f
                )
            )
            .padding(horizontal = 16.dp)
    ) {
        IconButton(
            onClick = { navController.navigate("menu") },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menú",
                tint = Color.Black,
                modifier = Modifier.size(40.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tu Resumen",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6A1B9A)
            )
            Text(
                text = "Saldo actualizado",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "€${moneyState.value}",
                fontSize = 50.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (transactionsState.value.isNotEmpty()) {
                BarChartComposable(transactionsState.value)
            } else {
                Text("No hay transacciones disponibles.", color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Metas",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6A1B9A),
                modifier = Modifier.padding(vertical = 8.dp)
            )


            if (metasState.value.isEmpty()) {
                Text("No hay metas configuradas.", fontSize = 16.sp, color = Color.Gray)
            } else {
                metasState.value.forEach { meta ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5E6FF)),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "${meta.cantidad}€ antes del ${meta.fecha} en ${meta.categoria}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = (meta.progreso / 100).toFloat(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp),
                                color = Color(0xFF6A1B9A)
                            )
                            Text(
                                text = "Progreso: ${meta.progreso.toInt()}%",
                                fontSize = 14.sp,
                                color = Color(0xFF6A1B9A)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {

            FloatingActionButton(
                onClick = { showAddTransactionDialog.value = true },
                containerColor = Color(0xFF6A1B9A),
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

        if (showAddTransactionDialog.value) {
            Dialog(onDismissRequest = { showAddTransactionDialog.value = false }) {
                Surface(
                    modifier = Modifier.padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    AgregarTransaccionContent(
                        moneyState = moneyState,
                        //funcion lambda para cerrar el dialogo
                        onDismiss = { showAddTransactionDialog.value = false }
                    )
                }
            }
        }
    }
}

@Composable
fun BarChartComposable(transactions: List<Triple<String, Double, String>>) {
    //categoria, cantidad, tipo
    val gastos = transactions.filter { it.third == "gasto" }
    val ahorros = transactions.filter { it.third == "ahorro" }
    val categorias = (gastos.map { it.first } + ahorros.map { it.first }).distinct()
    //Itera sobre las categorias y filtra las transacciones que pertenece a esa categoria
    val gastoEntries = categorias.mapIndexed { index, categoria ->
        //cojo el segundo parametro para sumar las cantidades
        BarEntry(index.toFloat(), gastos.filter { it.first == categoria }.map { it.second }.sum().toFloat())
    }
    val ahorroEntries = categorias.mapIndexed { index, categoria ->
        BarEntry(index.toFloat(), ahorros.filter { it.first == categoria }.map { it.second }.sum().toFloat())
    }

    //aqui se almacenan las entradas de gastos y ahorros
    val gastoDataSet = BarDataSet(gastoEntries, "Gastos").apply {
        color = android.graphics.Color.parseColor("#f2e8ff")
    }
    val ahorroDataSet = BarDataSet(ahorroEntries, "Ahorros").apply {
        color = android.graphics.Color.parseColor("#7B2CBF")
    }

    val totalGastos = gastoEntries.map { it.y }.sum()
    val totalAhorros = ahorroEntries.map { it.y }.sum()
    val barData = if (totalGastos >= totalAhorros) {
        BarData(gastoDataSet, ahorroDataSet)
    } else {
        BarData(ahorroDataSet, gastoDataSet)
    }.apply {
        barWidth = 0.3f
    }

    val barChart = BarChart(LocalContext.current).apply {
        data = barData
        setFitBars(true)
        xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(categorias)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            textSize = 12f
        }
        axisLeft.setDrawGridLines(false)
        axisRight.isEnabled = false
        description.isEnabled = false
        legend.apply {
            isEnabled = true
            textSize = 16f
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            orientation = Legend.LegendOrientation.HORIZONTAL
            xEntrySpace = 20f
        }
        extraBottomOffset = 16f // Separar las categorías del gráfico
        groupBars(0f, 0.2f, 0.05f) // Separar las barras dentro de un grupo
    }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AndroidView(
                factory = { barChart },
                modifier = Modifier
                    .width((categorias.size * 150).dp)
                    .height(400.dp)
            )
        }

    }
}

@Composable
fun AgregarCategoriaDialog(onDismiss: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var nuevaCategoria by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Agregar Categoría", fontSize = 20.sp, color = Color(0xFF6A1B9A))

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = nuevaCategoria,
            onValueChange = { nuevaCategoria = it },
            label = { Text("Nombre de la Categoría") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }

            Button(
                onClick = {
                    if (nuevaCategoria.isBlank()) {
                        Toast.makeText(context, "El nombre no puede estar vacío.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    currentUser?.let {
                        Categorias.agregarCategoria(it.uid, nuevaCategoria.trim(), {
                            Toast.makeText(context, "Categoría agregada con éxito.", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }, { error ->
                            Toast.makeText(context, "Error al agregar categoría: ${error.message}", Toast.LENGTH_SHORT).show()
                        })
                    }
                }
            ) {
                Text("Guardar")
            }
        }
    }
}

@Composable
fun AgregarTransaccionContent(moneyState: MutableState<Double>, onDismiss: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var tipoTransaccion by remember { mutableStateOf("gasto") }
    var cantidad by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    val categorias = remember { mutableStateListOf("CASA", "COCHE", "COMIDA", "MEDICO") }
    var descripcion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val showAddCategoryDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Cargar categorías dinámicas desde Firebase y combinar con las predeterminadas
    LaunchedEffect(currentUser) {
        currentUser?.let {
            Categorias.obtenerCategorias(it.uid, { categoriasList ->
                categorias.clear()
                categorias.addAll(listOf("CASA", "COCHE", "COMIDA", "MEDICO"))
                categorias.addAll(categoriasList)
            }, { error ->
                Toast.makeText(context, "Error al cargar categorías: ${error.message}", Toast.LENGTH_SHORT).show()
            })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Agregar Transacción", fontSize = 20.sp, color = Color(0xFF6A1B9A))

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            RadioButton(
                selected = tipoTransaccion == "gasto",
                onClick = { tipoTransaccion = "gasto" }
            )
            Text("Gasto")

            Spacer(modifier = Modifier.width(16.dp))

            RadioButton(
                selected = tipoTransaccion == "ahorro",
                onClick = { tipoTransaccion = "ahorro" }
            )
            Text("Ahorro")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = cantidad,
            onValueChange = {
                if (it.all { char -> char.isDigit() || char == '.' }) cantidad = it
            },
            label = { Text("Cantidad (€)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Dropdown para seleccionar categoría
        Box {
            TextField(
                value = categoria,
                onValueChange = {},
                label = { Text("Categoría") },
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                    }
                },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                categorias.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            categoria = item
                            expanded = false
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Agregar Categoría...") },
                    onClick = {
                        showAddCategoryDialog.value = true
                        expanded = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = fecha,
            onValueChange = { fecha = it },
            label = { Text("Fecha (dd/MM/yyyy)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (showAddCategoryDialog.value) {
            Dialog(onDismissRequest = { showAddCategoryDialog.value = false }) {
                Surface(
                    modifier = Modifier.padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    AgregarCategoriaDialog(onDismiss = {
                        showAddCategoryDialog.value = false
                        currentUser?.let {
                            Categorias.obtenerCategorias(it.uid, { categoriasList ->
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

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }

            Button(
                onClick = {
                    if (cantidad.isEmpty() || cantidad.toDoubleOrNull() == null || cantidad.toDouble() <= 0) {
                        Toast.makeText(context, "Por favor ingrese una cantidad válida.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (tipoTransaccion == "gasto" && moneyState.value - cantidad.toDouble() < 0) {
                        Toast.makeText(context, "No puedes realizar esta transacción. El saldo no puede ser negativo.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    guardarTransaccionYActualizarBalance(
                        currentUser?.uid,
                        tipoTransaccion,
                        cantidad,
                        categoria,
                        descripcion,
                        fecha,
                        moneyState,
                        context
                    )
                    onDismiss()
                }
            ) {
                Text("Guardar")
            }
        }
    }
}
private fun guardarTransaccionYActualizarBalance(
    userId: String?,
    tipo: String,
    cantidadStr: String,
    categoria: String,
    descripcion: String,
    fecha: String,
    moneyState: MutableState<Double>,
    context: Context
) {
    if (userId == null) return

    val db = FirebaseFirestore.getInstance()
    val cantidad = cantidadStr.toDoubleOrNull() ?: return

    val fechaValida = try {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fecha)
    } catch (e: Exception) {
        null
    }

    if (fechaValida == null) {
        Toast.makeText(
            context,
            "Por favor ingrese una fecha válida en formato dd/MM/yyyy.",
            Toast.LENGTH_SHORT
        ).show()
        return
    }

    db.collection("users").document(userId).get()
        .addOnSuccessListener { document ->
            if (document != null) {
                val moneyActual = document.getDouble("money") ?: 0.0
                val moneyTotal = if (tipo == "ahorro") moneyActual + cantidad else moneyActual - cantidad

                if (moneyTotal < 0) {
                    Toast.makeText(
                        context,
                        "No puedes realizar esta transacción. El saldo no puede ser negativo.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addOnSuccessListener
                }

                db.collection("users").document(userId).update("money", moneyTotal)
                    .addOnSuccessListener {
                        moneyState.value = moneyTotal
                        agregarTransaccionYActualizarMeta(userId, tipo, cantidadStr, categoria, descripcion, fecha, db)
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirestoreError", "Error al actualizar saldo: ${e.message}")
                    }
            }
        }
        .addOnFailureListener { e ->
            Log.e("FirestoreError", "Error al obtener el usuario: ${e.message}")
        }
}
private fun agregarTransaccionYActualizarMeta(
    userId: String?,
    tipo: String,
    cantidadStr: String,
    categoria: String,
    descripcion: String,
    fecha: String,
    db: FirebaseFirestore
) {
    if (userId == null) return

    val cantidad = cantidadStr.toDoubleOrNull() ?: return
    val fechaTransaccion = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fecha) ?: return

    // Agregar la transacción a Firestore
    val transaccion = hashMapOf(
        "tipo" to tipo,
        "cantidad" to cantidad,
        "categoria" to categoria,
        "descripcion" to descripcion,
        "fecha" to fecha
    )

    db.collection("users").document(userId).collection("transacciones").add(transaccion)
        .addOnSuccessListener {
            Log.d("Firestore", "Transacción agregada correctamente")
        }
        .addOnFailureListener { e ->
            Log.e("FirestoreError", "Error al agregar la transacción: ${e.message}")
        }

    // Si la transacción es de tipo "ahorro", actualizar metas
    if (tipo == "ahorro") {
        db.collection("users").document(userId).collection("metas")
            .whereEqualTo("tipo", "ahorro")
            .whereEqualTo("estado", "en_proceso")
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot?.let {
                    val metas = snapshot.documents

                    metas.forEach { doc ->
                        val metaCategoria = doc.getString("categoria")?.trim()?.lowercase(Locale.getDefault()) ?: return@forEach
                        val metaCantidad = doc.getDouble("cantidad") ?: return@forEach
                        val metaProgreso = doc.getDouble("progreso") ?: 0.0
                        val metaFechaInicioStr = doc.getString("fechaInicio") ?: return@forEach

                        val metaFechaInicio = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(metaFechaInicioStr)
                            ?: return@forEach

                        if (metaCategoria == categoria.trim().lowercase(Locale.getDefault()) &&
                            !fechaTransaccion.before(metaFechaInicio)
                        ) {
                            val nuevoProgreso = (metaProgreso + cantidad) / metaCantidad * 100
                            val progresoFinal = minOf(nuevoProgreso, 100.0)

                            val estadoFinal = if (progresoFinal >= 100.0) "completada" else "en_proceso"

                            db.collection("users").document(userId).collection("metas").document(doc.id)
                                .update(
                                    "progreso", progresoFinal,
                                    "estado", estadoFinal
                                )
                                .addOnSuccessListener {
                                    Log.d("Firestore", "Meta actualizada correctamente")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("FirestoreError", "Error al actualizar la meta: ${e.message}")
                                }
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error al obtener metas: ${e.message}")
            }
    }
}
