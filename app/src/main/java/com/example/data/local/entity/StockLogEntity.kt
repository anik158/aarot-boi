package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_logs")
data class StockLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val productId: Long,
    val productName: String,
    val date: Long = System.currentTimeMillis(),
    val type: String, // "IN", "OUT", "ADJUSTMENT"
    val changeBags: Int,
    val resultingBags: Int,
    val reason: String = ""
)
