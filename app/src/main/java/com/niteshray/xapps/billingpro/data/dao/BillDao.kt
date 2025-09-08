package com.niteshray.xapps.billingpro.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.niteshray.xapps.billingpro.data.entity.Bill
import com.niteshray.xapps.billingpro.data.entity.BillItem

@Dao
interface BillDao {
    
    @Query("SELECT * FROM bills WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllBills(userId: String): Flow<List<Bill>>
    
    @Query("SELECT * FROM bills WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentBills(userId: String, limit: Int = 5): Flow<List<Bill>>
    
    @Query("SELECT * FROM bills WHERE billId = :billId")
    suspend fun getBillById(billId: String): Bill?
    
    @Query("SELECT * FROM bill_items WHERE billId = :billId")
    suspend fun getBillItems(billId: String): List<BillItem>
    
    @Insert
    suspend fun insertBill(bill: Bill)
    
    @Insert
    suspend fun insertBillItems(billItems: List<BillItem>)
    
    @Transaction
    suspend fun insertBillWithItems(bill: Bill, billItems: List<BillItem>) {
        insertBill(bill)
        insertBillItems(billItems)
    }
    
    @Query("DELETE FROM bills WHERE billId = :billId")
    suspend fun deleteBill(billId: String)
    
    @Query("DELETE FROM bill_items WHERE billId = :billId")
    suspend fun deleteBillItems(billId: String)
    
    @Transaction
    suspend fun deleteBillWithItems(billId: String) {
        deleteBillItems(billId)
        deleteBill(billId)
    }
    
    @Query("SELECT COUNT(*) FROM bills WHERE userId = :userId")
    fun getBillCount(userId: String): Flow<Int>
    
    @Query("SELECT SUM(totalAmount) FROM bills WHERE userId = :userId")
    fun getTotalSales(userId: String): Flow<Double?>
}
