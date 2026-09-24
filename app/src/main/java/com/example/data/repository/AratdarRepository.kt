package com.example.data.repository

import com.example.data.local.AratdarDatabase
import com.example.data.local.entity.PartyEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.StockLogEntity
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AratdarRepository(private val database: AratdarDatabase) {

    private val productDao = database.productDao()
    private val partyDao = database.partyDao()
    private val transactionDao = database.transactionDao()
    private val paymentDao = database.paymentDao()
    private val stockLogDao = database.stockLogDao()

    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val allParties: Flow<List<PartyEntity>> = partyDao.getAllParties()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allPayments: Flow<List<PaymentEntity>> = paymentDao.getAllPayments()
    val allStockLogs: Flow<List<StockLogEntity>> = stockLogDao.getAllLogs()

    suspend fun insertProduct(product: ProductEntity): Long = withContext(Dispatchers.IO) {
        productDao.insertProduct(product)
    }

    suspend fun adjustStock(productId: Long, productName: String, deltaBags: Int, reason: String) = withContext(Dispatchers.IO) {
        productDao.adjustStock(productId, deltaBags)
        val product = productDao.getProductById(productId)
        val resulting = product?.currentBags ?: 0
        stockLogDao.insertLog(
            StockLogEntity(
                productId = productId,
                productName = productName,
                type = if (deltaBags >= 0) "IN" else "OUT",
                changeBags = deltaBags,
                resultingBags = resulting,
                reason = reason
            )
        )
    }

    suspend fun insertParty(party: PartyEntity): Long = withContext(Dispatchers.IO) {
        partyDao.insertParty(party)
    }

    suspend fun updateParty(party: PartyEntity) = withContext(Dispatchers.IO) {
        partyDao.updateParty(party)
    }

    suspend fun updatePartyEmptyBags(partyId: Long, delta: Int) = withContext(Dispatchers.IO) {
        partyDao.updateEmptyBags(partyId, delta)
    }

    /**
     * Executes a true 4-in-1 Transaction in a single flow:
     * 1. Inserts transaction record into transactions table
     * 2. Updates product stock (decrements for SALE, increments for PURCHASE) & logs stock movement
     * 3. Updates Customer Due (দেনাদার খাতা) or Miller Payable (পাওনাদার খাতা)
     * 4. Updates Cash Book (ক্যাশ খাতা) by inserting payment record if paidAmount > 0
     */
    suspend fun executeTransaction(tx: TransactionEntity, emptyBagsGiven: Int = 0): Long = withContext(Dispatchers.IO) {
        val txId = transactionDao.insertTransaction(tx)

        if (tx.type == "SALE") {
            // 1. Deduct stock from গোডাউন
            productDao.adjustStock(tx.productId, -tx.bagCount)
            val product = productDao.getProductById(tx.productId)
            stockLogDao.insertLog(
                StockLogEntity(
                    productId = tx.productId,
                    productName = tx.productName,
                    type = "OUT",
                    changeBags = -tx.bagCount,
                    resultingBags = product?.currentBags ?: 0,
                    reason = if (tx.tradeType == "CONSIGNMENT") "কমিশন চালান বিক্রয় (চালান #${txId})" else "চালান বিক্রয় (চালান #${txId})"
                )
            )

            // 2. দেনাদার খাতা: Customer owes remaining due
            if (tx.customerId != null && tx.dueAmount > 0) {
                partyDao.updateBalance(tx.customerId, tx.dueAmount)
            }

            // 3. খালি বস্তা খাতা: If empty bags given, track empty bags due
            if (tx.customerId != null && emptyBagsGiven > 0) {
                partyDao.updateEmptyBags(tx.customerId, emptyBagsGiven)
            }

            // 4. পাওনাদার খাতা (For Consignment): We owe the miller netPayableToMiller
            if (tx.tradeType == "CONSIGNMENT" && tx.supplierId != null && tx.netPayableToMiller > 0) {
                partyDao.updateBalance(tx.supplierId, -tx.netPayableToMiller)
            }

            // 5. ক্যাশ খাতা: Record immediate cash/bank/bkash payment received
            if (tx.paidAmount > 0) {
                paymentDao.insertPayment(
                    PaymentEntity(
                        date = tx.date,
                        partyId = tx.customerId ?: 0L,
                        partyName = tx.customerName.ifEmpty { "নগদ খরিদ্দার" },
                        type = "RECEIVED",
                        amount = tx.paidAmount,
                        paymentMethod = tx.paymentMethod,
                        trxId = tx.trxId,
                        note = "চালান #${txId} এ নগদ/অনলাইন জমা (${tx.productName} ${tx.bagCount} বস্তা)"
                    )
                )
            }
        } else if (tx.type == "PURCHASE") {
            // 1. Add stock to গোডাউন
            productDao.adjustStock(tx.productId, tx.bagCount)
            val product = productDao.getProductById(tx.productId)
            stockLogDao.insertLog(
                StockLogEntity(
                    productId = tx.productId,
                    productName = tx.productName,
                    type = "IN",
                    changeBags = tx.bagCount,
                    resultingBags = product?.currentBags ?: 0,
                    reason = "ক্রয় চালান (চালান #${txId})"
                )
            )

            // 2. পাওনাদার খাতা: Miller payable
            if (tx.supplierId != null && tx.dueAmount > 0) {
                partyDao.updateBalance(tx.supplierId, -tx.dueAmount)
            }

            // 3. ক্যাশ খাতা: Immediate cash paid to supplier
            if (tx.paidAmount > 0) {
                paymentDao.insertPayment(
                    PaymentEntity(
                        date = tx.date,
                        partyId = tx.supplierId ?: 0L,
                        partyName = tx.supplierName,
                        type = "PAID",
                        amount = tx.paidAmount,
                        paymentMethod = tx.paymentMethod,
                        trxId = tx.trxId,
                        note = "ক্রয় চালান #${txId} এ নগদ প্রদান (${tx.productName} ${tx.bagCount} বস্তা)"
                    )
                )
            }
        }

        txId
    }

    /**
     * Records a payment and adjusts party running balance
     */
    suspend fun recordPayment(payment: PaymentEntity): Long = withContext(Dispatchers.IO) {
        val payId = paymentDao.insertPayment(payment)
        if (payment.type == "RECEIVED") {
            // Customer paid us -> decreases customer due (subtract amount)
            partyDao.updateBalance(payment.partyId, -payment.amount)
        } else if (payment.type == "PAID") {
            // We paid Miller -> decreases our payable to miller (add amount towards 0)
            partyDao.updateBalance(payment.partyId, payment.amount)
        }
        payId
    }

    /**
     * Checks and populates realistic demo data if database is empty
     */
    suspend fun checkAndSeedSampleData() = withContext(Dispatchers.IO) {
        val prodCount = productDao.countProducts()
        if (prodCount == 0) {
            val sampleProducts = listOf(
                ProductEntity(
                    name = "মিনিকেট (কুষ্টিয়া স্পেশাল)",
                    category = "চিকন চাল",
                    bagSizeKg = 50,
                    currentBags = 280,
                    defaultSaleRate = 3450.0,
                    defaultPurchaseRate = 3250.0,
                    notes = "৫০ কেজি চটের বস্তা, প্রিমিয়াম পলিশ"
                ),
                ProductEntity(
                    name = "নাজিরশাইল (দিনাজপুর)",
                    category = "চিকন চাল",
                    bagSizeKg = 50,
                    currentBags = 160,
                    defaultSaleRate = 3700.0,
                    defaultPurchaseRate = 3500.0,
                    notes = "পুরনো চাল, ঝরঝরে ভাত"
                ),
                ProductEntity(
                    name = "বিআর-২৮ (নওগাঁ)",
                    category = "মাঝারি চাল",
                    bagSizeKg = 50,
                    currentBags = 350,
                    defaultSaleRate = 2850.0,
                    defaultPurchaseRate = 2650.0,
                    notes = "৫০ কেজি বস্তা, নিয়মিত বিক্রি"
                ),
                ProductEntity(
                    name = "কাটারিভোগ (দিনাজপুর)",
                    category = "সুগন্ধি চাল",
                    bagSizeKg = 25,
                    currentBags = 90,
                    defaultSaleRate = 2400.0,
                    defaultPurchaseRate = 2150.0,
                    notes = "২৫ কেজি ব্যাগ, পোলাও ও বিরিয়ানি"
                ),
                ProductEntity(
                    name = "চিনিগুঁড়া সুগন্ধি (বগুড়া)",
                    category = "সুগন্ধি চাল",
                    bagSizeKg = 25,
                    currentBags = 120,
                    defaultSaleRate = 2950.0,
                    defaultPurchaseRate = 2700.0,
                    notes = "২৫ কেজি ছোট প্যাকেট"
                ),
                ProductEntity(
                    name = "স্বর্ণা / মোটা চাল",
                    category = "মোটা চাল",
                    bagSizeKg = 50,
                    currentBags = 210,
                    defaultSaleRate = 2450.0,
                    defaultPurchaseRate = 2300.0,
                    notes = "৫০ কেজি বস্তা"
                )
            )
            productDao.insertProducts(sampleProducts)

            val sampleParties = listOf(
                PartyEntity(
                    name = "মেসার্স ভাই ভাই স্টোর",
                    phone = "01712345678",
                    address = "বাবুবাজার চালের আড়ত, ঢাকা",
                    type = "CUSTOMER",
                    balance = 78500.0, // Customer owes us 78,500 TK
                    creditLimit = 150000.0,
                    emptyBagsDue = 45,
                    note = "নিয়মিত পাইকারি ক্রেতা"
                ),
                PartyEntity(
                    name = "জনতা ট্রেডার্স",
                    phone = "01819876543",
                    address = "চকবাজার, ঢাকা",
                    type = "CUSTOMER",
                    balance = 45000.0,
                    creditLimit = 100000.0,
                    emptyBagsDue = 20,
                    note = "সাপ্তাহিক বাকি শোধ করে"
                ),
                PartyEntity(
                    name = "সততা রাইস মিল (নওগাঁ)",
                    phone = "01733445566",
                    address = "নওগাঁ সদর",
                    type = "SUPPLIER",
                    balance = -185000.0, // We owe miller 1,85,000 TK
                    creditLimit = 500000.0,
                    emptyBagsDue = 0,
                    note = "মিলার (কমিশন চালান পাঠান)"
                ),
                PartyEntity(
                    name = "গোল্ডেন অটো রাইস মিল",
                    phone = "01911223344",
                    address = "কুষ্টিয়া বাইপাস",
                    type = "SUPPLIER",
                    balance = -120000.0,
                    creditLimit = 300000.0,
                    emptyBagsDue = 0,
                    note = "মিনিকেটের নিয়মিত মিলার"
                ),
                PartyEntity(
                    name = "মায়ের দোয়া জেনারেল স্টোর",
                    phone = "01622334455",
                    address = "মিরপুর-১ বাজার, ঢাকা",
                    type = "CUSTOMER",
                    balance = 25000.0,
                    creditLimit = 50000.0,
                    emptyBagsDue = 15,
                    note = "খুচরা ক্রেতা"
                )
            )
            partyDao.insertParties(sampleParties)

            // Insert initial sample transactions
            val initialTx1 = TransactionEntity(
                date = System.currentTimeMillis() - 86400000L * 2,
                type = "SALE",
                tradeType = "OWN_STOCK",
                customerId = 1L,
                customerName = "মেসার্স ভাই ভাই স্টোর",
                productId = 1L,
                productName = "মিনিকেট (কুষ্টিয়া স্পেশাল)",
                bagCount = 30,
                bagSizeKg = 50,
                ratePerBag = 3450.0,
                totalAmount = 103500.0,
                paidAmount = 25000.0,
                dueAmount = 78500.0,
                paymentMethod = "CASH",
                chalanNo = "CH-1001",
                notes = "৩০ বস্তা নিজস্ব মিনিকেট ডেলিভারি"
            )
            transactionDao.insertTransaction(initialTx1)

            val initialTx2 = TransactionEntity(
                date = System.currentTimeMillis() - 86400000L,
                type = "SALE",
                tradeType = "CONSIGNMENT",
                customerId = 2L,
                customerName = "জনতা ট্রেডার্স",
                supplierId = 3L,
                supplierName = "সততা রাইস মিল (নওগাঁ)",
                productId = 3L,
                productName = "বিআর-২৮ (নওগাঁ)",
                bagCount = 50,
                bagSizeKg = 50,
                ratePerBag = 2850.0,
                totalAmount = 142500.0,
                commissionPerBag = 20.0,
                totalCommission = 1000.0,
                laborCost = 500.0,
                netPayableToMiller = 141000.0,
                paidAmount = 97500.0,
                dueAmount = 45000.0,
                paymentMethod = "BKASH",
                trxId = "BK79812A",
                chalanNo = "CH-1002",
                truckNo = "ঢাকা মেট্রো-ট ১১-৪৫৬৭",
                notes = "নওগাঁর চালান থেকে সরাসরি ডেলিভারি"
            )
            transactionDao.insertTransaction(initialTx2)

            val initialPayment = PaymentEntity(
                date = System.currentTimeMillis() - 43200000L,
                partyId = 1L,
                partyName = "মেসার্স ভাই ভাই স্টোর",
                type = "RECEIVED",
                amount = 20000.0,
                paymentMethod = "CASH",
                trxId = "",
                note = "বকেয়া টাকা জমা ক্যাশে"
            )
            paymentDao.insertPayment(initialPayment)
        }
    }
}
