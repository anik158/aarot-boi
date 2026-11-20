package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentLate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.TransactionEntity
import com.example.ui.components.ChalanMemoDialog
import com.example.ui.components.DeviceBindingDialog
import com.example.ui.theme.LocalNanoColors
import com.example.util.AppCredits
import com.example.util.BanglaNumberUtils
import com.example.viewmodel.AratdarViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AratdarViewModel,
    onNavigateToChalan: () -> Unit,
    onNavigateToStock: () -> Unit,
    onNavigateToCash: () -> Unit,
    onNavigateToParties: () -> Unit
) {
    val colors = LocalNanoColors.current
    val metrics by viewModel.metrics.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val bindingState by viewModel.bindingState.collectAsState()

    var selectedTransactionForMemo by remember { mutableStateOf<TransactionEntity?>(null) }
    var showDeviceBindingDialog by remember { mutableStateOf(false) }
    var showSalesChartDialog by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isNarrow = maxWidth < 380.dp
        val headerTitleSize = if (isNarrow) 16.sp else 18.sp
        val metricTitleSize = if (isNarrow) 13.sp else 14.sp
        val metricValueSize = if (isNarrow) 20.sp else 22.sp
        val metricCardHeight = if (isNarrow) 132.dp else 140.dp

        val last7DaySales = remember(transactions) { buildLast7DaySales(transactions) }

        val pulse = rememberInfiniteTransition(label = "fab_pulse")
        val fabScale by pulse.animateFloat(
            initialValue = 1f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(900),
                repeatMode = RepeatMode.Reverse
            ),
            label = "fab_scale"
        )
        val fabAlpha by pulse.animateFloat(
            initialValue = 0.88f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(900),
                repeatMode = RepeatMode.Reverse
            ),
            label = "fab_alpha"
        )

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
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .clickable { showDeviceBindingDialog = true }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.primaryContainer)
                                    .border(1.dp, colors.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "আরতবই • ${bindingState.shopName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = headerTitleSize,
                                    color = colors.headerText,
                                    maxLines = 1
                                )
                                Text(
                                    text = "ডিজিটাল পাইকারি স্টক ও ৪-ইন-১ খাতা",
                                    fontSize = 11.sp,
                                    color = colors.headerSubtext,
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Device Binding & Offline Grace pill (48dp interactive component)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(colors.badgeSuccessBg)
                                .border(1.dp, colors.badgeSuccessText.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                .clickable { showDeviceBindingDialog = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(colors.badgeSuccessText)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "সক্রিয়",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.badgeSuccessText
                                )
                            }
                        }
                    }
                }
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToChalan,
                    icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp), tint = colors.onPrimary) },
                    text = { Text("নতুন চালান", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.onPrimary) },
                    containerColor = colors.primary.copy(alpha = fabAlpha),
                    contentColor = colors.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .scale(fabScale)
                        .testTag("fab_new_chalan")
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "ব্যবসার সারসংক্ষেপ",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(metricCardHeight),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CyberMetricCard(
                                title = "মোট মজুদ চাল",
                                value = "${BanglaNumberUtils.toBanglaDigits(metrics.totalStockBags)} বস্তা",
                                subtext = "~${BanglaNumberUtils.toBanglaDigits(metrics.totalStockMon.toInt())} মণ",
                                icon = Icons.Default.Inventory2,
                                badgeColor = colors.badgeSuccessBg,
                                accentColor = colors.badgeSuccessText,
                                titleSize = metricTitleSize,
                                valueSize = metricValueSize,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { onNavigateToStock() }
                            )

                            CyberMetricCard(
                                title = "আজকের মোট বিক্রি",
                                value = BanglaNumberUtils.formatCurrency(metrics.todaySalesAmount),
                                subtext = "${BanglaNumberUtils.toBanglaDigits(metrics.todaySalesBags)} বস্তা • ট্যাপ করে গ্রাফ",
                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                                badgeColor = colors.primaryContainer,
                                accentColor = colors.primary,
                                titleSize = metricTitleSize,
                                valueSize = metricValueSize,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { showSalesChartDialog = true }
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(metricCardHeight),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CyberMetricCard(
                                title = "বাজারে মোট বাকি",
                                value = BanglaNumberUtils.formatCurrency(metrics.totalCustomerDue),
                                subtext = "দেনাদারদের কাছে পাওনা",
                                icon = Icons.Default.AssignmentLate,
                                badgeColor = colors.badgeDangerBg,
                                accentColor = colors.badgeDangerText,
                                titleSize = metricTitleSize,
                                valueSize = metricValueSize,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { onNavigateToParties() }
                            )

                            CyberMetricCard(
                                title = "ক্যাশ বাক্সে নগদ",
                                value = BanglaNumberUtils.formatCurrency(metrics.cashInHand),
                                subtext = "হাতে থাকা ক্যাশ স্থিতি",
                                icon = Icons.Default.AccountBalanceWallet,
                                badgeColor = colors.badgeAmberBg,
                                accentColor = colors.badgeAmberText,
                                titleSize = metricTitleSize,
                                valueSize = metricValueSize,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { onNavigateToCash() }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "সাম্প্রতিক চালান",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textMuted
                        )
                        Text(
                            text = "${BanglaNumberUtils.toBanglaDigits(transactions.size)} টি",
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }
                }

                // Transactions List
                if (transactions.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                    contentDescription = null,
                                    tint = colors.textMuted,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "এখনো কোনো চালান তৈরি করা হয়নি",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = colors.textSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "নিচের 'নতুন চালান' বাটনে চাপ দিয়ে এন্ট্রি শুরু করুন",
                                    fontSize = 12.sp,
                                    color = colors.textMuted
                                )
                            }
                        }
                    }
                } else {
                    items(transactions.take(6)) { tx ->
                        CyberTransactionItemCard(
                            transaction = tx,
                            isNarrow = isNarrow,
                            onClick = { selectedTransactionForMemo = tx }
                        )
                    }
                }

                item {
                    Text(
                        text = AppCredits.LINE,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 72.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                }
            }
        }

        // Memo popup dialog
        selectedTransactionForMemo?.let { tx ->
            ChalanMemoDialog(
                transaction = tx,
                onDismiss = { selectedTransactionForMemo = null }
            )
        }

        // Device Binding dialog
        if (showDeviceBindingDialog) {
            DeviceBindingDialog(
                bindingState = bindingState,
                onDismiss = { showDeviceBindingDialog = false },
                onSave = { name, owner, phone, labor ->
                    viewModel.updateRegistration(name, owner, phone, bindingState.googleAccount, labor)
                }
            )
        }

        if (showSalesChartDialog) {
            Dialog(onDismissRequest = { showSalesChartDialog = false }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "আজকের বিক্রি",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = BanglaNumberUtils.formatCurrency(metrics.todaySalesAmount),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = colors.primary
                                )
                            }
                            IconButton(onClick = { showSalesChartDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "বন্ধ", tint = colors.textMuted)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${BanglaNumberUtils.toBanglaDigits(metrics.todaySalesBags)} বস্তা বিক্রি হয়েছে",
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "গত ৭ দিনের বিক্রি",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        SevenDaySalesSparkline(
                            values = last7DaySales,
                            barColor = colors.primary,
                            trackColor = colors.surfaceElevated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showSalesChartDialog = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.onPrimary
                            )
                        ) {
                            Text("বুঝেছি", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun buildLast7DaySales(transactions: List<TransactionEntity>): List<Float> {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val todayStart = cal.timeInMillis
    val dayMs = 86_400_000L
    return (6 downTo 0).map { daysAgo ->
        val start = todayStart - daysAgo * dayMs
        val end = start + dayMs
        transactions
            .filter { it.type == "SALE" && it.date >= start && it.date < end }
            .sumOf { it.totalAmount }
            .toFloat()
    }
}

@Composable
fun CyberMetricCard(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    badgeColor: Color,
    accentColor: Color,
    titleSize: androidx.compose.ui.unit.TextUnit = 12.sp,
    valueSize: androidx.compose.ui.unit.TextUnit = 17.sp,
    modifier: Modifier = Modifier
) {
    val colors = LocalNanoColors.current

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        modifier = modifier.border(1.dp, colors.border, RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = titleSize,
                    color = colors.textSecondary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(badgeColor)
                        .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
            Column {
                Text(
                    text = value,
                    fontSize = valueSize,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtext,
                    fontSize = 12.sp,
                    color = colors.textMuted,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun SevenDaySalesSparkline(
    values: List<Float>,
    barColor: Color,
    trackColor: Color,
    modifier: Modifier = Modifier
) {
    val max = (values.maxOrNull() ?: 0f).coerceAtLeast(1f)
    Canvas(modifier = modifier) {
        val count = values.size.coerceAtLeast(1)
        val gap = 4.dp.toPx()
        val barWidth = (size.width - gap * (count - 1)) / count
        values.forEachIndexed { index, value ->
            val heightRatio = (value / max).coerceIn(0.08f, 1f)
            val barHeight = size.height * heightRatio
            val left = index * (barWidth + gap)
            val top = size.height - barHeight
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(left, 0f),
                size = Size(barWidth, size.height),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
            drawRoundRect(
                color = barColor,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}

@Composable
fun CyberTransactionItemCard(
    transaction: TransactionEntity,
    isNarrow: Boolean = false,
    onClick: () -> Unit
) {
    val colors = LocalNanoColors.current
    val isSale = transaction.type == "SALE"
    val isConsignment = transaction.tradeType == "CONSIGNMENT"
    val dateFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(transaction.date))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    isConsignment -> colors.badgeBlueBg
                                    isSale -> colors.badgeSuccessBg
                                    else -> colors.badgeOrangeBg
                                }
                            )
                            .border(
                                1.dp,
                                when {
                                    isConsignment -> colors.badgeBlueText.copy(alpha = 0.4f)
                                    isSale -> colors.badgeSuccessText.copy(alpha = 0.4f)
                                    else -> colors.badgeOrangeText.copy(alpha = 0.4f)
                                },
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = when {
                                isConsignment -> "কমিশন চালান"
                                isSale -> "নিজস্ব বিক্রি"
                                else -> "ক্রয় চালান"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isConsignment -> colors.badgeBlueText
                                isSale -> colors.badgeSuccessText
                                else -> colors.badgeOrangeText
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSale) transaction.customerName.ifEmpty { "নগদ খরিদ্দার" } else transaction.supplierName,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isNarrow) 13.sp else 14.sp,
                        color = colors.textPrimary,
                        maxLines = 1
                    )
                }

                Text(
                    text = BanglaNumberUtils.formatCurrency(transaction.totalAmount),
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isNarrow) 14.sp else 15.sp,
                    color = colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${transaction.productName} • ${BanglaNumberUtils.toBanglaDigits(transaction.bagCount)} বস্তা (${BanglaNumberUtils.formatCurrency(transaction.ratePerBag)}/বস্তা)",
                    fontSize = if (isNarrow) 11.sp else 12.sp,
                    color = colors.textSecondary
                )
                Text(
                    text = dateFormatted,
                    fontSize = 10.sp,
                    color = colors.textMuted
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = colors.borderSubtle, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (transaction.dueAmount > 0) {
                    Text(
                        text = "বকেয়া: ${BanglaNumberUtils.formatCurrency(transaction.dueAmount)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.badgeDangerText
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = colors.badgeSuccessText,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "সম্পূর্ণ পরিশোধিত",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.badgeSuccessText
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "মেমো দেখুন",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.primary
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
