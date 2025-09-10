package com.niteshray.xapps.billingpro.features.ProductManagement.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.niteshray.xapps.billingpro.data.database.BillingProDatabase
import com.niteshray.xapps.billingpro.data.entity.Product
import com.niteshray.xapps.billingpro.data.model.User
import com.niteshray.xapps.billingpro.features.ProductManagement.domain.ProductRepository
import com.niteshray.xapps.billingpro.features.profile.domain.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val database = BillingProDatabase.Companion.getDatabase(application)
    private val repository = ProductRepository(database.productDao())
    private val userRepository = UserRepository()
    private val firebaseAuth = FirebaseAuth.getInstance()

    // Simple variables instead of complex UiState
    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user get() = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadUserInfo()
    }

    // Filtered products based on search query
    val products: StateFlow<List<Product>> = combine(
        _allProducts,
        _searchQuery
    ) { allProducts, query ->
        if (query.isBlank()) {
            allProducts
        } else {
            allProducts.filter { product ->
                product.name.contains(query, ignoreCase = true) ||
                        product.productId.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun loadUserInfo(){
        val userId = currentUserId ?: return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = userRepository.getUser(userId)
                result.fold(
                    onSuccess = { user ->
                        _user.value = user
                    },
                    onFailure = { exception ->
                        _errorMessage.value = "Failed to load user info: ${exception.message}"
                        // Set default user with isUnlocked = true if loading fails
                        _user.value = User(userId = userId, unlocked = true)
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Error loading user info: ${e.message}"
                // Set default user with isUnlocked = true if loading fails
                _user.value = User(userId = userId, unlocked = true)
            }
            _isLoading.value = false

        }
    }



    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _lowStockProducts = MutableStateFlow<List<Product>>(emptyList())
    val lowStockProducts: StateFlow<List<Product>> = _lowStockProducts.asStateFlow()

    private val _totalInventoryValue = MutableStateFlow(0.0)
    val totalInventoryValue: StateFlow<Double> = _totalInventoryValue.asStateFlow()

    private val _productCount = MutableStateFlow(0)
    val productCount: StateFlow<Int> = _productCount.asStateFlow()

    // Get current user ID
    private val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    init {
        // Only load data if user is authenticated
        if (currentUserId != null) {
            loadUserInfo()
            loadProducts()
            loadLowStockProducts()
            loadInventoryStats()
        }
    }

    // Load all products for current user
    private fun loadProducts() {
        val userId = currentUserId ?: return

        _isLoading.value = true
        repository.getAllProductsForUser(userId)
            .onEach { productList ->
                _allProducts.value = productList
                _isLoading.value = false
            }
            .catch { e ->
                _errorMessage.value =  e.message
                _isLoading.value = false
            }
            .launchIn(viewModelScope)
    }

    // Load low stock products
    private fun loadLowStockProducts() {
        val userId = currentUserId ?: return

        repository.getLowStockProductsForUser(userId, 10)
            .onEach { lowStockList ->
                _lowStockProducts.value = lowStockList
            }
            .launchIn(viewModelScope)
    }

    // Load inventory statistics
    private fun loadInventoryStats() {
        val userId = currentUserId ?: return

        viewModelScope.launch {
            try {
                val count = repository.getProductCountForUser(userId)
                val value = repository.getTotalInventoryValueForUser(userId)

                _productCount.value = count
                _totalInventoryValue.value = value
            } catch (e: Exception) {
                _errorMessage.value = "Stats load nahi ho sake: ${e.message}"
            }
        }
    }

    // Add new product with custom or generated productId
    fun addProduct(name: String, price: Double, quantity: Int, customProductId: String? = null) {
        val userId = currentUserId
        if (userId == null) {
            _errorMessage.value = "User is unauthenticated "
            return
        }

        viewModelScope.launch {
            try {
                val productId = customProductId?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()

                val newProduct = Product(
                    productId = productId,
                    userId = userId,
                    name = name,
                    price = price,
                    quantity = quantity
                )

                repository.insertProduct(newProduct)
                _errorMessage.value = null
                loadInventoryStats() // Refresh stats
            } catch (e: Exception) {
                _errorMessage.value = "Failed To Add Product: ${e.message}"
            }
        }
    }

    // Update product
    fun updateProduct(product: Product) {
        val userId = currentUserId ?: return

        viewModelScope.launch {
            try {
                repository.updateProduct(product)
                _errorMessage.value = null
                loadInventoryStats()
            } catch (e: Exception) {
                _errorMessage.value = "Product update nahi ho saka: ${e.message}"
            }
        }
    }

    // Update product quantity only
    fun updateProductQuantity(productId: String, newQuantity: Int) {
        val userId = currentUserId ?: return

        viewModelScope.launch {
            try {
                repository.updateProductQuantity(productId, userId, newQuantity)
                _errorMessage.value = null
                loadInventoryStats()
            } catch (e: Exception) {
                _errorMessage.value = "Quantity update nahi ho saki: ${e.message}"
            }
        }
    }

    // Update product price only
    fun updateProductPrice(productId: String, newPrice: Double) {
        val userId = currentUserId ?: return

        viewModelScope.launch {
            try {
                repository.updateProductPrice(productId, userId, newPrice)
                _errorMessage.value = null
                loadInventoryStats()
            } catch (e: Exception) {
                _errorMessage.value = "Price update nahi ho saki: ${e.message}"
            }
        }
    }

    // Delete product
    fun deleteProduct(productId: String) {
        val userId = currentUserId ?: return

        viewModelScope.launch {
            try {
                repository.deleteProductForUser(productId, userId)
                _errorMessage.value = null
                loadInventoryStats()
            } catch (e: Exception) {
                _errorMessage.value = "Product delete nahi ho saka: ${e.message}"
            }
        }
    }

    // Get specific product by ID
    suspend fun getProductById(productId: String): Product? {
        val userId = currentUserId ?: return null
        return try {
            repository.getProductByIdForUser(productId, userId)
        } catch (e: Exception) {
            _errorMessage.value = "Product load nahi ho saka: ${e.message}"
            null
        }
    }

    // Update search query - filtering is now reactive
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Clear error message
    fun clearError() {
        _errorMessage.value = null
    }

    // Check if user is authenticated
    fun isUserAuthenticated(): Boolean = currentUserId != null

    // Refresh data when user logs in
    fun onUserLoggedIn() {
        if (currentUserId != null) {
            loadUserInfo()
            loadProducts()
            loadLowStockProducts()
            loadInventoryStats()
        }
    }

    // Clear data when user logs out
    fun onUserLoggedOut() {
        _allProducts.value = emptyList()
        _lowStockProducts.value = emptyList()
        _totalInventoryValue.value = 0.0
        _productCount.value = 0
        _searchQuery.value = ""
        _errorMessage.value = null
        _user.value = User()
    }
}