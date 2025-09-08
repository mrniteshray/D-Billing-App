package com.niteshray.xapps.billingpro.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey
    val billId: String,
    val userId: String,
    val customerName: String,
    val customerPhone: String,
    val totalAmount: Double,
    val totalItems: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val billStatus: String = "COMPLETED"
)

@Entity(tableName = "bill_items")
data class BillItem(
    @PrimaryKey
    val billItemId: String,
    val billId: String,
    val productId: String,
    val productName: String,
    val productPrice: Double,
    val quantity: Int,
    val totalPrice: Double
)
