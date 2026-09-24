package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parties")
data class PartyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String, // e.g. মেসার্স ভাই ভাই ট্রেডার্স
    val phone: String, // e.g. 01711223344
    val address: String = "", // e.g. বাবুবাজার, ঢাকা
    val type: String = "CUSTOMER", // "CUSTOMER" (দেনাদার/খুচরা/পাইকার), "SUPPLIER" (পাওনাদার/মিলার), "BOTH"
    val balance: Double = 0.0, // Positive = We receive (Due / পাওনা), Negative = We owe (দেনা)
    val creditLimit: Double = 0.0, // সর্বোচ্চ বাকি সীমা (০ = আনলিমিটেড)
    val emptyBagsDue: Int = 0, // খালি বস্তা পাওনা/দেনা সংখ্যা (+ = কাস্টমার থেকে খালি বস্তা পাওনা)
    val note: String = ""
)
