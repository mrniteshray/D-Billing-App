package com.niteshray.xapps.billingpro.features.ProductManagement.domain

import com.niteshray.xapps.billingpro.data.dao.ProductDao
import com.niteshray.xapps.billingpro.data.entity.Product
import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val productDao: ProductDao
) {

    // Get all products for a specific user
    fun getAllProductsForUser(userId: String): Flow<List<Product>> =
        productDao.getAllProductsForUser(userId)

    // Get specific product by ID for a user
    suspend fun getProductByIdForUser(productId: String, userId: String): Product? =
        productDao.getProductByIdForUser(productId, userId)

    // Search products for a specific user
    fun searchProductsForUser(userId: String, searchQuery: String): Flow<List<Product>> =
        productDao.searchProductsForUser(userId, searchQuery)

    // Get low stock products for a specific user
    fun getLowStockProductsForUser(userId: String, threshold: Int = 10): Flow<List<Product>> =
        productDao.getLowStockProductsForUser(userId, threshold)

    // Insert product (must include userId)
    suspend fun insertProduct(product: Product) = productDao.insertProduct(product)

    // Insert multiple products
    suspend fun insertProducts(products: List<Product>) = productDao.insertProducts(products)

    // Update product
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)

    // Update product quantity for specific user
    suspend fun updateProductQuantity(productId: String, userId: String, newQuantity: Int) =
        productDao.updateProductQuantity(productId, userId, newQuantity)

    // Update product price for specific user
    suspend fun updateProductPrice(productId: String, userId: String, newPrice: Double) =
        productDao.updateProductPrice(productId, userId, newPrice)

    // Delete product for specific user
    suspend fun deleteProductForUser(productId: String, userId: String) =
        productDao.deleteProductForUser(productId, userId)

    // Get product count for specific user
    suspend fun getProductCountForUser(userId: String): Int =
        productDao.getProductCountForUser(userId)

    // Get total inventory value for specific user
    suspend fun getTotalInventoryValueForUser(userId: String): Double =
        productDao.getTotalInventoryValueForUser(userId) ?: 0.0

    // Check if product exists for user
    suspend fun productExistsForUser(productId: String, userId: String): Boolean =
        productDao.productExistsForUser(productId, userId)

    // Utility functions for inventory management
    suspend fun addStockForUser(productId: String, userId: String, quantityToAdd: Int): Boolean {
        val product = getProductByIdForUser(productId, userId)
        return if (product != null) {
            val newQuantity = product.quantity + quantityToAdd
            updateProductQuantity(productId, userId, newQuantity)
            true
        } else {
            false
        }
    }

    suspend fun removeStockForUser(productId: String, userId: String, quantityToRemove: Int): Boolean {
        val product = getProductByIdForUser(productId, userId)
        return if (product != null && product.quantity >= quantityToRemove) {
            val newQuantity = product.quantity - quantityToRemove
            updateProductQuantity(productId, userId, newQuantity)
            true
        } else {
            false
        }
    }

    suspend fun isProductAvailableForUser(productId: String, userId: String, requiredQuantity: Int): Boolean {
        val product = getProductByIdForUser(productId, userId)
        return product != null && product.quantity >= requiredQuantity
    }
}