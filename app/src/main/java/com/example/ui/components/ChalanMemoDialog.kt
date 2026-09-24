package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.TransactionEntity
import com.example.ui.theme.LocalAratdarColors
import com.example.util.BanglaNumberUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChalanMemoDialog(
    transaction: TransactionEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalAratdarColors.current
    val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(transaction.date))
    val isConsignment = transaction.tradeType == "CONSIGNMENT"

    fun shareMemo(ctx: Context) {
        val memoText = buildString {
            appendLine("🌾 মেসার্স চালের আরত")
            appendLine("চালান মেমো: #${transaction.id}")
            appendLine("তারিখ: $dateStr")
            appendLine("--------------------------------")
            if (transaction.type == "SALE") {
                appendLine("ক্রেতা: ${transaction.customerName.ifEmpty { "নগদ খরিদ্দার" }}")
            } else {
                appendLine("সরবরাহকারী: ${transaction.supplierName}")
            }
            if (isConsignment) {
                appendLine("চালানের ধরন: কমিশন চালান (মিলার: ${transaction.supplierName})")
            }
            appendLine("চাল: ${transaction.productName}")
            appendLine("পরিমাণ: ${BanglaNumberUtils.toBanglaDigits(transaction.bagCount)} বস্তা (${BanglaNumberUtils.toBanglaDigits(transaction.bagCount * transaction.bagSizeKg)} কেজি)")
            appendLine("দর: ${BanglaNumberUtils.formatCurrency(transaction.ratePerBag)} / বস্তা")
            appendLine("মোট মূল্য: ${BanglaNumberUtils.formatCurrency(transaction.totalAmount)}")
            if (isConsignment) {
                appendLine("কমিশন কর্তন: ${BanglaNumberUtils.formatCurrency(transaction.totalCommission)}")
                appendLine("লেবার/খালাসি: ${BanglaNumberUtils.formatCurrency(transaction.laborCost)}")
                appendLine("মিলারকে প্রদেয়: ${BanglaNumberUtils.formatCurrency(transaction.netPayableToMiller)}")
            }
            appendLine("নগদ পরিশোধ: ${BanglaNumberUtils.formatCurrency(transaction.paidAmount)} (${transaction.paymentMethod})")
            if (transaction.dueAmount > 0) {
                appendLine("বকেয়া: ${BanglaNumberUtils.formatCurrency(transaction.dueAmount)}")
            }
            appendLine("--------------------------------")
            appendLine("ধন্যবাদান্তে, মেসার্স চালের আরত")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, memoText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "চালান মেমো শেয়ার করুন")
        ctx.startActivity(shareIntent)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = colors.badgeSuccessText,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "চালান সংরক্ষিত হয়েছে!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.badgeSuccessText
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ", tint = colors.textMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Paper Memo Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceElevated, RoundedCornerShape(12.dp))
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "মেসার্স চালের আরত",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "পাইকারি চালের আড়ত ও কমিশন এজেন্ট",
                            fontSize = 12.sp,
                            color = colors.textMuted,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "চালান #${BanglaNumberUtils.toBanglaDigits(transaction.id)}  •  $dateStr",
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            textAlign = TextAlign.Center
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = colors.borderSubtle)

                        // Party details
                        if (transaction.type == "SALE") {
                            MemoRow(label = "ক্রেতার নাম:", value = transaction.customerName.ifEmpty { "নগদ খরিদ্দার" }, isBold = true)
                        } else {
                            MemoRow(label = "মিলার/পার্টি:", value = transaction.supplierName.ifEmpty { "সাধারণ মিলার" }, isBold = true)
                        }

                        if (isConsignment) {
                            MemoRow(label = "চালানের ধরন:", value = "কমিশন চালান (মিলার: ${transaction.supplierName})", valueColor = colors.badgeBlueText)
                        } else {
                            MemoRow(label = "চালানের ধরন:", value = "নিজস্ব মজুদ থেকে বিক্রয়")
                        }

                        MemoRow(label = "চালের নাম:", value = transaction.productName)
                        MemoRow(
                            label = "পরিমাণ:",
                            value = "${BanglaNumberUtils.toBanglaDigits(transaction.bagCount)} বস্তা (${BanglaNumberUtils.toBanglaDigits(transaction.bagCount * transaction.bagSizeKg)} কেজি)"
                        )
                        MemoRow(label = "দর:", value = "${BanglaNumberUtils.formatCurrency(transaction.ratePerBag)} / বস্তা")

                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = colors.borderSubtle)

                        MemoRow(label = "মোট মূল্য:", value = BanglaNumberUtils.formatCurrency(transaction.totalAmount), isBold = true)

                        if (isConsignment) {
                            MemoRow(label = "আড়তের কমিশন (আয়):", value = "- ${BanglaNumberUtils.formatCurrency(transaction.totalCommission)}", valueColor = colors.badgeBlueText)
                            MemoRow(label = "লেবার/খালাসি কর্তন:", value = "- ${BanglaNumberUtils.formatCurrency(transaction.laborCost)}", valueColor = colors.textMuted)
                            MemoRow(label = "মিলারকে প্রদেয় নিট:", value = BanglaNumberUtils.formatCurrency(transaction.netPayableToMiller), isBold = true, valueColor = colors.badgeSuccessText)
                        }

                        MemoRow(label = "নগদ জমা:", value = "${BanglaNumberUtils.formatCurrency(transaction.paidAmount)} (${transaction.paymentMethod})", isBold = true, valueColor = colors.badgeSuccessText)

                        if (transaction.dueAmount > 0) {
                            MemoRow(label = "বকেয়া / বাকি:", value = BanglaNumberUtils.formatCurrency(transaction.dueAmount), isBold = true, valueColor = colors.badgeDangerText)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { shareMemo(context) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("মেমো শেয়ার", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary
                        )
                    ) {
                        Text("ঠিক আছে", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MemoRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    valueColor: Color? = null
) {
    val colors = LocalAratdarColors.current
    val resolvedColor = valueColor ?: colors.textPrimary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = colors.textSecondary)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = resolvedColor
        )
    }
}
