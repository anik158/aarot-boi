package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val titleBangla: String, val icon: ImageVector) {
    object Home : Screen("home", "হোম", Icons.Default.Dashboard)
    object Khata : Screen("khata", "খাতা", Icons.Default.MenuBook)
    object Chalan : Screen("chalan", "নতুন চালান", Icons.AutoMirrored.Filled.ReceiptLong)
    object Parties : Screen("parties", "দেনা-পাওনা", Icons.Default.Group)

    // Backwards compatibility aliases
    val Dashboard get() = Home
    val Stock get() = Khata
}
