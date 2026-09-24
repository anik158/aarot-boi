package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AratdarDatabase
import com.example.data.local.entity.PartyEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.repository.AratdarRepository
import com.example.util.BanglaNumberUtils
import com.example.util.DeviceBindingManager
import com.example.viewmodel.CashDenomination
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: AratdarDatabase
    private lateinit var repository: AratdarRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AratdarDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = AratdarRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("আরতবই", appName)
    }

    @Test
    fun `test bangla number and currency conversion`() {
        val bengaliInput = "১২৩৪৫"
        val convertedEng = BanglaNumberUtils.toEnglishDigits(bengaliInput)
        assertEquals("12345", convertedEng)

        val engInput = "98760"
        val convertedBangla = BanglaNumberUtils.toBanglaDigits(engInput)
        assertEquals("৯৮৭৬০", convertedBangla)

        val formattedCurrency = BanglaNumberUtils.formatCurrency(185500.0)
        assertEquals("৳১,৮৫,৫০০", formattedCurrency)
    }

    // Step 1 Testing: Device Binding & Offline Grace Period
    @Test
    fun `test device binding activation and 30-day offline grace period`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val bindingManager = DeviceBindingManager(context)
        val state = bindingManager.bindingState.value

        assertTrue(state.isActivated)
        assertTrue(state.deviceId.startsWith("ARAT-BD-"))
        assertEquals("মেসার্স চালের আরত", state.shopName)
        assertEquals(30, state.graceDaysRemaining)
        assertTrue(state.canAccessOffline)

        // Test updating registration details
        bindingManager.updateRegistration(
            shopName = "মেসার্স সততা চালের আরত",
            proprietorName = "হাজী মো: ইউনুস",
            phone = "01812-998877",
            googleAccount = "ahsanulhouqe321@gmail.com"
        )
        val updatedState = bindingManager.bindingState.value
        assertEquals("মেসার্স সততা চালের আরত", updatedState.shopName)
        assertEquals("হাজী মো: ইউনুস", updatedState.proprietorName)
        assertEquals("01812-998877", updatedState.phone)
    }

    // Step 2 Testing: Product & Mon Conversion
    @Test
    fun `test mon and bag conversion for 50kg and 25kg bags`() {
        // 50 kg bag: 100 bags = 5000 kg = 125 Mon (since 40 kg = 1 Mon)
        val prod50 = ProductEntity(
            name = "মিনিকেট ৫০ কেজি",
            bagSizeKg = 50,
            currentBags = 100,
            defaultSaleRate = 3400.0
        )
        assertEquals(5000.0, prod50.totalKg, 0.01)
        assertEquals(125.0, prod50.totalMon, 0.01)
        assertEquals(2720.0, prod50.ratePerMon, 0.01)

        // 25 kg bag: 80 bags = 2000 kg = 50 Mon
        val prod25 = ProductEntity(
            name = "চিনিগুঁড়া ২৫ কেজি",
            bagSizeKg = 25,
            currentBags = 80,
            defaultSaleRate = 2800.0
        )
        assertEquals(2000.0, prod25.totalKg, 0.01)
        assertEquals(50.0, prod25.totalMon, 0.01)
        assertEquals(4480.0, prod25.ratePerMon, 0.01)
    }

    // Step 3 Testing: Party with Credit Limit and Empty Bags Balance
    @Test
    fun `test party credit limit and empty bags balance`() = runBlocking {
        val partyId = repository.insertParty(
            PartyEntity(
                name = "জনতা স্টোর",
                phone = "01711223344",
                type = "CUSTOMER",
                balance = 25000.0,
                creditLimit = 50000.0,
                emptyBagsDue = 10
            )
        )

        // Add 15 empty bags given to customer
        repository.updatePartyEmptyBags(partyId, 15)

        val updated = repository.allParties.first().first { it.id == partyId }
        assertEquals(50000.0, updated.creditLimit, 0.01)
        assertEquals(25, updated.emptyBagsDue)
    }

    // Step 4 Testing: 4-in-1 Transaction (Stock + Due + Miller + Cash with TrxID)
    @Test
    fun `test 4-in-1 own stock sale transaction updates stock customer due and cash book`() = runBlocking {
        // 1. Setup product with 100 bags
        val prodId = repository.insertProduct(
            ProductEntity(
                name = "নাজিরশাইল (দিনাজপুর)",
                category = "চিকন চাল",
                bagSizeKg = 50,
                currentBags = 100,
                defaultSaleRate = 3700.0
            )
        )

        // 2. Setup customer with 0 balance
        val customerId = repository.insertParty(
            PartyEntity(
                name = "মেসার্স ভাই ভাই স্টোর",
                phone = "01711223344",
                address = "বাবুবাজার, ঢাকা",
                type = "CUSTOMER",
                balance = 0.0,
                creditLimit = 150000.0,
                emptyBagsDue = 0
            )
        )

        // 3. Execute Sale: 30 bags @ 3700 = 111,000 TK. Paid 41,000 TK (Cash), Due 70,000 TK, 30 empty bags given
        val tx = TransactionEntity(
            date = System.currentTimeMillis(),
            type = "SALE",
            tradeType = "OWN_STOCK",
            customerId = customerId,
            customerName = "মেসার্স ভাই ভাই স্টোর",
            productId = prodId,
            productName = "নাজিরশাইল (দিনাজপুর)",
            bagCount = 30,
            bagSizeKg = 50,
            ratePerBag = 3700.0,
            totalAmount = 111000.0,
            paidAmount = 41000.0,
            dueAmount = 70000.0,
            paymentMethod = "CASH"
        )
        repository.executeTransaction(tx, emptyBagsGiven = 30)

        // Check 1: Stock decreased: 100 - 30 = 70 bags
        val updatedProduct = repository.allProducts.first().first { it.id == prodId }
        assertEquals(70, updatedProduct.currentBags)

        // Check 2: Customer due balance increased by 70,000 TK
        val updatedCustomer = repository.allParties.first().first { it.id == customerId }
        assertEquals(70000.0, updatedCustomer.balance, 0.01)

        // Check 3: Customer empty bags balance increased by 30
        assertEquals(30, updatedCustomer.emptyBagsDue)

        // Check 4: Cash payment of 41,000 TK automatically recorded in payment ledger
        val payments = repository.allPayments.first()
        assertTrue(payments.any { it.partyId == customerId && it.amount == 41000.0 && it.type == "RECEIVED" })

        // Check 5: Stock audit log recorded
        val stockLogs = repository.allStockLogs.first()
        assertTrue(stockLogs.any { it.productId == prodId && it.changeBags == -30 })
    }

    // Step 4 Testing: Consignment Chalan (Commission + Labor + Miller Net Payable)
    @Test
    fun `test consignment chalan calculates commission labor and updates customer due miller payable and cash`() = runBlocking {
        val prodId = repository.insertProduct(
            ProductEntity(
                name = "বিআর-২৮ (নওগাঁ)",
                category = "মাঝারি চাল",
                bagSizeKg = 50,
                currentBags = 200,
                defaultSaleRate = 2800.0
            )
        )

        val customerId = repository.insertParty(
            PartyEntity(
                name = "জনতা ট্রেডার্স",
                phone = "01811223344",
                type = "CUSTOMER",
                balance = 0.0
            )
        )
        val millerId = repository.insertParty(
            PartyEntity(
                name = "সততা রাইস মিল (নওগাঁ)",
                phone = "01722334455",
                type = "SUPPLIER",
                balance = 0.0
            )
        )

        // 50 bags @ 2800 = 140,000 TK
        // Commission @ 20 TK/bag = 1,000 TK
        // Labor = 500 TK
        // Miller net payable = 140,000 - 1,000 - 500 = 138,500 TK
        // Customer paid 80,000 TK via bKash with TrxID, Due = 60,000 TK
        val consignmentTx = TransactionEntity(
            date = System.currentTimeMillis(),
            type = "SALE",
            tradeType = "CONSIGNMENT",
            customerId = customerId,
            customerName = "জনতা ট্রেডার্স",
            supplierId = millerId,
            supplierName = "সততা রাইস মিল (নওগাঁ)",
            productId = prodId,
            productName = "বিআর-২৮ (নওগাঁ)",
            bagCount = 50,
            bagSizeKg = 50,
            ratePerBag = 2800.0,
            totalAmount = 140000.0,
            commissionPerBag = 20.0,
            totalCommission = 1000.0,
            laborCost = 500.0,
            netPayableToMiller = 138500.0,
            paidAmount = 80000.0,
            dueAmount = 60000.0,
            paymentMethod = "BKASH",
            trxId = "BK79812A",
            truckNo = "ঢাকা মেট্রো-ট ১১-৪৫৬৭"
        )
        repository.executeTransaction(consignmentTx, emptyBagsGiven = 50)

        // Stock decreased: 200 - 50 = 150 bags
        val updatedProduct = repository.allProducts.first().first { it.id == prodId }
        assertEquals(150, updatedProduct.currentBags)

        // Customer due = 60,000 TK
        val updatedCustomer = repository.allParties.first().first { it.id == customerId }
        assertEquals(60000.0, updatedCustomer.balance, 0.01)

        // Miller payable = -138,500 TK (denoting aratdar's debt to miller)
        val updatedMiller = repository.allParties.first().first { it.id == millerId }
        assertEquals(-138500.0, updatedMiller.balance, 0.01)

        // Payment of 80,000 TK recorded with TrxID
        val payments = repository.allPayments.first()
        val bkashPay = payments.firstOrNull { it.partyId == customerId && it.amount == 80000.0 }
        assertTrue(bkashPay != null)
        assertEquals("BK79812A", bkashPay?.trxId)
    }

    // Step 7 Testing: Rokar (Cash Denomination Reconciliation)
    @Test
    fun `test rokar denomination calculation matches physical cash`() {
        val denom = CashDenomination(
            count1000 = 50, // 50,000
            count500 = 40,  // 20,000
            count200 = 20,  // 4,000
            count100 = 30,  // 3,000
            count50 = 20,   // 1,000
            count20 = 25,   // 500
            count10 = 50    // 500
        )
        // Total = 50000 + 20000 + 4000 + 3000 + 1000 + 500 + 500 = 79,000 TK
        assertEquals(79000.0, denom.totalPhysicalCash, 0.01)
    }
}
