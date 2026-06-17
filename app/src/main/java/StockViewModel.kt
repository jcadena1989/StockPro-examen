package com.example.stockpro

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class StockViewModel : ViewModel() {
    // creamos 6 productos para mostrar como funciona
    val inventario = mutableStateListOf(
        Producto(1, "Martillo", "Herramienta manual", 15.50, 10),
        Producto(2, "Clavos", "Caja de 100 unidades", 3.00, 2), // Stock crítico
        Producto(3, "Taladro", "Eléctrico 500W", 45.00, 0), // Stock en cero
        Producto(4, "Destornillador", "Punta estrella", 5.00, 8),
        Producto(5, "Alicate", "Corte diagonal", 8.50, 4), // Stock crítico
        Producto(6, "Cinta Métrica", "5 metros", 4.00, 15)
    )

    fun obtenerProducto(id: Int): Producto? = inventario.find { it.id == id } //

    fun actualizarStock(id: Int, nuevaCantidad: Int) { //
        val index = inventario.indexOfFirst { it.id == id }
        if (index != -1 && nuevaCantidad >= 0) {
            // Para que Compose detecte el cambio en listas, a veces es necesario reasignar el objeto
            inventario[index] = inventario[index].copy(stockActual = nuevaCantidad)
        }
    }

    fun calcularValorTotalInventario(): Double { //
        return inventario.sumOf { it.precio * it.stockActual }
    }

    fun obtenerProductosEnRiesgo(): List<Producto> { //
        return inventario.filter { it.stockActual < 5 }
    }

    fun contarProductosEnCero(): Int {
        return inventario.count { it.stockActual == 0 }
    }
}