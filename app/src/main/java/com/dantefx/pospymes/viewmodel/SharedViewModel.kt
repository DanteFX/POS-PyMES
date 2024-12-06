package com.dantefx.pospymes.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dantefx.pospymes.model.Product
import kotlinx.coroutines.launch

class SharedViewModel(private val productViewModel: ProductViewModel) : ViewModel() {

    enum class Mode {
        INVENTORY, SALE
    }

    private val _currentMode = MutableLiveData(Mode.INVENTORY)
    val currentMode: LiveData<Mode> get() = _currentMode

    // Para observar el inventario desde la base de datos
    val inventory: LiveData<List<Product>> = productViewModel.allProducts

    private val _scannedProducts = MutableLiveData<MutableList<Product>>(mutableListOf())
    val scannedProducts: LiveData<MutableList<Product>> get() = _scannedProducts

    private val _totalPrice = MutableLiveData(0.0)
    val totalPrice: LiveData<Double> get() = _totalPrice

    init {
        // Llamada al método para inicializar productos de prueba
        initializeInventory()
        initializeSales()
    }

    // Método para agregar productos de prueba al inventario
    private fun initializeInventory() {
        viewModelScope.launch {
            val sampleProducts = listOf(
                Product(name = "Coca-Cola", price = 15.0, quantity = 50),
                Product(name = "Pepsi", price = 14.0, quantity = 30),
                Product(name = "Galletas Oreo", price = 25.0, quantity = 20)
            )
            sampleProducts.forEach { product ->
                productViewModel.insertProduct(product)
            }
        }
    }

    private fun initializeSales() {
        val sampleSaleProducts = listOf(
            Product(name = "Coca-Cola", price = 15.0, quantity = 1),
            Product(name = "Pepsi", price = 14.0, quantity = 2)
        )
        val updatedList = _scannedProducts.value ?: mutableListOf()
        updatedList.addAll(sampleSaleProducts)
        _scannedProducts.value = updatedList
        updateTotalPrice()
    }

    // Cambiar modo (Inventario o Venta)
    fun setMode(mode: Mode) {
        _currentMode.value = mode
        println("Mode changed to: $mode")
    }

    // Buscar un producto en el inventario (ahora desde la base de datos)
    fun findProductInInventory(name: String): Product? {
        return inventory.value?.find { it.name.equals(name, ignoreCase = true) }
    }

    // Agregar un producto al inventario (ahora a la base de datos)
    fun addProductToInventory(product: Product) {
        viewModelScope.launch {
            productViewModel.insertProduct(product)
        }
    }

    // Agregar un producto a la lista de ventas
    fun addProductToSale(product: Product) {
        val updatedList = _scannedProducts.value ?: mutableListOf()
        updatedList.add(product)
        _scannedProducts.value = updatedList
        updateTotalPrice()
    }

    // Remover un producto de la lista de ventas
    fun removeProductFromSale(product: Product) {
        val updatedList = _scannedProducts.value ?: mutableListOf()
        updatedList.remove(product)
        _scannedProducts.value = updatedList
        updateTotalPrice()
    }

    // Actualizar el precio total
    private fun updateTotalPrice() {
        _totalPrice.value = _scannedProducts.value?.sumOf { it.price } ?: 0.0
    }

    // Limpiar la lista de ventas
    fun clearSale() {
        _scannedProducts.value?.clear()
        updateTotalPrice()
    }

    // Actualizar un producto (por ejemplo, después de una venta)
    fun updateProductInInventory(product: Product) {
        viewModelScope.launch {
            productViewModel.updateProduct(product)

        }

    }

}
