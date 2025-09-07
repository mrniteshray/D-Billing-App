package com.niteshray.xapps.billingpro.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.niteshray.xapps.billingpro.data.dao.ProductDao
import com.niteshray.xapps.billingpro.data.entity.Product

@Database(
    entities = [Product::class],
    version = 2,
    exportSchema = false
)
abstract class BillingProDatabase : RoomDatabase() {
    
    abstract fun productDao(): ProductDao
    
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
