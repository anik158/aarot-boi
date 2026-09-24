package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payments
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.PaymentEntity
import com.example.ui.theme.LocalNanoColors
import com.example.util.BanglaNumberUtils
import com.example.viewmodel.AratdarViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(
    viewModel: AratdarViewModel
) {
    val colors = LocalNanoColors.current
    val payments by viewModel.payments.collectAsState()
    val metrics by viewModel.metrics.collectAsState()
    val denomination by viewModel.denomination.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, RECEIVED, PAID
    var showDenominationDialog by remember { mutableStateOf(false) }

    val filteredPayments = payments.filter {
        when (selectedFilter) {
            "RECEIVED" -> it.type == "RECEIVED"
            "PAID" -> it.type == "PAID"
            else -> true
        }
    }

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
                            text = "জমা-খরচ ও ক্যাশ রোকার",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = colors.headerText
                        )
                        Text(
                            text = "দৈনিক নগদ ও অনলাইন ট্রানজেকশন খাতা",
                            fontSize = 11.sp,
                            color = colors.headerSubtext
                        )
                    }

                    // Denomination counter button
                    Button(
                        onClick = { showDenominationDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.badgeAmberBg,
                            contentColor = colors.badgeAmberText
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("open_denomination_button")
                    ) {
                        Icon(Icons.Default.Money, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ক্যাশ মিলান", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
            // Cash Summary Card (Screenshot 3 Container Card Style)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ক্যাশ ড্রয়ারে মোট নগদ স্থিতি",
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = BanglaNumberUtils.formatCurrency(metrics.cashInHand),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.badgeAmberBg)
                            .border(1.dp, colors.badgeAmberText.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = colors.badgeAmberText,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ALL" to "সকল লেনদেন (${BanglaNumberUtils.toBanglaDigits(payments.size)})",
                    "RECEIVED" to "নগদ জমা",
                    "PAID" to "নগদ খরচ / প্রদান"
                ).forEach { (filterKey, label) ->
                    val isSelected = selectedFilter == filterKey
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filterKey },
                        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
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

            Spacer(modifier = Modifier.height(8.dp))

            // Payment Records List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredPayments) { payment ->
                    CyberPaymentItemCard(payment = payment)
                }

                item {
                    Spacer(modifier = Modifier.height(70.dp))
                }
            }
        }
    }

    // Denomination Reconciliation Dialog (রোকার / সান্ধ্যকালীন ক্যাশ মিলানো)
    if (showDenominationDialog) {
        Dialog(onDismissRequest = { showDenominationDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border, RoundedCornerShape(20.dp))
            ) {
                var c1000 by remember { mutableStateOf(denomination.count1000.toString()) }
                var c500 by remember { mutableStateOf(denomination.count500.toString()) }
                var c200 by remember { mutableStateOf(denomination.count200.toString()) }
                var c100 by remember { mutableStateOf(denomination.count100.toString()) }
                var c50 by remember { mutableStateOf(denomination.count50.toString()) }
                var c20 by remember { mutableStateOf(denomination.count20.toString()) }
                var c10 by remember { mutableStateOf(denomination.count10.toString()) }

                val n1000 = BanglaNumberUtils.toEnglishDigits(c1000).toIntOrNull() ?: 0
                val n500 = BanglaNumberUtils.toEnglishDigits(c500).toIntOrNull() ?: 0
                val n200 = BanglaNumberUtils.toEnglishDigits(c200).toIntOrNull() ?: 0
                val n100 = BanglaNumberUtils.toEnglishDigits(c100).toIntOrNull() ?: 0
                val n50 = BanglaNumberUtils.toEnglishDigits(c50).toIntOrNull() ?: 0
                val n20 = BanglaNumberUtils.toEnglishDigits(c20).toIntOrNull() ?: 0
                val n10 = BanglaNumberUtils.toEnglishDigits(c10).toIntOrNull() ?: 0

                val physicalTotal = (n1000 * 1000 + n500 * 500 + n200 * 200 + n100 * 100 + n50 * 50 + n20 * 20 + n10 * 10).toDouble()
                val diff = physicalTotal - metrics.cashInHand

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
                            text = "সান্ধ্যকালীন ক্যাশ মিলানো (রোকার)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colors.textPrimary
                        )
                        IconButton(onClick = { showDenominationDialog = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "বন্ধ", tint = colors.textMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DenomInputRow("১০০০", c1000, { c1000 = it }, Modifier.weight(1f))
                        DenomInputRow("৫০০", c500, { c500 = it }, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DenomInputRow("২০০", c200, { c200 = it }, Modifier.weight(1f))
                        DenomInputRow("১০০", c100, { c100 = it }, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DenomInputRow("৫০", c50, { c50 = it }, Modifier.weight(1f))
                        DenomInputRow("২০", c20, { c20 = it }, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DenomInputRow("১০", c10, { c10 = it }, Modifier.weight(1f))
                        Box(modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Reconciliation summary
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceElevated)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("হাতে গণনা করা ক্যাশ:", fontSize = 12.sp, color = colors.textSecondary)
                                Text(BanglaNumberUtils.formatCurrency(physicalTotal), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("সিস্টেম খাতার ক্যাশ:", fontSize = 12.sp, color = colors.textMuted)
                                Text(BanglaNumberUtils.formatCurrency(metrics.cashInHand), fontSize = 12.sp, color = colors.textMuted)
                            }
                            HorizontalDivider(color = colors.borderSubtle, modifier = Modifier.padding(vertical = 4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("পার্থক্য / গরমিল:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                Text(
                                    text = if (diff == 0.0) "সম্পূর্ণ মিলেছে (০ ৳)" else "${if (diff > 0) "+" else ""}${BanglaNumberUtils.formatCurrency(diff)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (diff == 0.0) colors.badgeSuccessText else colors.badgeOrangeText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.updateDenomination(n1000, n500, n200, n100, n50, n20, n10)
                            showDenominationDialog = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)
                    ) {
                        Text("ক্যাশ রোকার সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DenomInputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalNanoColors.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("৳$label নোট", fontSize = 11.sp, color = colors.textMuted) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.borderSubtle
        )
    )
}

@Composable
fun CyberPaymentItemCard(payment: PaymentEntity) {
    val colors = LocalNanoColors.current
    val isReceived = payment.type == "RECEIVED"
    val dateFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(payment.date))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, colors.border, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isReceived) colors.badgeSuccessBg else colors.badgeOrangeBg)
                        .border(1.dp, if (isReceived) colors.badgeSuccessText.copy(alpha = 0.3f) else colors.badgeOrangeText.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = if (isReceived) colors.badgeSuccessText else colors.badgeOrangeText,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = payment.partyName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "${payment.paymentMethod} • $dateFormatted${if (payment.trxId.isNotEmpty()) " (TrxID: ${payment.trxId})" else ""}",
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isReceived) "+" else "-"} ${BanglaNumberUtils.formatCurrency(payment.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isReceived) colors.badgeSuccessText else colors.badgeOrangeText
                )
                Text(
                    text = if (isReceived) "জমা হয়েছে" else "প্রদান করা হয়েছে",
                    fontSize = 10.sp,
                    color = colors.textMuted
                )
            }
        }
    }
}
