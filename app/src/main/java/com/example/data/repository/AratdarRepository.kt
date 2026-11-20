package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AratdarDatabase
import com.example.data.local.entity.PartyEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.StockLogEntity
import com.example.data.local.entity.TransactionEntity
import com.example.util.LedgerMath
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

    suspend fun insertProduct(
        product: ProductEntity,
        supplierId: Long = 0L,
        supplierName: String = ""
    ): Long = withContext(Dispatchers.IO) {
        database.withTransaction {
            val id = productDao.insertProduct(product)
            if (product.currentBags > 0) {
                stockLogDao.insertLog(
                    StockLogEntity(
                        productId = id,
                        productName = product.name,
                        type = "IN",
                        changeBags = product.currentBags,
                        resultingBags = product.currentBags,
                        reason = "শুরুর স্টক",
                        supplierId = supplierId,
                        supplierName = supplierName
                    )
                )
            }
            id
        }
    }

    suspend fun deleteProduct(productId: Long) = withContext(Dispatchers.IO) {
        database.withTransaction {
            stockLogDao.deleteLogsForProduct(productId)
            productDao.deleteProduct(productId)
        }
    }

    suspend fun adjustStock(
        productId: Long,
        productName: String,
        deltaBags: Int,
        reason: String,
        supplierId: Long = 0L,
        supplierName: String = ""
    ) = withContext(Dispatchers.IO) {
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
                reason = reason,
                supplierId = if (deltaBags >= 0) supplierId else 0L,
                supplierName = if (deltaBags >= 0) supplierName else ""
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
     * Writes one invoice and the four books in a single database transaction.
     * Returns null when a sale asks for more sacks than the warehouse has.
     * Party balance uses sale total minus amount paid, so an overpayment is a credit.
     * Home cash is derived only from payment rows, never from the invoice paid amount as well.
     */
    suspend fun executeTransaction(tx: TransactionEntity, emptyBagsGiven: Int = 0): Long? = withContext(Dispatchers.IO) {
        database.withTransaction {
            if (tx.type == "SALE") {
                val onHand = productDao.getProductById(tx.productId)?.currentBags ?: 0
                if (tx.bagCount > onHand) {
                    return@withTransaction null
                }
            }

            val txId = transactionDao.insertTransaction(tx)
            val balanceDelta = LedgerMath.partyBalanceDelta(tx.totalAmount, tx.paidAmount)

            if (tx.type == "SALE") {
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

                if (tx.customerId != null && balanceDelta != 0.0) {
                    partyDao.updateBalance(tx.customerId, balanceDelta)
                }

                if (tx.customerId != null && emptyBagsGiven > 0) {
                    partyDao.updateEmptyBags(tx.customerId, emptyBagsGiven)
                }

                if (tx.tradeType == "CONSIGNMENT" && tx.supplierId != null && tx.netPayableToMiller > 0) {
                    partyDao.updateBalance(tx.supplierId, -tx.netPayableToMiller)
                }

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

                // Labor is already removed from the mill's net. This row only takes notes out of the drawer.
                if (tx.tradeType == "CONSIGNMENT" && tx.laborCost > 0) {
                    paymentDao.insertPayment(
                        PaymentEntity(
                            date = tx.date,
                            partyId = 0L,
                            partyName = "খালাসি",
                            type = "PAID",
                            amount = tx.laborCost,
                            paymentMethod = "CASH",
                            note = "কমিশন চালান #${txId} খালাসি খরচ"
                        )
                    )
                }
            } else if (tx.type == "PURCHASE") {
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

                if (tx.supplierId != null && balanceDelta != 0.0) {
                    partyDao.updateBalance(tx.supplierId, -balanceDelta)
                }

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
    }

    /**
     * Records a payment and adjusts the party balance in one database transaction.
     */
    suspend fun recordPayment(payment: PaymentEntity): Long = withContext(Dispatchers.IO) {
        database.withTransaction {
            val payId = paymentDao.insertPayment(payment)
            if (payment.partyId != 0L) {
                if (payment.type == "RECEIVED") {
                    partyDao.updateBalance(payment.partyId, -payment.amount)
                } else if (payment.type == "PAID") {
                    partyDao.updateBalance(payment.partyId, payment.amount)
                }
            }
            payId
        }
    }
}
