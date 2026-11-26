package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
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
import com.example.data.local.entity.PartyEntity
import com.example.data.local.entity.ProductEntity
import com.example.ui.components.OptionalMillerField
import com.example.ui.theme.AppIcon
import com.example.ui.theme.AppSize
import com.example.ui.theme.AppText
import com.example.ui.theme.LocalNanoColors
import com.example.util.BanglaNumberUtils
import com.example.viewmodel.AratdarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(
    viewModel: AratdarViewModel
) {
    val colors = LocalNanoColors.current
    val products by viewModel.products.collectAsState()
    val parties by viewModel.parties.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val stockLogs by viewModel.stockLogs.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Dialog state for Stock Adjustment
    var selectedProductForAdjust by remember { mutableStateOf<ProductEntity?>(null) }
    var adjustDeltaStr by remember { mutableStateOf("") }
    var isAddingBags by remember { mutableStateOf(true) }
    var adjustReasonStr by remember { mutableStateOf("") }
    var selectedIntakeMiller by remember { mutableStateOf<PartyEntity?>(null) }
    var productPendingDelete by remember { mutableStateOf<ProductEntity?>(null) }

    // Dialog state for adding new product
    var showAddProductDialog by remember { mutableStateOf(false) }
    var newProdName by remember { mutableStateOf("") }
    var newProdCategory by remember { mutableStateOf("চিকন চাল") }
    var newProdBagSize by remember { mutableStateOf("50") }
    var newProdOpeningBags by remember { mutableStateOf("") }
    var newProdSaleRate by remember { mutableStateOf("") }
    var newProdPurchaseRate by remember { mutableStateOf("") }
    var newProdMiller by remember { mutableStateOf<PartyEntity?>(null) }

    val filteredProducts = products.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true)
    }

    val totalBags = products.sumOf { it.currentBags }
    val totalMon = products.sumOf { it.totalMon }
    val totalValue = products.sumOf { it.currentBags * it.defaultSaleRate }

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
                            text = "মাল খাতা (গোডাউন স্টক)",
                            fontWeight = FontWeight.Bold,
                            fontSize = AppText.TitleLg,
                            color = colors.headerText
                        )
                        Text(
                            text = "মোট মজুদ: ${BanglaNumberUtils.toBanglaDigits(totalBags)} বস্তা (~${BanglaNumberUtils.toBanglaDigits(totalMon.toInt())} মণ) • মূল্য: ${BanglaNumberUtils.formatCurrency(totalValue)}",
                            fontSize = AppText.Caption,
                            color = colors.headerSubtext
                        )
                    }

                    Button(
                        onClick = { showAddProductDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_product_action")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(AppIcon.Sm))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("নতুন চাল", fontSize = AppText.Small, fontWeight = FontWeight.Bold)
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
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("search_stock_input"),
                placeholder = { Text("চালের নাম বা ক্যাটেগরি দিয়ে খুঁজুন...", color = colors.textMuted) },
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

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredProducts) { product ->
                    val lastMiller = stockLogs
                        .filter { it.productId == product.id && it.type == "IN" && it.supplierName.isNotBlank() }
                        .maxByOrNull { it.id }
                        ?.supplierName
                    CyberProductCard(
                        product = product,
                        lastMillerName = lastMiller,
                        onAdjustClick = {
                            selectedProductForAdjust = product
                            adjustDeltaStr = ""
                            adjustReasonStr = ""
                            isAddingBags = true
                            selectedIntakeMiller = null
                        },
                        onDeleteClick = { productPendingDelete = product }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(70.dp))
                }
            }
        }
    }

    // Stock Adjustment Dialog
    selectedProductForAdjust?.let { product ->
        Dialog(onDismissRequest = { selectedProductForAdjust = null }) {
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
                        .heightIn(max = 560.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "স্টক সমন্বয়: ${product.name}",
                            fontWeight = FontWeight.Bold,
                            fontSize = AppText.Title,
                            color = colors.textPrimary
                        )
                        IconButton(onClick = { selectedProductForAdjust = null }, modifier = Modifier.size(AppSize.ButtonSm)) {
                            Icon(Icons.Default.Close, contentDescription = "বন্ধ", tint = colors.textMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "বর্তমান মজুদ: ${BanglaNumberUtils.toBanglaDigits(product.currentBags)} বস্তা (~${BanglaNumberUtils.toBanglaDigits(product.totalMon.toInt())} মণ)",
                        fontSize = AppText.Small,
                        color = colors.textMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { isAddingBags = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAddingBags) colors.badgeSuccessBg else colors.surfaceElevated,
                                contentColor = if (isAddingBags) colors.badgeSuccessText else colors.textMuted
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(AppIcon.Sm))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("স্টক ইন (যোগ)")
                        }

                        Button(
                            onClick = { isAddingBags = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isAddingBags) colors.badgeDangerBg else colors.surfaceElevated,
                                contentColor = if (!isAddingBags) colors.badgeDangerText else colors.textMuted
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(AppIcon.Sm))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ঘাটতি / আউট")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = adjustDeltaStr,
                        onValueChange = { adjustDeltaStr = it },
                        label = { Text("বস্তার পরিমাণ", color = colors.textMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("adjust_delta_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = adjustReasonStr,
                        onValueChange = { adjustReasonStr = it },
                        label = { Text("কারণ (যেমন: গোডাউন খালাসি, নমুনা কর্তন)", color = colors.textMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border
                        ),
                        singleLine = true
                    )

                    if (isAddingBags) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OptionalMillerField(
                            parties = parties,
                            selectedMiller = selectedIntakeMiller,
                            onMillerSelected = { selectedIntakeMiller = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val count = BanglaNumberUtils.toEnglishDigits(adjustDeltaStr).toIntOrNull() ?: 0
                            if (count > 0) {
                                val delta = if (isAddingBags) count else -count
                                viewModel.adjustStock(
                                    productId = product.id,
                                    productName = product.name,
                                    delta = delta,
                                    reason = adjustReasonStr.ifEmpty { if (isAddingBags) "হাতে গোডাউন ইন" else "ঘাটতি সমন্বয়" },
                                    supplierId = if (isAddingBags) selectedIntakeMiller?.id ?: 0L else 0L,
                                    supplierName = if (isAddingBags) selectedIntakeMiller?.name.orEmpty() else "",
                                    onSuccess = { selectedProductForAdjust = null }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = AppSize.Button)
                            .testTag("confirm_adjust_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)
                    ) {
                        Text("সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Add Product Dialog
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
                        .heightIn(max = 560.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "নতুন চালের জাত যোগ করুন",
                            fontWeight = FontWeight.Bold,
                            fontSize = AppText.Title,
                            color = colors.textPrimary
                        )
                        IconButton(onClick = { showAddProductDialog = false }, modifier = Modifier.size(AppSize.ButtonSm)) {
                            Icon(Icons.Default.Close, contentDescription = "বন্ধ", tint = colors.textMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = newProdName,
                        onValueChange = { newProdName = it },
                        label = { Text("চালের নাম (যেমন: জিরাশাইল কুষ্টিয়া)", color = colors.textMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_product_name_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bag size toggle (25kg vs 50kg)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = newProdBagSize == "50",
                            onClick = { newProdBagSize = "50" },
                            label = { Text("৫০ কেজি বস্তা (১.২৫ মণ)", fontSize = AppText.Caption) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primaryContainer,
                                selectedLabelColor = colors.primary
                            )
                        )
                        FilterChip(
                            selected = newProdBagSize == "25",
                            onClick = { newProdBagSize = "25" },
                            label = { Text("২৫ কেজি বস্তা (০.৬২৫ মণ)", fontSize = AppText.Caption) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primaryContainer,
                                selectedLabelColor = colors.primary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newProdOpeningBags,
                        onValueChange = { newProdOpeningBags = it },
                        label = { Text("শুরুর স্টক বস্তা", color = colors.textMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_product_bags_input"),
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
                        Spacer(modifier = Modifier.height(10.dp))
                        OptionalMillerField(
                            parties = parties,
                            selectedMiller = newProdMiller,
                            onMillerSelected = { newProdMiller = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = newProdSaleRate,
                            onValueChange = { newProdSaleRate = it },
                            label = { Text("বিক্রি দর (বস্তা)", color = colors.textMuted) },
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
                            label = { Text("ক্রয় দর (বস্তা)", color = colors.textMuted) },
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

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (newProdName.isNotBlank()) {
                                val bags = BanglaNumberUtils.toEnglishDigits(newProdOpeningBags).toIntOrNull() ?: 0
                                viewModel.addProduct(
                                    name = newProdName,
                                    category = newProdCategory,
                                    bagSizeKg = newProdBagSize.toIntOrNull() ?: 50,
                                    openingBags = bags,
                                    saleRate = BanglaNumberUtils.toEnglishDigits(newProdSaleRate).toDoubleOrNull() ?: 0.0,
                                    purchaseRate = BanglaNumberUtils.toEnglishDigits(newProdPurchaseRate).toDoubleOrNull() ?: 0.0,
                                    supplierId = if (bags > 0) newProdMiller?.id ?: 0L else 0L,
                                    supplierName = if (bags > 0) newProdMiller?.name.orEmpty() else "",
                                    onSuccess = {
                                        newProdMiller = null
                                        showAddProductDialog = false
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = AppSize.Button)
                            .testTag("confirm_add_product_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)
                    ) {
                        Text("সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    productPendingDelete?.let { product ->
        val chalanCount = transactions.count { it.productId == product.id }
        Dialog(onDismissRequest = { productPendingDelete = null }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "চাল মুছে ফেলবেন?",
                        fontWeight = FontWeight.Bold,
                        fontSize = AppText.Title,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "${product.name} মাল খাতা থেকে উঠে যাবে। বাকি ${BanglaNumberUtils.toBanglaDigits(product.currentBags)} বস্তা মোট মজুদ থেকে বাদ যাবে। ক্যাশ, খরিদ্দারের বাকি এবং মিলারের পাওনা অপরিবর্তিত থাকবে।",
                        fontSize = AppText.Body,
                        color = colors.textSecondary
                    )
                    if (chalanCount > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "এই চালে ${BanglaNumberUtils.toBanglaDigits(chalanCount)}টি চালান আছে। পুরনো মেমো, বাকি এবং খালাসি ক্যাশ থেকে যাবে। বিক্রি বাতিল হবে না।",
                            fontSize = AppText.Body,
                            color = colors.badgeDangerText
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { productPendingDelete = null },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceElevated),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("বাতিল", color = colors.textSecondary)
                        }
                        Button(
                            onClick = {
                                viewModel.deleteProduct(product.id) {
                                    productPendingDelete = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.badgeDangerText,
                                contentColor = colors.surface
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("confirm_delete_product_button")
                        ) {
                            Text("মুছুন", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CyberProductCard(
    product: ProductEntity,
    lastMillerName: String?,
    onAdjustClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val colors = LocalNanoColors.current

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
                            .size(AppIcon.Badge)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.badgeSuccessBg)
                            .border(1.dp, colors.badgeSuccessText.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            tint = colors.badgeSuccessText,
                            modifier = Modifier.size(AppIcon.Md)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = product.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = AppText.Label,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${product.category} • ${product.bagSizeKg} কেজি চটের ব্যাগ",
                            fontSize = AppText.Caption,
                            color = colors.textMuted
                        )
                        if (!lastMillerName.isNullOrBlank()) {
                            Text(
                                text = "শেষ মিলার: $lastMillerName",
                                fontSize = AppText.Caption,
                                color = colors.badgeBlueText
                            )
                        }
                    }
                }

                // Bag & Mon count pill (Screenshot 3 Container Style)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${BanglaNumberUtils.toBanglaDigits(product.currentBags)} বস্তা",
                        fontWeight = FontWeight.Bold,
                        fontSize = AppText.Title,
                        color = colors.primary
                    )
                    Text(
                        text = "~${BanglaNumberUtils.toBanglaDigits(product.totalMon.toInt())} মণ",
                        fontSize = AppText.Caption,
                        color = colors.badgeAmberText
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = colors.borderSubtle)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "বিক্রয় দর: ${BanglaNumberUtils.formatCurrency(product.defaultSaleRate)} /বস্তা",
                        fontSize = AppText.Small,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "মণ দর: ~${BanglaNumberUtils.formatCurrency(product.ratePerMon)} /মণ",
                        fontSize = AppText.Caption,
                        color = colors.textMuted
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onDeleteClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.badgeDangerBg,
                            contentColor = colors.badgeDangerText
                        ),
                        modifier = Modifier
                            .heightIn(min = AppSize.ButtonSm)
                            .testTag("delete_product_button")
                    ) {
                        Text("মুছুন", fontSize = AppText.Caption)
                    }
                    Button(
                        onClick = onAdjustClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.surfaceElevated,
                            contentColor = colors.textPrimary
                        ),
                        modifier = Modifier.heightIn(min = AppSize.ButtonSm)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(AppIcon.Xs))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("স্টক ইন/আউট", fontSize = AppText.Caption)
                    }
                }
            }
        }
    }
}
