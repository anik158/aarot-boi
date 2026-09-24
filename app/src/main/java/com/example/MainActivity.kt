package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.KhataScreen
import com.example.ui.screens.PartiesScreen
import com.example.ui.screens.TransactionEntryScreen
import com.example.ui.theme.LocalNanoColors
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AratdarViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AratdarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContainer(viewModel: AratdarViewModel) {
    val colors = LocalNanoColors.current

    // Exactly 4 Core Navigation Items as requested:
    // 1. হোম | 2. খাতা | 3. নতুন চালান | 4. দেনা-পাওনা
    val navItems = listOf(
        Screen.Home,
        Screen.Khata,
        Screen.Chalan,
        Screen.Parties
    )

    var currentRoute by rememberSaveable { mutableStateOf(Screen.Home.route) }
    var khataSubTab by rememberSaveable { mutableIntStateOf(0) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // Responsive font scaling based on screen width
        val isCompactScreen = maxWidth < 380.dp
        val navLabelSize = if (isCompactScreen) 11.sp else 12.sp
        val navIconSize = if (isCompactScreen) 22.dp else 24.dp

        Scaffold(
            containerColor = colors.bg,
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                // Bottom Bar with Safe Area Insets (navigationBarsPadding)
                // Guaranteed >= 48dp touch targets, clean active pill highlight
                Surface(
                    color = colors.navBg,
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding() // Protects against Android gesture pill & nav buttons
                        .border(1.dp, colors.navBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 64.dp)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        navItems.forEach { screen ->
                            val isSelected = currentRoute == screen.route
                            val interactionSource = remember { MutableInteractionSource() }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 52.dp) // Minimum 48dp+ interactive touch target
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = ripple(bounded = true, color = colors.primary)
                                    ) {
                                        currentRoute = screen.route
                                    }
                                    .padding(vertical = 4.dp, horizontal = 2.dp)
                                    .testTag("nav_${screen.route}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    // Highlighted Pill for Active Tab (Icon container)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) colors.primaryContainer else Color.Transparent)
                                            .padding(horizontal = if (isSelected) 14.dp else 0.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.titleBangla,
                                            tint = if (isSelected) colors.primary else colors.textMuted,
                                            modifier = Modifier.size(navIconSize)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = screen.titleBangla,
                                        fontSize = navLabelSize,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) colors.primary else colors.textMuted,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentRoute) {
                    Screen.Home.route -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToChalan = { currentRoute = Screen.Chalan.route },
                        onNavigateToStock = {
                            khataSubTab = 0
                            currentRoute = Screen.Khata.route
                        },
                        onNavigateToParties = { currentRoute = Screen.Parties.route }
                    )
                    Screen.Khata.route -> KhataScreen(
                        viewModel = viewModel,
                        initialTab = khataSubTab
                    )
                    Screen.Chalan.route -> TransactionEntryScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentRoute = Screen.Home.route }
                    )
                    Screen.Parties.route -> PartiesScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
