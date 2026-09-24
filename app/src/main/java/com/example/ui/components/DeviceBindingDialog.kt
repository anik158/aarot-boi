package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhonelinkLock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.LocalNanoColors
import com.example.util.BanglaNumberUtils
import com.example.util.DeviceBindingState

@Composable
fun DeviceBindingDialog(
    bindingState: DeviceBindingState,
    onDismiss: () -> Unit,
    onSave: (shopName: String, ownerName: String, phone: String) -> Unit
) {
    val colors = LocalNanoColors.current
    var shopName by remember { mutableStateOf(bindingState.shopName) }
    var ownerName by remember { mutableStateOf(bindingState.proprietorName) }
    var phone by remember { mutableStateOf(bindingState.phone) }
    var isEditing by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.border, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
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
                                .background(colors.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhonelinkLock,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ডিভাইস বাইন্ডিং ও লাইসেন্স",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "নিরাপদ অফলাইন আড়ত সিস্টেম",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ", tint = colors.textMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status pill row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.badgeSuccessBg)
                            .border(1.dp, colors.badgeSuccessText.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.badgeSuccessText, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("লাইসেন্স স্ট্যাটাস", fontSize = 10.sp, color = colors.textMuted)
                            }
                            Text(bindingState.licenseStatus, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.badgeSuccessText)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.badgeOrangeBg)
                            .border(1.dp, colors.badgeOrangeText.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(colors.badgeOrangeText))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("অফলাইন বাফার", fontSize = 10.sp, color = colors.textMuted)
                            }
                            Text("${BanglaNumberUtils.toBanglaDigits(bindingState.graceDaysRemaining)} দিন গ্রেস পিরিয়ড", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.badgeOrangeText)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Detail card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceElevated)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("হার্ডওয়্যার আইডি:", fontSize = 12.sp, color = colors.textMuted)
                            Text(bindingState.deviceId, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.primary)
                        }
                        HorizontalDivider(color = colors.borderSubtle)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("গুগল অ্যাকাউন্ট:", fontSize = 12.sp, color = colors.textMuted)
                            Text(bindingState.googleAccount, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                        }
                        HorizontalDivider(color = colors.borderSubtle)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("অফলাইন এক্সেস:", fontSize = 12.sp, color = colors.textMuted)
                            Text("নিরাপদ ও সক্রিয়", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.badgeSuccessText)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isEditing) {
                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text("আড়তের নাম", color = colors.textMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("মালিকের নাম", color = colors.textMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("মোবাইল নম্বর", color = colors.textMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isEditing = false },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceElevated),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("বাতিল", color = colors.textSecondary)
                        }
                        Button(
                            onClick = {
                                onSave(shopName, ownerName, phone)
                                isEditing = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("সংরক্ষণ", color = colors.onPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { isEditing = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.surfaceElevated,
                                contentColor = colors.textPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("তথ্য সম্পাদনা")
                        }
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.onPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("ঠিক আছে", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
