package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val date: Long = System.currentTimeMillis(),
    val type: String, // "SALE" (বিক্রি) or "PURCHASE" (ক্রয়)
    val tradeType: String = "OWN_STOCK", // "OWN_STOCK" (নিজস্ব চাল) or "CONSIGNMENT" (কমিশন চালান)
    val customerId: Long? = null,
    val customerName: String = "",
    val supplierId: Long? = null,
    val supplierName: String = "",
    val productId: Long,
    val productName: String,
    val bagCount: Int,
    val bagSizeKg: Int = 50,
    val ratePerBag: Double, // Selling or Purchase rate per bag
    val totalAmount: Double, // bagCount * ratePerBag
    val commissionPerBag: Double = 0.0, // For consignment
    val totalCommission: Double = 0.0, // bagCount * commissionPerBag
    val laborCost: Double = 0.0, // Khulasi / labor deduction
    val netPayableToMiller: Double = 0.0, // totalAmount - totalCommission - laborCost
    val paidAmount: Double = 0.0, // Cash collected or paid immediately
    val dueAmount: Double = 0.0, // Remaining due
    val paymentMethod: String = "CASH", // CASH, BKASH, NAGAD, CREDIT
    val trxId: String = "",
    val chalanNo: String = "",
    val truckNo: String = "",
    val notes: String = ""
)
