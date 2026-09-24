package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AratdarDatabase
import com.example.data.local.entity.PartyEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.StockLogEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.repository.AratdarRepository
import com.example.util.DeviceBindingManager
import com.example.util.DeviceBindingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardMetrics(
    val totalStockBags: Int = 0,
    val totalStockMon: Double = 0.0,
    val totalStockValue: Double = 0.0,
    val todaySalesAmount: Double = 0.0,
    val todaySalesBags: Int = 0,
    val totalCustomerDue: Double = 0.0,
    val totalMillerPayable: Double = 0.0,
    val cashInHand: Double = 0.0
)

data class CashDenomination(
    val count1000: Int = 0,
    val count500: Int = 0,
    val count200: Int = 0,
    val count100: Int = 0,
    val count50: Int = 0,
    val count20: Int = 0,
    val count10: Int = 0
) {
    val totalPhysicalCash: Double
        get() = (count1000 * 1000 + count500 * 500 + count200 * 200 + count100 * 100 +
                count50 * 50 + count20 * 20 + count10 * 10).toDouble()
}

class AratdarViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AratdarRepository
    private val deviceBindingManager = DeviceBindingManager(application)

    val bindingState: StateFlow<DeviceBindingState> = deviceBindingManager.bindingState

    private val _denomination = MutableStateFlow(CashDenomination())
    val denomination: StateFlow<CashDenomination> = _denomination.asStateFlow()

    init {
        val db = AratdarDatabase.getInstance(application)
        repository = AratdarRepository(db)
        viewModelScope.launch {
            repository.checkAndSeedSampleData()
        }
    }

    val products: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val parties: StateFlow<List<PartyEntity>> = repository.allParties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<PaymentEntity>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockLogs: StateFlow<List<StockLogEntity>> = repository.allStockLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val metrics: StateFlow<DashboardMetrics> = combine(
        products,
        parties,
        transactions,
        payments
    ) { prodList, partyList, txList, payList ->
        val totalBags = prodList.sumOf { it.currentBags }
        val totalMon = prodList.sumOf { it.totalMon }
        val totalValue = prodList.sumOf { it.currentBags * it.defaultSaleRate }

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = cal.timeInMillis

        val todayTxs = txList.filter { it.date >= startOfToday && it.type == "SALE" }
        val todaySalesAmt = todayTxs.sumOf { it.totalAmount }
        val todaySalesB = todayTxs.sumOf { it.bagCount }

        val customerDue = partyList.filter { it.balance > 0 }.sumOf { it.balance }
        val millerPayable = partyList.filter { it.balance < 0 }.sumOf { kotlin.math.abs(it.balance) }

        val cashSales = txList.filter { it.type == "SALE" && it.paymentMethod == "CASH" }.sumOf { it.paidAmount }
        val cashPaymentsIn = payList.filter { it.type == "RECEIVED" && it.paymentMethod == "CASH" }.sumOf { it.amount }
        val cashPaymentsOut = payList.filter { it.type == "PAID" && it.paymentMethod == "CASH" }.sumOf { it.amount }
        val cashTotal = (cashSales + cashPaymentsIn) - cashPaymentsOut

        DashboardMetrics(
            totalStockBags = totalBags,
            totalStockMon = totalMon,
            totalStockValue = totalValue,
            todaySalesAmount = todaySalesAmt,
            todaySalesBags = todaySalesB,
            totalCustomerDue = customerDue,
            totalMillerPayable = millerPayable,
            cashInHand = if (cashTotal < 0) 0.0 else cashTotal
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    val lastSavedTransaction = MutableStateFlow<TransactionEntity?>(null)

    fun executeTransaction(tx: TransactionEntity, emptyBagsGiven: Int = 0, onSuccess: (TransactionEntity) -> Unit) {
        viewModelScope.launch {
            val id = repository.executeTransaction(tx, emptyBagsGiven)
            val saved = tx.copy(id = id)
            lastSavedTransaction.value = saved
            onSuccess(saved)
        }
    }

    fun recordPayment(payment: PaymentEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.recordPayment(payment)
            onSuccess()
        }
    }

    fun adjustStock(productId: Long, productName: String, delta: Int, reason: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.adjustStock(productId, productName, delta, reason)
            onSuccess()
        }
    }

    fun addParty(
        name: String,
        phone: String,
        address: String,
        type: String,
        openingBalance: Double,
        creditLimit: Double = 0.0,
        emptyBagsDue: Int = 0,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val party = PartyEntity(
                name = name,
                phone = phone,
                address = address,
                type = type,
                balance = openingBalance,
                creditLimit = creditLimit,
                emptyBagsDue = emptyBagsDue
            )
            repository.insertParty(party)
            onSuccess()
        }
    }

    fun addProduct(
        name: String,
        category: String,
        bagSizeKg: Int,
        openingBags: Int,
        saleRate: Double,
        purchaseRate: Double,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val product = ProductEntity(
                name = name,
                category = category,
                bagSizeKg = bagSizeKg,
                currentBags = openingBags,
                defaultSaleRate = saleRate,
                defaultPurchaseRate = purchaseRate
            )
            repository.insertProduct(product)
            onSuccess()
        }
    }

    fun updateRegistration(shopName: String, proprietorName: String, phone: String, googleAccount: String) {
        deviceBindingManager.updateRegistration(shopName, proprietorName, phone, googleAccount)
    }

    fun updateDenomination(
        c1000: Int, c500: Int, c200: Int, c100: Int, c50: Int, c20: Int, c10: Int
    ) {
        _denomination.value = CashDenomination(c1000, c500, c200, c100, c50, c20, c10)
    }
}
