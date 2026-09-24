package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String, // e.g. মিনিকেট (কুষ্টিয়া স্পেশাল)
    val category: String = "চিকন চাল", // চিকন চাল, মোটা চাল, সুগন্ধি চাল
    val bagSizeKg: Int = 50, // 50 or 25 kg
    val currentBags: Int = 0,
    val defaultSaleRate: Double = 0.0, // Default selling rate per bag
    val defaultPurchaseRate: Double = 0.0, // Default purchase rate per bag
    val notes: String = ""
) {
    // 40 KG = 1 Mon in Bangladeshi Rice wholesale
    val totalKg: Double get() = currentBags.toDouble() * bagSizeKg
    val totalMon: Double get() = totalKg / 40.0
    val ratePerMon: Double get() = if (bagSizeKg > 0) (defaultSaleRate / bagSizeKg) * 40.0 else 0.0
}
