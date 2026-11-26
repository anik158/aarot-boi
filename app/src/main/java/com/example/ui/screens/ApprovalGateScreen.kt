package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppSize
import com.example.ui.theme.AppText
import com.example.ui.theme.LocalNanoColors
import com.example.util.AppCredits
import com.example.viewmodel.AratdarViewModel
import kotlinx.coroutines.delay

@Composable
fun ApprovalGateScreen(viewModel: AratdarViewModel) {
    val colors = LocalNanoColors.current
    val binding by viewModel.bindingState.collectAsState()
    val message by viewModel.gateMessage.collectAsState()
    var shopName by remember { mutableStateOf(binding.shopName) }
    var ownerName by remember { mutableStateOf(binding.proprietorName) }
    var phone by remember { mutableStateOf(binding.phone) }
    var editing by remember { mutableStateOf(binding.shopName.isBlank()) }

    LaunchedEffect(binding.shopName, binding.isActivated, editing) {
        if (editing || binding.isActivated || binding.shopName.isBlank()) return@LaunchedEffect
        while (true) {
            viewModel.refreshLicense()
            delay(5000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "আরতবই",
            fontSize = AppText.Hero,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (editing) "এই ফোনটি ব্যবহারের অনুমতি চাইতে আড়তের তথ্য দিন।" else "অনুমোদনের অপেক্ষায়",
            fontSize = AppText.BodyLg,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))

        if (editing) {
            GateField(shopName, { shopName = it }, "আড়তের নাম")
            Spacer(modifier = Modifier.height(10.dp))
            GateField(ownerName, { ownerName = it }, "মালিকের নাম")
            Spacer(modifier = Modifier.height(10.dp))
            GateField(phone, { phone = it }, "মোবাইল নম্বর", KeyboardType.Phone)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (shopName.isBlank() || phone.isBlank()) return@Button
                    editing = false
                    viewModel.requestAccess(shopName, ownerName, phone)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = AppSize.Button),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)
            ) {
                Text("অনুমতি চাই", fontWeight = FontWeight.Bold)
            }
        } else {
            Text(
                text = binding.shopName,
                fontSize = AppText.TitleLg,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "ডিভাইস আইডি: ${binding.deviceId}",
                fontSize = AppText.Small,
                color = colors.textMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "অনুমোদন হলে এই পাতা নিজে থেকে খুলবে।",
                fontSize = AppText.Body,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.requestAccess(binding.shopName, binding.proprietorName, binding.phone) },
                modifier = Modifier.fillMaxWidth().heightIn(min = AppSize.Button),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)
            ) {
                Text("আবার চেষ্টা করুন", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { editing = true },
                modifier = Modifier.fillMaxWidth().heightIn(min = AppSize.Button),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceElevated, contentColor = colors.textPrimary)
            ) {
                Text("তথ্য বদলান")
            }
        }

        if (message.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = message, color = colors.badgeDangerText, fontSize = AppText.Body, textAlign = TextAlign.Center)
        }

        if (binding.licenseStatus == "revoked") {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "এই ফোনের অনুমতি বাতিল করা হয়েছে।",
                color = colors.badgeDangerText,
                fontSize = AppText.Body,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = AppCredits.LINE,
            fontSize = AppText.Small,
            color = colors.textMuted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboard: KeyboardType = KeyboardType.Text
) {
    val colors = LocalNanoColors.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = colors.textMuted) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.border
        )
    )
}
