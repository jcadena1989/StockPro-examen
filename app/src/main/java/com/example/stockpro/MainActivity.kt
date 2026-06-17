package com.example.stockpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stockpro.ui.theme.StockProTheme //siempre debemos verificar concordancia en nombres

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StockProTheme {
                // vamos con la navegación principal
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavegacionApp()
                }
            }
        }
    }
}


// CONFIGURACION DE NAVEGACION

@Composable
fun NavegacionApp() {
    val navController = rememberNavController()
    val viewModel: StockViewModel = viewModel() // instanciamos ViewModel compartido

    NavHost(navController = navController, startDestination = "pantalla1_login") {
        composable("pantalla1_login") {
            PantallaLogin(navController)
        }
        composable("pantalla2_catalogo/{nombreOperario}") { backStackEntry ->
            // Recibimos el nombre del operario [cite: 40]
            val nombre = backStackEntry.arguments?.getString("nombreOperario") ?: ""
            PantallaCatalogo(navController, viewModel, nombre)
        }
        composable("pantalla3_edicion/{productoId}") { backStackEntry ->
            // Recibimos el ID del producto
            val id = backStackEntry.arguments?.getString("productoId")?.toIntOrNull() ?: 0
            PantallaEdicion(navController, viewModel, id)
        }
        composable("pantalla4_reporte") {
            PantallaReporte(navController, viewModel)
        }
    }
}


// PANTALLA 1: DE LOGIN

@Composable
fun PantallaLogin(navController: NavController) {
    var nombre by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Bienvenido a StockPro", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del Operario") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            // navegamos pasando el nombre como parametro
            onClick = { navController.navigate("pantalla2_catalogo/$nombre") },
            enabled = nombre.length >= 3 // Solo habilitado si tiene 3 o más caracteres [cite: 39]
        ) {
            Text("Ingresar al Sistema") // [cite: 38]
        }
    }
}

// ==========================================
// PANTALLA 2: CATÁLOGO DE INVENTARIO [cite: 41]
// ==========================================
@Composable
fun PantallaCatalogo(navController: NavController, viewModel: StockViewModel, nombreOperario: String) {
    var verSoloCritico by remember { mutableStateOf(false) }

    // Obtenemos la lista dependiendo del filtro seleccionado
    val productos = if (verSoloCritico) {
        viewModel.obtenerProductosEnRiesgo() // [cite: 43]
    } else {
        viewModel.inventario
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("pantalla4_reporte") }) { // [cite: 50]
                Text("Reporte")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp).fillMaxSize()) {

            Text("Operario: $nombreOperario", fontSize = 20.sp, fontWeight = FontWeight.SemiBold) // [cite: 42]
            Spacer(modifier = Modifier.height(16.dp))

            // Botones de filtro [cite: 43]
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { verSoloCritico = false }) { Text("Ver Todo") }
                Button(onClick = { verSoloCritico = true }) { Text("Stock Crítico") }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Lista de productos [cite: 44]
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(productos) { producto ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { navController.navigate("pantalla3_edicion/${producto.id}") }, // Navegar al presionar [cite: 48, 49]
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Producto: ${producto.nombre}", fontWeight = FontWeight.Bold) // [cite: 45]
                            Text("Precio: $${producto.precio}") // [cite: 46]

                            // Validar color del stock [cite: 47]
                            val colorStock = if (producto.stockActual < 5) Color.Red else Color.Black
                            Text("Stock: ${producto.stockActual}", color = colorStock, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// PANTALLA 3: EDICIÓN DE STOCK [cite: 51]
// ==========================================
@Composable
fun PantallaEdicion(navController: NavController, viewModel: StockViewModel, id: Int) {
    val producto = viewModel.obtenerProducto(id)

    if (producto != null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(producto.nombre, fontSize = 28.sp, fontWeight = FontWeight.Bold) // [cite: 52]
            Text(producto.descripcion, fontSize = 16.sp) // [cite: 52]
            Spacer(modifier = Modifier.height(32.dp))

            Text("Stock Actual", fontSize = 20.sp)
            Text("${producto.stockActual}", fontSize = 60.sp, fontWeight = FontWeight.Black) // Texto grande [cite: 52]
            Spacer(modifier = Modifier.height(32.dp))

            // Botones de + y - [cite: 53]
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Button(
                    onClick = { viewModel.actualizarStock(id, producto.stockActual - 1) },
                    enabled = producto.stockActual > 0 // Deshabilitar si es 0 [cite: 54]
                ) {
                    Text("- 1", fontSize = 24.sp)
                }

                Button(
                    onClick = { viewModel.actualizarStock(id, producto.stockActual + 1) } // [cite: 55]
                ) {
                    Text("+ 1", fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
            Button(onClick = { navController.popBackStack() }) { // Regresar [cite: 56]
                Text("Guardar y Volver")
            }
        }
    } else {
        // En caso de que no encuentre el ID (por seguridad)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Producto no encontrado")
        }
    }
}

// ==========================================
// PANTALLA 4: REPORTE FINANCIERO [cite: 58]
// ==========================================
@Composable
fun PantallaReporte(navController: NavController, viewModel: StockViewModel) {
    // Le pedimos al ViewModel que haga los cálculos [cite: 59]
    val totalInvertido = viewModel.calcularValorTotalInventario()
    val productosEnCero = viewModel.contarProductosEnCero()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Capital Invertido Total", fontSize = 24.sp, fontWeight = FontWeight.Bold) // [cite: 61]
        Text("$$totalInvertido", fontSize = 48.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black) // [cite: 61]

        Spacer(modifier = Modifier.height(48.dp))

        Text("Total de productos con stock en cero:", fontSize = 18.sp) // [cite: 62]
        Text("$productosEnCero", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Red)

        Spacer(modifier = Modifier.height(48.dp))

        Button(onClick = { navController.popBackStack() }) { // [cite: 63]
            Text("Volver al Catálogo")
        }
    }
}