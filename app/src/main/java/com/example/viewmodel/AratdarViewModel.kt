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
import com.example.util.LedgerMath
import com.example.util.LicenseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    private val licenseMutex = Mutex()
    private val _gateMessage = MutableStateFlow("")
    val gateMessage: StateFlow<String> = _gateMessage.asStateFlow()

    val bindingState: StateFlow<DeviceBindingState> = deviceBindingManager.bindingState

    private val _denomination = MutableStateFlow(CashDenomination())
    val denomination: StateFlow<CashDenomination> = _denomination.asStateFlow()

    init {
        val db = AratdarDatabase.getInstance(application)
        repository = AratdarRepository(db)
        if (!deviceBindingManager.isSampleLedgerCleared()) {
            runBlocking(Dispatchers.IO) { repository.clearLedger() }
            deviceBindingManager.markSampleLedgerCleared()
            deviceBindingManager.saveCashCounts(IntArray(7))
        }
        val savedCount = deviceBindingManager.loadCashCounts()
        _denomination.value = CashDenomination(
            count1000 = savedCount[0],
            count500 = savedCount[1],
            count200 = savedCount[2],
            count100 = savedCount[3],
            count50 = savedCount[4],
            count20 = savedCount[5],
            count10 = savedCount[6]
        )
        viewModelScope.launch {
            if (deviceBindingManager.bindingState.value.shopName.isNotBlank()) {
                refreshLicense()
            }
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

        val cashReceived = payList.filter { it.type == "RECEIVED" && it.paymentMethod == "CASH" }.sumOf { it.amount }
        val cashPaid = payList.filter { it.type == "PAID" && it.paymentMethod == "CASH" }.sumOf { it.amount }

        DashboardMetrics(
            totalStockBags = totalBags,
            totalStockMon = totalMon,
            totalStockValue = totalValue,
            todaySalesAmount = todaySalesAmt,
            todaySalesBags = todaySalesB,
            totalCustomerDue = customerDue,
            totalMillerPayable = millerPayable,
            cashInHand = LedgerMath.cashInHand(cashReceived, cashPaid)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    val lastSavedTransaction = MutableStateFlow<TransactionEntity?>(null)

    fun executeTransaction(tx: TransactionEntity, emptyBagsGiven: Int = 0, onSuccess: (TransactionEntity) -> Unit) {
        viewModelScope.launch {
            val id = repository.executeTransaction(tx, emptyBagsGiven) ?: return@launch
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

    fun adjustStock(
        productId: Long,
        productName: String,
        delta: Int,
        reason: String,
        supplierId: Long = 0L,
        supplierName: String = "",
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.adjustStock(productId, productName, delta, reason, supplierId, supplierName)
            onSuccess()
        }
    }

    fun deleteProduct(productId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
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
        onSuccess: (Long) -> Unit
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
            val id = repository.insertParty(party)
            onSuccess(id)
        }
    }

    fun addProduct(
        name: String,
        category: String,
        bagSizeKg: Int,
        openingBags: Int,
        saleRate: Double,
        purchaseRate: Double,
        supplierId: Long = 0L,
        supplierName: String = "",
        onSuccess: (Long) -> Unit
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
            val id = repository.insertProduct(
                product,
                supplierId = if (openingBags > 0) supplierId else 0L,
                supplierName = if (openingBags > 0) supplierName else ""
            )
            onSuccess(id)
        }
    }

    fun requestAccess(shopName: String, proprietorName: String, phone: String) {
        viewModelScope.launch {
            licenseMutex.withLock {
                deviceBindingManager.saveShopProfile(shopName, proprietorName, phone)
                if (deviceBindingManager.bindingState.value.licenseStatus != "approved") {
                    deviceBindingManager.setLicenseStatus("pending")
                }
                val state = deviceBindingManager.bindingState.value
                val registered = withContext(Dispatchers.IO) { LicenseClient.register(state) }
                if (!registered) {
                    _gateMessage.value = "একবার ইন্টারনেট লাগবে। তারপর আবার চেষ্টা করুন।"
                    return@withLock
                }
                _gateMessage.value = ""
                val remote = withContext(Dispatchers.IO) { LicenseClient.status(state.deviceId) }
                applyRemoteStatus(remote)
            }
        }
    }

    fun refreshLicense() {
        viewModelScope.launch {
            licenseMutex.withLock {
                val state = deviceBindingManager.bindingState.value
                if (state.deviceId.isBlank()) return@withLock
                val remote = withContext(Dispatchers.IO) { LicenseClient.status(state.deviceId) }
                applyRemoteStatus(remote)
            }
        }
    }

    private fun applyRemoteStatus(remote: String?) {
        if (remote == null) {
            if (!deviceBindingManager.bindingState.value.isActivated) {
                _gateMessage.value = "একবার ইন্টারনেট লাগবে। তারপর আবার চেষ্টা করুন।"
            }
            return
        }
        _gateMessage.value = ""
        val current = deviceBindingManager.bindingState.value.licenseStatus
        when (remote) {
            "approved" -> deviceBindingManager.setLicenseStatus("approved")
            "revoked" -> deviceBindingManager.setLicenseStatus("revoked")
            "unknown" -> if (current == "approved") {
                deviceBindingManager.setLicenseStatus("revoked")
            } else {
                deviceBindingManager.setLicenseStatus("pending")
                _gateMessage.value = "এই ফোনটি এখনো তালিকায় নেই। আবার চেষ্টা করুন।"
            }
            else -> deviceBindingManager.setLicenseStatus("pending")
        }
    }

    fun updateRegistration(
        shopName: String,
        proprietorName: String,
        phone: String,
        googleAccount: String,
        defaultLaborCost: Double
    ) {
        deviceBindingManager.updateRegistration(
            shopName,
            proprietorName,
            phone,
            googleAccount,
            defaultLaborCost
        )
    }

    fun updateDenomination(
        c1000: Int, c500: Int, c200: Int, c100: Int, c50: Int, c20: Int, c10: Int
    ) {
        _denomination.value = CashDenomination(c1000, c500, c200, c100, c50, c20, c10)
        deviceBindingManager.saveCashCounts(intArrayOf(c1000, c500, c200, c100, c50, c20, c10))
    }
}
