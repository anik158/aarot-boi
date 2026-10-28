package com.example

import com.example.util.LedgerMath
import org.junit.Assert.assertEquals
import org.junit.Test

class LedgerMathTest {

    @Test
    fun guidelineConsignmentExample_nets582400ToTheMill() {
        val bags = 200
        val sale = LedgerMath.saleTotal(bags, 3000.0)
        val fee = LedgerMath.commissionTotal(bags, 20.0)
        val laborAndTruck = 1600.0 + 12000.0
        val millNet = LedgerMath.netPayableToMiller(sale, fee, laborAndTruck)

        assertEquals(600_000.0, sale, 0.0)
        assertEquals(4_000.0, fee, 0.0)
        assertEquals(582_400.0, millNet, 0.0)
    }

    @Test
    fun overpayment_isACredit() {
        assertEquals(-200.0, LedgerMath.partyBalanceDelta(1000.0, 1200.0), 0.0)
        assertEquals(78_500.0, LedgerMath.partyBalanceDelta(103_500.0, 25_000.0), 0.0)
    }

    @Test
    fun cashInHand_countsPaymentRowsOnce() {
        val cashFromSale = 25_000.0
        val laterCollection = 20_000.0
        val laborPaidInNotes = 500.0

        val cash = LedgerMath.cashInHand(cashFromSale + laterCollection, laborPaidInNotes)
        assertEquals(44_500.0, cash, 0.0)

        val doubleCounted = LedgerMath.cashInHand(
            cashFromSale + cashFromSale + laterCollection,
            laborPaidInNotes
        )
        assertEquals(69_500.0, doubleCounted, 0.0)
        assertEquals(44_500.0, cash, 0.0)
    }

    @Test
    fun cashInHand_staysNegativeWhenTheDrawerIsShort() {
        assertEquals(-150.0, LedgerMath.cashInHand(100.0, 250.0), 0.0)
    }
}
