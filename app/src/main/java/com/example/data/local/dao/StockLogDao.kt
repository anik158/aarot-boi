package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.StockLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockLogDao {
    @Query("SELECT * FROM stock_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<StockLogEntity>>

    @Query("SELECT * FROM stock_logs WHERE productId = :productId ORDER BY date DESC")
    fun getLogsForProduct(productId: Long): Flow<List<StockLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: StockLogEntity): Long
}
