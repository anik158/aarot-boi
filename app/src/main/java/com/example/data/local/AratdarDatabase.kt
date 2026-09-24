package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.PartyDao
import com.example.data.local.dao.PaymentDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.StockLogDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.entity.PartyEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.StockLogEntity
import com.example.data.local.entity.TransactionEntity

@Database(
    entities = [
        ProductEntity::class,
        PartyEntity::class,
        TransactionEntity::class,
        PaymentEntity::class,
        StockLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AratdarDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun partyDao(): PartyDao
    abstract fun transactionDao(): TransactionDao
    abstract fun paymentDao(): PaymentDao
    abstract fun stockLogDao(): StockLogDao

    companion object {
        @Volatile
        private var INSTANCE: AratdarDatabase? = null

        fun getInstance(context: Context): AratdarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AratdarDatabase::class.java,
                    "chaler_aratdar.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
