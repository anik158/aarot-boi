package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val date: Long = System.currentTimeMillis(),
    val partyId: Long,
    val partyName: String,
    val type: String, // "RECEIVED" (কাস্টমার থেকে জমা), "PAID" (মিলারকে প্রদান)
    val amount: Double,
    val paymentMethod: String = "CASH", // CASH, BKASH, NAGAD, BANK
    val trxId: String = "",
    val note: String = ""
)
