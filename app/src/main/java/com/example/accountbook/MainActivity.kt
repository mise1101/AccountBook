package com.example.accountbook

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.accountbook.ui.screen.AddTransactionScreen
import com.example.accountbook.ui.screen.CategoryManageScreen
import com.example.accountbook.ui.screen.HomeScreen
import com.example.accountbook.ui.screen.StatsScreen
import com.example.accountbook.ui.theme.AccountBookTheme
import com.example.accountbook.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val addRequestCount = mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (intent?.action == "com.example.accountbook.ACTION_ADD_TRANSACTION") {
            addRequestCount.intValue = 1
        }
        setContent {
            AccountBookTheme {
                AccountBookApp(addRequestCount = addRequestCount)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == "com.example.accountbook.ACTION_ADD_TRANSACTION") {
            addRequestCount.intValue++
        }
    }
}

private val tabScreens = listOf(
    TabScreen("Account", Icons.AutoMirrored.Filled.List),
    TabScreen("Book", Icons.Default.BarChart)
)

private data class TabScreen(val label: String, val icon: ImageVector)

@Composable
fun AccountBookApp(addRequestCount: State<Int>) {
    val viewModel: MainViewModel = viewModel()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var isAdding by rememberSaveable { mutableStateOf(false) }
    var isManagingCategories by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(addRequestCount.value) {
        if (addRequestCount.value > 0) {
            isAdding = true
        }
    }

    if (isManagingCategories) {
        CategoryManageScreen(
            viewModel = viewModel,
            onBack = { isManagingCategories = false }
        )
    } else if (isAdding) {
        AddTransactionScreen(
            viewModel = viewModel,
            onBack = { isAdding = false },
            onManageCategories = { isManagingCategories = true }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    tabScreens.forEachIndexed { index, screen ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            when (selectedTab) {
                0 -> HomeScreen(
                    viewModel = viewModel,
                    onAddClick = { isAdding = true },
                    modifier = Modifier.padding(innerPadding)
                )
                1 -> StatsScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
