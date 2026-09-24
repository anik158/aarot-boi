package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.PartyEntity
import com.example.data.local.entity.PaymentEntity
import com.example.ui.theme.LocalNanoColors
import com.example.util.BanglaNumberUtils
import com.example.viewmodel.AratdarViewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartiesScreen(
    viewModel: AratdarViewModel
) {
    val colors = LocalNanoColors.current
    val parties by viewModel.parties.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf("ALL") } // ALL, CUSTOMER, SUPPLIER
    var searchQuery by remember { mutableStateOf("") }

    // Dialog for recording quick payment
    var paymentTargetParty by remember { mutableStateOf<PartyEntity?>(null) }
    var payAmountStr by remember { mutableStateOf("") }
    var payMethod by remember { mutableStateOf("CASH") }
    var payTrxId by remember { mutableStateOf("") }
    var payNote by remember { mutableStateOf("") }

    // Dialog for adding new party
    var showAddPartyDialog by remember { mutableStateOf(false) }
    var newPartyName by remember { mutableStateOf("") }
    var newPartyPhone by remember { mutableStateOf("") }
    var newPartyAddress by remember { mutableStateOf("") }
    var newPartyType by remember { mutableStateOf("CUSTOMER") }
    var newPartyOpeningBalance by remember { mutableStateOf("") }
    var newPartyCreditLimit by remember { mutableStateOf("") }
    var newPartyEmptyBags by remember { mutableStateOf("") }

    val filteredParties = parties.filter { party ->
        val matchesTab = when (selectedTab) {
            "CUSTOMER" -> party.type == "CUSTOMER" || party.type == "BOTH" || party.balance > 0
            "SUPPLIER" -> party.type == "SUPPLIER" || party.type == "BOTH" || party.balance < 0
            else -> true
        }
        val matchesSearch = party.name.contains(searchQuery, ignoreCase = true) ||
                party.phone.contains(searchQuery, ignoreCase = true) ||
                party.address.contains(searchQuery, ignoreCase = true)
        matchesTab && matchesSearch
    }

    val totalCustomerDue = parties.filter { it.balance > 0 }.sumOf { it.balance }
    val totalMillerPayable = parties.filter { it.balance < 0 }.sumOf { abs(it.balance) }

    Scaffold(
        containerColor = colors.bg,
        topBar = {
            Surface(
                color = colors.headerBg,
                shadowElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.borderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "দেনা-পাওনা খাতা",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = colors.headerText
                        )
                        Text(
                            text = "বাকি পাওনা: ${BanglaNumberUtils.formatCurrency(totalCustomerDue)} • মিলার পাবে: ${BanglaNumberUtils.formatCurrency(totalMillerPayable)}",
                            fontSize = 11.sp,
                            color = colors.headerSubtext
                        )
                    }

                    Button(
                        onClick = { showAddPartyDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_party_action")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("নতুন পার্টি", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .testTag("search_party_input"),
                placeholder = { Text("পার্টির নাম, ফোন বা ঠিকানা দিয়ে খুঁজুন...", color = colors.textMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colors.textMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.borderSubtle,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                ),
                shape = RoundedCornerShape(14.dp)
            )

            // Filter Tabs (Screenshot 2 Rounded Pill Style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ALL" to "সকল খাতা (${BanglaNumberUtils.toBanglaDigits(parties.size)})",
                    "CUSTOMER" to "দেনাদার (বাকি পাওনা)",
                    "SUPPLIER" to "পাওনাদার (মিলার)"
                ).forEach { (tabKey, title) ->
                    val isSelected = selectedTab == tabKey
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTab = tabKey },
                        label = { Text(title, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.navPillActive,
                            selectedLabelColor = colors.primary,
                            containerColor = colors.surface,
                            labelColor = colors.textSecondary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredParties) { party ->
                    CyberPartyCard(
                        party = party,
                        context = context,
                        onReceivePaymentClick = {
                            paymentTargetParty = party
                            payAmountStr = if (party.balance > 0) party.balance.toInt().toString() else abs(party.balance).toInt().toString()
                            payMethod = "CASH"
                            payTrxId = ""
                            payNote = ""
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(70.dp))
                }
            }
        }
    }

    // Payment Dialog
    paymentTargetParty?.let { party ->
        Dialog(onDismissRequest = { paymentTargetParty = null }) {
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
                ) {
                    val isReceiving = party.balance > 0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isReceiving) "বকেয়া আদায়: ${party.name}" else "মিলারকে পরিশোধ: ${party.name}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colors.textPrimary
                        )
                        IconButton(onClick = { paymentTargetParty = null }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "বন্ধ", tint = colors.textMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isReceiving) "বর্তমান বকেয়া: ${BanglaNumberUtils.formatCurrency(party.balance)}" else "মিলার মোট পাবে: ${BanglaNumberUtils.formatCurrency(abs(party.balance))}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isReceiving) colors.badgeDangerText else colors.badgeOrangeText
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = payAmountStr,
                        onValueChange = { payAmountStr = it },
                        label = { Text("জমার পরিমাণ (টাকা)", color = colors.textMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pay_amount_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("CASH" to "নগদ", "BKASH" to "বিকাশ", "NAGAD" to "নগদ", "BANK" to "ব্যাংক").forEach { (method, label) ->
                            FilterChip(
                                selected = payMethod == method,
                                onClick = { payMethod = method },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.primaryContainer,
                                    selectedLabelColor = colors.primary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (payMethod != "CASH") {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = payTrxId,
                            onValueChange = { payTrxId = it },
                            label = { Text("TrxID / রেফারেন্স নং", color = colors.textMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = payNote,
                        onValueChange = { payNote = it },
                        label = { Text("মন্তব্য (ঐচ্ছিক)", color = colors.textMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val amount = BanglaNumberUtils.toEnglishDigits(payAmountStr).toDoubleOrNull() ?: 0.0
                            if (amount > 0) {
                                val payment = PaymentEntity(
                                    partyId = party.id,
                                    partyName = party.name,
                                    type = if (isReceiving) "RECEIVED" else "PAID",
                                    amount = amount,
                                    paymentMethod = payMethod,
                                    trxId = payTrxId,
                                    note = payNote.ifEmpty { if (isReceiving) "বকেয়া আদায়" else "মিলারকে পরিশোধ" }
                                )
                                viewModel.recordPayment(payment) {
                                    paymentTargetParty = null
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("confirm_payment_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)
                    ) {
                        Text("পেমেন্ট নিশ্চিত করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Add Party Dialog
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
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "নতুন পার্টি তৈরি",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colors.textPrimary
                        )
                        IconButton(onClick = { showAddPartyDialog = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "বন্ধ", tint = colors.textMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = newPartyType == "CUSTOMER",
                            onClick = { newPartyType = "CUSTOMER" },
                            label = { Text("খরিদ্দার / দেনাদার", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primaryContainer,
                                selectedLabelColor = colors.primary
                            )
                        )
                        FilterChip(
                            selected = newPartyType == "SUPPLIER",
                            onClick = { newPartyType = "SUPPLIER" },
                            label = { Text("মিলার / পাওনাদার", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primaryContainer,
                                selectedLabelColor = colors.primary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newPartyName,
                        onValueChange = { newPartyName = it },
                        label = { Text("দোকান / পার্টির নাম", color = colors.textMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_party_name_input"),
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
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newPartyCreditLimit,
                            onValueChange = { newPartyCreditLimit = it },
                            label = { Text("ক্রেডিট লিমিট (৳)", color = colors.textMuted) },
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
                            value = newPartyEmptyBags,
                            onValueChange = { newPartyEmptyBags = it },
                            label = { Text("খালি বস্তা পাওনা", color = colors.textMuted) },
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
                            if (newPartyName.isNotBlank()) {
                                val openBal = BanglaNumberUtils.toEnglishDigits(newPartyOpeningBalance).toDoubleOrNull() ?: 0.0
                                val balanceAdjusted = if (newPartyType == "CUSTOMER") openBal else -openBal
                                val limit = BanglaNumberUtils.toEnglishDigits(newPartyCreditLimit).toDoubleOrNull() ?: 0.0
                                val bags = BanglaNumberUtils.toEnglishDigits(newPartyEmptyBags).toIntOrNull() ?: 0

                                viewModel.addParty(
                                    name = newPartyName,
                                    phone = newPartyPhone,
                                    address = newPartyAddress,
                                    type = newPartyType,
                                    openingBalance = balanceAdjusted,
                                    creditLimit = limit,
                                    emptyBagsDue = bags,
                                    onSuccess = { showAddPartyDialog = false }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("confirm_add_party_button"),
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
fun CyberPartyCard(
    party: PartyEntity,
    context: Context,
    onReceivePaymentClick: () -> Unit
) {
    val colors = LocalNanoColors.current
    val isDue = party.balance > 0
    val isPayable = party.balance < 0

    fun makeCall(phone: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        context.startActivity(intent)
    }

    fun sendWhatsAppReminder(party: PartyEntity) {
        val msg = "আসসালামু আলাইকুম ${party.name},\nমেসার্স চালের আরতে আপনার বর্তমান বকেয়া পাওনা ${BanglaNumberUtils.formatCurrency(party.balance)}। অনুগ্রহ করে বকেয়া পরিশোধে সহযোগিতা করবেন।\nধন্যবাদান্তে,\nমেসার্স চালের আরত।"
        val cleanPhone = party.phone.replace("-", "").replace(" ", "").trim()
        val formatted = if (cleanPhone.startsWith("0")) "88$cleanPhone" else cleanPhone
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formatted&text=${Uri.encode(msg)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, colors.border, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDue) colors.badgeDangerBg else colors.primaryContainer)
                            .border(1.dp, if (isDue) colors.badgeDangerText.copy(alpha = 0.3f) else colors.primary.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (isDue) colors.badgeDangerText else colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = party.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${if (party.type == "CUSTOMER") "খরিদ্দার" else "মিলার"} • ${party.phone}",
                            fontSize = 11.sp,
                            color = colors.textMuted
                        )
                    }
                }

                // Balance Pill
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = when {
                            isDue -> BanglaNumberUtils.formatCurrency(party.balance)
                            isPayable -> BanglaNumberUtils.formatCurrency(abs(party.balance))
                            else -> "পরিশোধিত"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = when {
                            isDue -> colors.badgeDangerText
                            isPayable -> colors.badgeOrangeText
                            else -> colors.badgeSuccessText
                        }
                    )
                    Text(
                        text = when {
                            isDue -> "বকেয়া পাওনা"
                            isPayable -> "মিলার পাবে"
                            else -> "ব্যালেন্স শূন্য"
                        },
                        fontSize = 10.sp,
                        color = colors.textMuted
                    )
                }
            }

            // Credit Limit and Empty Bags pills
            if (party.creditLimit > 0 || party.emptyBagsDue > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (party.creditLimit > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.surfaceElevated)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("লিমিট: ${BanglaNumberUtils.formatCurrency(party.creditLimit)}", fontSize = 10.sp, color = colors.textSecondary)
                        }
                    }
                    if (party.emptyBagsDue > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.badgeAmberBg)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("খালি বস্তা পাওনা: ${BanglaNumberUtils.toBanglaDigits(party.emptyBagsDue)} টি", fontSize = 10.sp, color = colors.badgeAmberText)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = colors.borderSubtle)
            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Call, WhatsApp, Pay
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { makeCall(party.phone) },
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceElevated, contentColor = colors.textPrimary)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("কল", fontSize = 12.sp)
                }

                if (isDue) {
                    Button(
                        onClick = { sendWhatsAppReminder(party) },
                        modifier = Modifier.weight(1.2f).height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.badgeSuccessBg, contentColor = colors.badgeSuccessText)
                    ) {
                        Text("WhatsApp তাগাদা", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Button(
                    onClick = onReceivePaymentClick,
                    modifier = Modifier.weight(1.3f).height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isDue) "বাকি আদায়" else "টাকা দিন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
