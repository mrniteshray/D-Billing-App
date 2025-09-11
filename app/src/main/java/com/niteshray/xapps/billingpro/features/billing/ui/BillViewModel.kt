package com.niteshray.xapps.billingpro.features.billing.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niteshray.xapps.billingpro.data.dao.BillDao
import com.niteshray.xapps.billingpro.data.entity.Bill
import com.niteshray.xapps.billingpro.data.entity.BillItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

class BillViewModel(private val billDao: BillDao) : ViewModel() {

    fun getAllBills(userId: String): Flow<List<Bill>> {
        return billDao.getAllBills(userId)
    }

    fun getRecentBills(userId: String, limit: Int = 5): Flow<List<Bill>> {
        return billDao.getRecentBills(userId, limit)
    }

    fun getBillCount(userId: String): Flow<Int> {
        return billDao.getBillCount(userId)
    }

    fun getTotalSales(userId: String): Flow<Double?> {
        return billDao.getTotalSales(userId)
    }

    suspend fun getBillById(billId: String): Bill? {
        return billDao.getBillById(billId)
    }

    suspend fun getBillItems(billId: String): List<BillItem> {
        return billDao.getBillItems(billId)
    }

    // Add DateFilter enum
    enum class DateFilter(val displayName: String) {
        ALL("All"),
        TODAY("Today"),
        YESTERDAY("Yesterday"),
        THIS_WEEK("This Week"),
        THIS_MONTH("This Month"),
        CUSTOM("Custom Date")
    }

    fun saveBill(
        userId: String,
        customerName: String,
        customerPhone: String,
        cartItems: List<Pair<String, Int>>, // productId to quantity
        productDetails: Map<String, Pair<String, Double>>, // productId to (name, price)
        onBillSaved: (String) -> Unit
    ) {
        viewModelScope.launch {
            val billId = UUID.randomUUID().toString()
            val totalAmount = cartItems.sumOf { (productId, quantity) ->
                val price = productDetails[productId]?.second ?: 0.0
                quantity * price
            }
            val totalItems = cartItems.sumOf { it.second }

            val bill = Bill(
                billId = billId,
                userId = userId,
                customerName = customerName,
                customerPhone = customerPhone,
                totalAmount = totalAmount,
                totalItems = totalItems,
                createdAt = System.currentTimeMillis(),
                billStatus = "Completed"
            )

            val billItems = cartItems.map { (productId, quantity) ->
                val (productName, productPrice) = productDetails[productId] ?: ("Unknown" to 0.0)
                BillItem(
                    billItemId = UUID.randomUUID().toString(),
                    billId = billId,
                    productId = productId,
                    productName = productName,
                    productPrice = productPrice,
                    quantity = quantity,
                    totalPrice = quantity * productPrice
                )
            }

            billDao.insertBillWithItems(bill, billItems)
            onBillSaved(billId)
        }
    }

    fun deleteBill(billId: String) {
        viewModelScope.launch {
            billDao.deleteBillWithItems(billId)
        }
    }
}