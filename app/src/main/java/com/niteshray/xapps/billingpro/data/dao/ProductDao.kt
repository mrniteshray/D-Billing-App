package com.niteshray.xapps.billingpro.data.dao

import androidx.room.*
import com.niteshray.xapps.billingpro.data.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    
    // Get all products for a specific user
    @Query("SELECT * FROM products WHERE userId = :userId ORDER BY name ASC")
    fun getAllProductsForUser(userId: String): Flow<List<Product>>
    
    // Get specific product by ID and user
    @Query("SELECT * FROM products WHERE productId = :productId AND userId = :userId")
    suspend fun getProductByIdForUser(productId: String, userId: String): Product?
    
    // Search products for a specific user
    @Query("SELECT * FROM products WHERE userId = :userId AND name LIKE '%' || :searchQuery || '%'")
    fun searchProductsForUser(userId: String, searchQuery: String): Flow<List<Product>>
    
    // Get low stock products for a specific user
    @Query("SELECT * FROM products WHERE userId = :userId AND quantity <= :threshold")
    fun getLowStockProductsForUser(userId: String, threshold: Int = 10): Flow<List<Product>>
    
    // Insert product (must include userId)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)
    
    // Insert multiple products
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)
    
    // Update product
    @Update
    suspend fun updateProduct(product: Product)
    
    // Update product quantity for specific user
    @Query("UPDATE products SET quantity = :newQuantity, updatedAt = :updatedAt WHERE productId = :productId AND userId = :userId")
    suspend fun updateProductQuantity(productId: String, userId: String, newQuantity: Int, updatedAt: Long = System.currentTimeMillis())
    
    // Update product price for specific user
    @Query("UPDATE products SET price = :newPrice, updatedAt = :updatedAt WHERE productId = :productId AND userId = :userId")
    suspend fun updateProductPrice(productId: String, userId: String, newPrice: Double, updatedAt: Long = System.currentTimeMillis())
    
    // Delete product for specific user
    @Query("DELETE FROM products WHERE productId = :productId AND userId = :userId")
    suspend fun deleteProductForUser(productId: String, userId: String)
    
    // Delete product entity
    @Delete
    suspend fun deleteProduct(product: Product)
    
    // Get product count for specific user
    @Query("SELECT COUNT(*) FROM products WHERE userId = :userId")
    suspend fun getProductCountForUser(userId: String): Int
    
    // Get total inventory value for specific user
    @Query("SELECT SUM(quantity * price) FROM products WHERE userId = :userId")
    suspend fun getTotalInventoryValueForUser(userId: String): Double?
    
    // Check if product exists for user
    @Query("SELECT EXISTS(SELECT 1 FROM products WHERE productId = :productId AND userId = :userId)")
    suspend fun productExistsForUser(productId: String, userId: String): Boolean
}
