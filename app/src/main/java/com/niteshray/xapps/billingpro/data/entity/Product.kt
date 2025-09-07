package com.niteshray.xapps.billingpro.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey
    val productId: String,
    val userId: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
