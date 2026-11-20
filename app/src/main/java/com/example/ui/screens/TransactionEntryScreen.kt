package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.PartyEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.TransactionEntity
import com.example.ui.components.ChalanMemoDialog
import com.example.ui.components.OptionalMillerField
import com.example.ui.theme.LocalNanoColors
import com.example.util.BanglaNumberUtils
import com.example.util.LedgerMath
import com.example.viewmodel.AratdarViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEntryScreen(
    viewModel: AratdarViewModel,
    onNavigateBack: () -> Unit
) {
    val colors = LocalNanoColors.current
    val products by viewModel.products.collectAsState()
    val parties by viewModel.parties.collectAsState()
    val bindingState by viewModel.bindingState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Mode: Sale vs Purchase
    var isSaleMode by remember { mutableStateOf(true) }

    // If Sale Mode: Toggle between Own Stock (Trading) and Consignment (কমিশন চালান)
    var isConsignment by remember { mutableStateOf(false) }

    // Selected Items
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(products.firstOrNull()) }
    var selectedCustomer by remember { mutableStateOf<PartyEntity?>(parties.firstOrNull { it.type == "CUSTOMER" || it.type == "BOTH" }) }
    var selectedMiller by remember { mutableStateOf<PartyEntity?>(parties.firstOrNull { it.type == "SUPPLIER" || it.type == "BOTH" }) }

    var productDropdownExpanded by remember { mutableStateOf(false) }
    var customerDropdownExpanded by remember { mutableStateOf(false) }
    var millerDropdownExpanded by remember { mutableStateOf(false) }

    // Inputs
    var bagCountStr by remember { mutableStateOf("50") }
    var ratePerBagStr by remember {
        mutableStateOf(selectedProduct?.defaultSaleRate?.toInt()?.toString() ?: "3450")
    }

    // Consignment deductions
    var commissionPerBagStr by remember { mutableStateOf("20") }
    val laborDefault = bindingState.defaultLaborCost
    val laborDefaultText = if (laborDefault % 1.0 == 0.0) laborDefault.toLong().toString() else laborDefault.toString()
    var laborCostStr by remember { mutableStateOf(laborDefaultText) }

    // Payment details
    var paidAmountStr by remember { mutableStateOf("0") }
    var paymentMethod by remember { mutableStateOf("CASH") } // CASH, BKASH, NAGAD, BANK
    var trxIdStr by remember { mutableStateOf("") }
    var truckNoStr by remember { mutableStateOf("") }
    var emptyBagsGivenStr by remember { mutableStateOf("0") }
    var notesStr by remember { mutableStateOf("") }

    // Dialog state for showing generated memo receipt
    var savedTransactionForMemo by remember { mutableStateOf<TransactionEntity?>(null) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showAddPartyDialog by remember { mutableStateOf(false) }
    var addPartyAsMiller by remember { mutableStateOf(false) }

    var newProdName by remember { mutableStateOf("") }
    var newProdCategory by remember { mutableStateOf("চিকন চাল") }
    var newProdBagSize by remember { mutableStateOf("50") }
    var newProdOpeningBags by remember { mutableStateOf("0") }
    var newProdSaleRate by remember { mutableStateOf("0") }
    var newProdPurchaseRate by remember { mutableStateOf("0") }
    var newProdMiller by remember { mutableStateOf<PartyEntity?>(null) }

    var newPartyName by remember { mutableStateOf("") }
    var newPartyPhone by remember { mutableStateOf("") }
    var newPartyAddress by remember { mutableStateOf("") }
    var newPartyType by remember { mutableStateOf("CUSTOMER") }
    var newPartyCreditLimit by remember { mutableStateOf("0") }
    var newPartyEmptyBags by remember { mutableStateOf("0") }

    val modeAccentBg = when {
        !isSaleMode -> colors.badgeOrangeBg
        isConsignment -> colors.badgeBlueBg
        else -> colors.badgeSuccessBg
    }
    val modeAccentText = when {
        !isSaleMode -> colors.badgeOrangeText
        isConsignment -> colors.badgeBlueText
        else -> colors.badgeSuccessText
    }

    val bagCount = BanglaNumberUtils.toEnglishDigits(bagCountStr).toIntOrNull() ?: 0
    val ratePerBag = BanglaNumberUtils.toEnglishDigits(ratePerBagStr).toDoubleOrNull() ?: 0.0
    val totalAmount = LedgerMath.saleTotal(bagCount, ratePerBag)

    // Mon conversion: 40 kg = 1 Mon
    val bagSizeKg = selectedProduct?.bagSizeKg ?: 50
    val totalKg = bagCount * bagSizeKg
    val totalMon = totalKg / 40.0
    val ratePerMon = if (bagSizeKg > 0) (ratePerBag / bagSizeKg) * 40.0 else 0.0

    val commissionPerBag = BanglaNumberUtils.toEnglishDigits(commissionPerBagStr).toDoubleOrNull() ?: 0.0
    val totalCommission = if (isConsignment) LedgerMath.commissionTotal(bagCount, commissionPerBag) else 0.0
    val laborCost = if (isConsignment) (BanglaNumberUtils.toEnglishDigits(laborCostStr).toDoubleOrNull() ?: 0.0) else 0.0
    val netPayableToMiller = if (isConsignment) {
        LedgerMath.netPayableToMiller(totalAmount, totalCommission, laborCost)
    } else 0.0

    val paidAmount = BanglaNumberUtils.toEnglishDigits(paidAmountStr).toDoubleOrNull() ?: 0.0
    val balanceDelta = LedgerMath.partyBalanceDelta(totalAmount, paidAmount)
    val dueAmount = if (balanceDelta > 0) balanceDelta else 0.0
    val emptyBagsGiven = BanglaNumberUtils.toEnglishDigits(emptyBagsGivenStr).toIntOrNull() ?: 0

    // Credit limit check
    val customerCreditLimit = selectedCustomer?.creditLimit ?: 0.0
    val customerCurrentDue = selectedCustomer?.balance ?: 0.0
    val willExceedCreditLimit = isSaleMode && customerCreditLimit > 0 && (customerCurrentDue + dueAmount > customerCreditLimit)

    Scaffold(
        containerColor = colors.bg,
        topBar = {
            Surface(
                color = modeAccentBg,
                shadowElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, modeAccentText.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান",
                            tint = modeAccentText
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = if (isSaleMode) "নতুন বিক্রয় চালান তৈরি" else "নতুন ক্রয় চালান তৈরি",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = modeAccentText
                        )
                        Text(
                            text = if (isConsignment) "কমিশন আড়তদারি (চালানী মাল বিক্রয়)" else if (isSaleMode) "নিজস্ব মজুদ থেকে বিক্রয় চালান" else "মিলার থেকে ক্রয় চালান",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = modeAccentText.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Mode Selector (Screenshot 2 Rounded Pill Style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.surfaceElevated)
                    .border(1.dp, colors.border, RoundedCornerShape(24.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { isSaleMode = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("mode_sale_button"),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSaleMode) colors.primary else Color.Transparent,
                        contentColor = if (isSaleMode) colors.onPrimary else colors.textSecondary
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("বিক্রি চালান (Sale)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        isSaleMode = false
                        isConsignment = false
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("mode_purchase_button"),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isSaleMode) colors.primary else Color.Transparent,
                        contentColor = if (!isSaleMode) colors.onPrimary else colors.textSecondary
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("ক্রয় চালান (Purchase)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // Sale mode: large Own Stock vs Commission choice cards
            if (isSaleMode) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "চালানের প্রকৃতি — কোনটা বেছে নিচ্ছেন?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TradeTypeChoiceCard(
                            title = "নিজস্ব চাল",
                            subtitle = "আপনার কেনা চাল বিক্রি",
                            selected = !isConsignment,
                            selectedBg = colors.badgeSuccessBg,
                            selectedBorder = colors.badgeSuccessText,
                            selectedText = colors.badgeSuccessText,
                            onClick = { isConsignment = false },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chip_own_stock")
                        )
                        TradeTypeChoiceCard(
                            title = "কমিশন চালান",
                            subtitle = "মিলারের চালানী মাল",
                            selected = isConsignment,
                            selectedBg = colors.badgeBlueBg,
                            selectedBorder = colors.badgeBlueText,
                            selectedText = colors.badgeBlueText,
                            onClick = { isConsignment = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chip_consignment")
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(modeAccentBg)
                            .border(1.dp, modeAccentText.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = modeAccentText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isConsignment) {
                                "মিলারের চাল বিক্রি করবেন। কমিশন ও খালাসি কেটে মিলারকে প্রদেয় টাকা হিসাব হবে।"
                            } else {
                                "আপনার নিজের কেনা চাল বিক্রি করবেন। লাভ-লোকসান আপনার।"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = modeAccentText
                        )
                    }
                }
            }

            // Section 1: Product Selection
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "১. চালের জাত ও স্টক নির্বাচন",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ExposedDropdownMenuBox(
                        expanded = productDropdownExpanded,
                        onExpandedChange = { productDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedProduct?.name ?: "চাল নির্বাচন করুন",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("select_product_dropdown"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.surfaceElevated,
                                unfocusedContainerColor = colors.surfaceElevated,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = productDropdownExpanded,
                            onDismissRequest = { productDropdownExpanded = false },
                            containerColor = colors.surfaceElevated
                        ) {
                            products.forEach { prod ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(prod.name, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                            Text(
                                                "মজুদ: ${BanglaNumberUtils.toBanglaDigits(prod.currentBags)} বস্তা (~${BanglaNumberUtils.toBanglaDigits(prod.totalMon.toInt())} মণ) • দর: ${BanglaNumberUtils.formatCurrency(if (isSaleMode) prod.defaultSaleRate else prod.defaultPurchaseRate)}",
                                                fontSize = 11.sp,
                                                color = colors.textMuted
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedProduct = prod
                                        ratePerBagStr = if (isSaleMode) prod.defaultSaleRate.toInt().toString() else prod.defaultPurchaseRate.toInt().toString()
                                        productDropdownExpanded = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("+ নতুন চাল যোগ করুন", fontWeight = FontWeight.Bold, color = colors.primary)
                                    }
                                },
                                onClick = {
                                    productDropdownExpanded = false
                                    showAddProductDialog = true
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bag count and Rate
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = bagCountStr,
                            onValueChange = { bagCountStr = it },
                            label = { Text("বস্তার সংখ্যা", color = colors.textMuted) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_bag_count"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.surfaceElevated,
                                unfocusedContainerColor = colors.surfaceElevated,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = ratePerBagStr,
                            onValueChange = { ratePerBagStr = it },
                            label = { Text("দর (প্রতি বস্তা)", color = colors.textMuted) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_rate_per_bag"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.surfaceElevated,
                                unfocusedContainerColor = colors.surfaceElevated,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Mon Conversion Indicator Pill (Screenshot 3 style)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceElevated)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "বস্তা সাইজ: ${selectedProduct?.bagSizeKg ?: 50} কেজি",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                            Text(
                                text = "ওজন: ${BanglaNumberUtils.toBanglaDigits(totalMon.toInt())} মণ (${BanglaNumberUtils.toBanglaDigits(totalKg)} কেজি)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                            Text(
                                text = "মণ দর: ~${BanglaNumberUtils.formatCurrency(ratePerMon)}",
                                fontSize = 11.sp,
                                color = colors.badgeAmberText
                            )
                        }
                    }
                }
            }

            // Section 2: Parties Selection
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isSaleMode) "২. ক্রেতা / খরিদ্দার নির্বাচন" else "২. মিলার / সরবরাহকারী নির্বাচন",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (isSaleMode) {
                        ExposedDropdownMenuBox(
                            expanded = customerDropdownExpanded,
                            onExpandedChange = { customerDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedCustomer?.name ?: "ক্রেতা নির্বাচন করুন",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .testTag("select_customer_dropdown"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = colors.surfaceElevated,
                                    unfocusedContainerColor = colors.surfaceElevated,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary,
                                    focusedBorderColor = colors.primary,
                                    unfocusedBorderColor = colors.border
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = customerDropdownExpanded,
                                onDismissRequest = { customerDropdownExpanded = false },
                                containerColor = colors.surfaceElevated
                            ) {
                                parties.filter { it.type == "CUSTOMER" || it.type == "BOTH" }.forEach { party ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(party.name, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                                    Text("ঠিকানা: ${party.address} • ফোন: ${party.phone}", fontSize = 11.sp, color = colors.textMuted)
                                                }
                                                Text(
                                                    text = if (party.balance > 0) "বাকি: ${BanglaNumberUtils.formatCurrency(party.balance)}" else "পরিশোধিত",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (party.balance > 0) colors.badgeDangerText else colors.badgeSuccessText
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedCustomer = party
                                            customerDropdownExpanded = false
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("+ নতুন পার্টি", fontWeight = FontWeight.Bold, color = colors.primary)
                                        }
                                    },
                                    onClick = {
                                        customerDropdownExpanded = false
                                        addPartyAsMiller = false
                                        newPartyType = "CUSTOMER"
                                        showAddPartyDialog = true
                                    }
                                )
                            }
                        }

                        // Credit Limit Warning if applicable
                        if (willExceedCreditLimit) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.badgeOrangeBg)
                                    .border(1.dp, colors.badgeOrangeText.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = colors.badgeOrangeText, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "সতর্কতা: কাস্টমারের ক্রেডিট লিমিট অতিক্রম করবে! (লিমিট: ${BanglaNumberUtils.formatCurrency(customerCreditLimit)})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.badgeOrangeText
                                    )
                                }
                            }
                        }
                    }

                    // If Consignment or Purchase: Select Miller
                    if (isConsignment || !isSaleMode) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isConsignment) "যে মিলারের চালানী মাল (সাপ্লায়ার):" else "মিলার নির্বাচন:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        ExposedDropdownMenuBox(
                            expanded = millerDropdownExpanded,
                            onExpandedChange = { millerDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedMiller?.name ?: "মিলার নির্বাচন করুন",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = millerDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .testTag("select_miller_dropdown"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = colors.surfaceElevated,
                                    unfocusedContainerColor = colors.surfaceElevated,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary,
                                    focusedBorderColor = colors.primary,
                                    unfocusedBorderColor = colors.border
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = millerDropdownExpanded,
                                onDismissRequest = { millerDropdownExpanded = false },
                                containerColor = colors.surfaceElevated
                            ) {
                                parties.filter { it.type == "SUPPLIER" || it.type == "BOTH" }.forEach { party ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(party.name, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                                    Text(party.address, fontSize = 11.sp, color = colors.textMuted)
                                                }
                                                Text(
                                                    text = "মিলার পাবে: ${BanglaNumberUtils.formatCurrency(kotlin.math.abs(party.balance))}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = colors.badgeOrangeText
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedMiller = party
                                            millerDropdownExpanded = false
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("+ নতুন মিলার", fontWeight = FontWeight.Bold, color = colors.primary)
                                        }
                                    },
                                    onClick = {
                                        millerDropdownExpanded = false
                                        addPartyAsMiller = true
                                        newPartyType = "SUPPLIER"
                                        showAddPartyDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: Consignment Deductions & Logistics (Only for Consignment)
            if (isConsignment) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.badgeBlueText.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Calculate, contentDescription = null, tint = colors.badgeBlueText, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "৩. কমিশন ও খালাসি কর্তন হিসাব",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = commissionPerBagStr,
                                onValueChange = { commissionPerBagStr = it },
                                label = { Text("কমিশন (প্রতি বস্তা)", color = colors.textMuted) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_commission_per_bag"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = colors.surfaceElevated,
                                    unfocusedContainerColor = colors.surfaceElevated,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary,
                                    focusedBorderColor = colors.primary,
                                    unfocusedBorderColor = colors.border
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = laborCostStr,
                                onValueChange = { laborCostStr = it },
                                label = { Text("লেবার / খালাসি খরচ", color = colors.textMuted) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_labor_cost"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = colors.surfaceElevated,
                                    unfocusedContainerColor = colors.surfaceElevated,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary,
                                    focusedBorderColor = colors.primary,
                                    unfocusedBorderColor = colors.border
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = truckNoStr,
                            onValueChange = { truckNoStr = it },
                            label = { Text("ট্রাক নম্বর / চালান রেফারেন্স", color = colors.textMuted) },
                            placeholder = { Text("ঢাকা মেট্রো-ট ১১-...", color = colors.textMuted) },
                            leadingIcon = { Icon(Icons.Default.LocalShipping, contentDescription = null, tint = colors.textMuted) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_truck_no"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.surfaceElevated,
                                unfocusedContainerColor = colors.surfaceElevated,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Consignment Calculation Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.badgeBlueBg)
                                .border(1.dp, colors.badgeBlueText.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("মোট বিক্রয় মূল্য:", fontSize = 12.sp, color = colors.textSecondary)
                                    Text(BanglaNumberUtils.formatCurrency(totalAmount), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("(-) আড়ত কমিশন (${BanglaNumberUtils.toBanglaDigits(bagCount)} × ৳${commissionPerBagStr}):", fontSize = 12.sp, color = colors.badgeSuccessText)
                                    Text("- ${BanglaNumberUtils.formatCurrency(totalCommission)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.badgeSuccessText)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("(-) কুলি ও খালাসি খরচ:", fontSize = 12.sp, color = colors.badgeOrangeText)
                                    Text("- ${BanglaNumberUtils.formatCurrency(laborCost)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.badgeOrangeText)
                                }
                                HorizontalDivider(color = colors.borderSubtle, modifier = Modifier.padding(vertical = 4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("(=) মিলারকে প্রদেয় নিট টাকা:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                    Text(BanglaNumberUtils.formatCurrency(netPayableToMiller), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                                }
                            }
                        }
                    }
                }
            }

            // Section 4: Payment Details
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "৪. পেমেন্ট ও নগদ জমা",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = paidAmountStr,
                            onValueChange = { paidAmountStr = it },
                            label = { Text("তাৎক্ষণিক পরিশোধ", color = colors.textMuted) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_paid_amount"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.surfaceElevated,
                                unfocusedContainerColor = colors.surfaceElevated,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Empty bags given input
                        OutlinedTextField(
                            value = emptyBagsGivenStr,
                            onValueChange = { emptyBagsGivenStr = it },
                            label = { Text("খালি বস্তা প্রদান", color = colors.textMuted) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_empty_bags"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.surfaceElevated,
                                unfocusedContainerColor = colors.surfaceElevated,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Payment Method Chips (Screenshot 2 Rounded Pills)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("CASH" to "নগদ", "BKASH" to "বিকাশ", "NAGAD" to "নগদ পে", "BANK" to "ব্যাংক").forEach { (method, label) ->
                            val selected = paymentMethod == method
                            FilterChip(
                                selected = selected,
                                onClick = { paymentMethod = method },
                                label = { Text(label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.primaryContainer,
                                    selectedLabelColor = colors.primary,
                                    containerColor = colors.surfaceElevated,
                                    labelColor = colors.textSecondary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (paymentMethod == "BKASH" || paymentMethod == "NAGAD" || paymentMethod == "BANK") {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = trxIdStr,
                            onValueChange = { trxIdStr = it },
                            label = { Text("ট্রানজেকশন আইডি (TrxID) / চেক নং", color = colors.textMuted) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_trx_id"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.surfaceElevated,
                                unfocusedContainerColor = colors.surfaceElevated,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Due Summary
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (dueAmount > 0) colors.badgeDangerBg else colors.badgeSuccessBg)
                            .border(1.dp, if (dueAmount > 0) colors.badgeDangerText.copy(alpha = 0.3f) else colors.badgeSuccessText.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (dueAmount > 0) "অবশিষ্ট বকেয়া (দেনাদার খাতা):" else "সম্পূর্ণ পরিশোধিত (ক্যাশ জমা):",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = BanglaNumberUtils.formatCurrency(if (dueAmount > 0) dueAmount else totalAmount),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (dueAmount > 0) colors.badgeDangerText else colors.badgeSuccessText
                        )
                    }
                }
            }

            // Submit Button
            Button(
                onClick = {
                    if (selectedProduct == null) {
                        scope.launch { snackbarHostState.showSnackbar("অনুগ্রহ করে চাল নির্বাচন করুন") }
                        return@Button
                    }
                    if (bagCount <= 0 || ratePerBag <= 0) {
                        scope.launch { snackbarHostState.showSnackbar("বস্তা এবং দর সঠিকভাবে দিন") }
                        return@Button
                    }
                    if (isSaleMode && bagCount > selectedProduct!!.currentBags) {
                        scope.launch { snackbarHostState.showSnackbar("গোডাউনে পর্যাপ্ত বস্তা নেই") }
                        return@Button
                    }
                    if (isSaleMode && selectedCustomer == null) {
                        scope.launch { snackbarHostState.showSnackbar("অনুগ্রহ করে ক্রেতা নির্বাচন করুন") }
                        return@Button
                    }
                    if ((isConsignment || !isSaleMode) && selectedMiller == null) {
                        scope.launch { snackbarHostState.showSnackbar("অনুগ্রহ করে মিলার নির্বাচন করুন") }
                        return@Button
                    }

                    val tx = TransactionEntity(
                        type = if (isSaleMode) "SALE" else "PURCHASE",
                        tradeType = if (isConsignment) "CONSIGNMENT" else "OWN_STOCK",
                        customerId = if (isSaleMode) selectedCustomer?.id else null,
                        customerName = if (isSaleMode) (selectedCustomer?.name ?: "") else "",
                        supplierId = if (isConsignment || !isSaleMode) selectedMiller?.id else null,
                        supplierName = if (isConsignment || !isSaleMode) (selectedMiller?.name ?: "") else "",
                        productId = selectedProduct!!.id,
                        productName = selectedProduct!!.name,
                        bagCount = bagCount,
                        bagSizeKg = selectedProduct!!.bagSizeKg,
                        ratePerBag = ratePerBag,
                        totalAmount = totalAmount,
                        commissionPerBag = commissionPerBag,
                        totalCommission = totalCommission,
                        laborCost = laborCost,
                        netPayableToMiller = netPayableToMiller,
                        paidAmount = paidAmount,
                        dueAmount = balanceDelta,
                        paymentMethod = paymentMethod,
                        trxId = trxIdStr,
                        truckNo = truckNoStr,
                        notes = notesStr
                    )

                    viewModel.executeTransaction(tx, emptyBagsGiven) { savedTx ->
                        savedTransactionForMemo = savedTx
                        scope.launch {
                            snackbarHostState.showSnackbar("চালান ও ৪টি খাতা সফলভাবে সংরক্ষিত হয়েছে!")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_transaction_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                )
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "চালান সংরক্ষণ ও ৪ খাতা আপডেট করুন",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Memo popup
    savedTransactionForMemo?.let { tx ->
        ChalanMemoDialog(
            transaction = tx,
            onDismiss = {
                savedTransactionForMemo = null
                onNavigateBack()
            }
        )
    }

    if (showAddProductDialog) {
        Dialog(onDismissRequest = { showAddProductDialog = false }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("নতুন চালের জাত যোগ করুন", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.textPrimary)
                        IconButton(onClick = { showAddProductDialog = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "বন্ধ", tint = colors.textMuted)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newProdName,
                        onValueChange = { newProdName = it },
                        label = { Text("চালের নাম", color = colors.textMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newProdCategory,
                        onValueChange = { newProdCategory = it },
                        label = { Text("চালের ধরন (যেমন: চিকন চাল)", color = colors.textMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = newProdBagSize == "50",
                            onClick = { newProdBagSize = "50" },
                            label = { Text("৫০ কেজি", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primaryContainer,
                                selectedLabelColor = colors.primary
                            )
                        )
                        FilterChip(
                            selected = newProdBagSize == "25",
                            onClick = { newProdBagSize = "25" },
                            label = { Text("২৫ কেজি", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primaryContainer,
                                selectedLabelColor = colors.primary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newProdOpeningBags,
                        onValueChange = { newProdOpeningBags = it },
                        label = { Text("শুরুর স্টক বস্তা", color = colors.textMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border
                        )
                    )
                    val openingBagsPreview = BanglaNumberUtils.toEnglishDigits(newProdOpeningBags).toIntOrNull() ?: 0
                    if (openingBagsPreview > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OptionalMillerField(
                            parties = parties,
                            selectedMiller = newProdMiller,
                            onMillerSelected = { newProdMiller = it }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newProdSaleRate,
                            onValueChange = { newProdSaleRate = it },
                            label = { Text("বিক্রি দর", color = colors.textMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border
                            )
                        )
                        OutlinedTextField(
                            value = newProdPurchaseRate,
                            onValueChange = { newProdPurchaseRate = it },
                            label = { Text("ক্রয় দর", color = colors.textMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            if (newProdName.isBlank()) return@Button
                            val bags = BanglaNumberUtils.toEnglishDigits(newProdOpeningBags).toIntOrNull() ?: 0
                            val sale = BanglaNumberUtils.toEnglishDigits(newProdSaleRate).toDoubleOrNull() ?: 0.0
                            val purchase = BanglaNumberUtils.toEnglishDigits(newProdPurchaseRate).toDoubleOrNull() ?: 0.0
                            val bagKg = newProdBagSize.toIntOrNull() ?: 50
                            viewModel.addProduct(
                                name = newProdName.trim(),
                                category = newProdCategory.ifBlank { "চিকন চাল" },
                                bagSizeKg = bagKg,
                                openingBags = bags,
                                saleRate = sale,
                                purchaseRate = purchase,
                                supplierId = if (bags > 0) newProdMiller?.id ?: 0L else 0L,
                                supplierName = if (bags > 0) newProdMiller?.name.orEmpty() else ""
                            ) { id ->
                                selectedProduct = ProductEntity(
                                    id = id,
                                    name = newProdName.trim(),
                                    category = newProdCategory.ifBlank { "চিকন চাল" },
                                    bagSizeKg = bagKg,
                                    currentBags = bags,
                                    defaultSaleRate = sale,
                                    defaultPurchaseRate = purchase
                                )
                                ratePerBagStr = if (isSaleMode) sale.toInt().toString() else purchase.toInt().toString()
                                showAddProductDialog = false
                                newProdName = ""
                                newProdOpeningBags = "0"
                                newProdSaleRate = "0"
                                newProdPurchaseRate = "0"
                                newProdMiller = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)
                    ) {
                        Text("সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showAddPartyDialog) {
        Dialog(onDismissRequest = { showAddPartyDialog = false }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (addPartyAsMiller) "নতুন মিলার যোগ করুন" else "নতুন খরিদ্দার যোগ করুন",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colors.textPrimary
                        )
                        IconButton(onClick = { showAddPartyDialog = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "বন্ধ", tint = colors.textMuted)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TradeTypeChoiceCard(
                            title = "খরিদ্দার",
                            subtitle = "যার কাছে বাকি পাব",
                            selected = newPartyType == "CUSTOMER",
                            selectedBg = colors.badgeSuccessBg,
                            selectedBorder = colors.badgeSuccessText,
                            selectedText = colors.badgeSuccessText,
                            onClick = { newPartyType = "CUSTOMER" },
                            modifier = Modifier.weight(1f)
                        )
                        TradeTypeChoiceCard(
                            title = "মিলার",
                            subtitle = "যাকে টাকা দিতে হবে",
                            selected = newPartyType == "SUPPLIER",
                            selectedBg = colors.badgeBlueBg,
                            selectedBorder = colors.badgeBlueText,
                            selectedText = colors.badgeBlueText,
                            onClick = { newPartyType = "SUPPLIER" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newPartyName,
                        onValueChange = { newPartyName = it },
                        label = { Text("দোকান / পার্টির নাম", color = colors.textMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPartyPhone,
                        onValueChange = { newPartyPhone = it },
                        label = { Text("মোবাইল নম্বর", color = colors.textMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPartyAddress,
                        onValueChange = { newPartyAddress = it },
                        label = { Text("বাজারের ঠিকানা", color = colors.textMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border
                        )
                    )
                    if (newPartyType == "CUSTOMER") {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPartyCreditLimit,
                            onValueChange = { newPartyCreditLimit = it },
                            label = { Text("সর্বোচ্চ বাকি (০ = সীমা নেই)", color = colors.textMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border
                            )
                        )
                        Text(
                            text = "এত টাকার বেশি বাকি হলে সতর্কবার্তা দেখাবে।",
                            fontSize = 11.sp,
                            color = colors.textMuted,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPartyEmptyBags,
                            onValueChange = { newPartyEmptyBags = it },
                            label = { Text("খালি বস্তা পাওনা (যদি থাকে)", color = colors.textMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border
                            )
                        )
                        Text(
                            text = "ক্রেতাকে খালি চটের বস্তা দিলে এখানে লিখুন।",
                            fontSize = 11.sp,
                            color = colors.textMuted,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            if (newPartyName.isBlank()) return@Button
                            val limit = BanglaNumberUtils.toEnglishDigits(newPartyCreditLimit).toDoubleOrNull() ?: 0.0
                            val bags = BanglaNumberUtils.toEnglishDigits(newPartyEmptyBags).toIntOrNull() ?: 0
                            viewModel.addParty(
                                name = newPartyName.trim(),
                                phone = newPartyPhone.trim(),
                                address = newPartyAddress.trim(),
                                type = newPartyType,
                                openingBalance = 0.0,
                                creditLimit = if (newPartyType == "CUSTOMER") limit else 0.0,
                                emptyBagsDue = if (newPartyType == "CUSTOMER") bags else 0
                            ) { id ->
                                val party = PartyEntity(
                                    id = id,
                                    name = newPartyName.trim(),
                                    phone = newPartyPhone.trim(),
                                    address = newPartyAddress.trim(),
                                    type = newPartyType,
                                    balance = 0.0,
                                    creditLimit = if (newPartyType == "CUSTOMER") limit else 0.0,
                                    emptyBagsDue = if (newPartyType == "CUSTOMER") bags else 0
                                )
                                if (newPartyType == "SUPPLIER") {
                                    selectedMiller = party
                                } else {
                                    selectedCustomer = party
                                }
                                showAddPartyDialog = false
                                newPartyName = ""
                                newPartyPhone = ""
                                newPartyAddress = ""
                                newPartyCreditLimit = "0"
                                newPartyEmptyBags = "0"
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)
                    ) {
                        Text("সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TradeTypeChoiceCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    selectedBg: Color,
    selectedBorder: Color,
    selectedText: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalNanoColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) selectedBg else colors.surfaceElevated)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) selectedBorder else colors.border,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = if (selected) selectedText else colors.textPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontSize = 11.sp,
            color = if (selected) selectedText.copy(alpha = 0.9f) else colors.textMuted
        )
    }
}
