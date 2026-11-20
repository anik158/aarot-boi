package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 4,
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
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE stock_logs ADD COLUMN supplierId INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE stock_logs ADD COLUMN supplierName TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
