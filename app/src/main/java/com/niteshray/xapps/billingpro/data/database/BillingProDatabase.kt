package com.niteshray.xapps.billingpro.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.niteshray.xapps.billingpro.data.dao.ProductDao
import com.niteshray.xapps.billingpro.data.dao.BillDao
import com.niteshray.xapps.billingpro.data.entity.Product
import com.niteshray.xapps.billingpro.data.entity.Bill
import com.niteshray.xapps.billingpro.data.entity.BillItem

@Database(
    entities = [Product::class, Bill::class, BillItem::class],
    version = 3,
    exportSchema = false
)
abstract class BillingProDatabase : RoomDatabase() {
    
    abstract fun productDao(): ProductDao
    abstract fun billDao(): BillDao
    
    companion object {
        @Volatile
        private var INSTANCE: BillingProDatabase? = null
        
        fun getDatabase(context: Context): BillingProDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BillingProDatabase::class.java,
                    "billing_pro_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
