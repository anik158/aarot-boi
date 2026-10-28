package com.example.util

/**
 * Invoice and cash-box arithmetic shared by the save path and Home.
 * Cash on Home is cash received minus cash paid. An invoice's paid amount
 * is not added a second time when that same money already has a payment row.
 */
object LedgerMath {

    fun saleTotal(bagCount: Int, ratePerBag: Double): Double = bagCount * ratePerBag

    fun commissionTotal(bagCount: Int, commissionPerBag: Double): Double =
        bagCount * commissionPerBag

    /** Mill is owed the sale minus the fee minus the labor lump. Never below zero. */
    fun netPayableToMiller(saleTotal: Double, commissionTotal: Double, laborTotal: Double): Double {
        val net = saleTotal - commissionTotal - laborTotal
        return if (net > 0.0) net else 0.0
    }

    /**
     * Positive: this invoice still leaves money owed.
     * Negative: the payment is larger than the invoice, which is a credit.
     */
    fun partyBalanceDelta(totalAmount: Double, paidAmount: Double): Double =
        totalAmount - paidAmount

    /** Paper notes in the drawer. Negative means the drawer is short. */
    fun cashInHand(cashReceived: Double, cashPaid: Double): Double = cashReceived - cashPaid
}
