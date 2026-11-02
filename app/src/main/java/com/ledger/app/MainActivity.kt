package com.ledger.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ledger.app.data.CurrencyPreference
import com.ledger.app.ui.FinanceViewModel
import com.ledger.app.ui.screens.AddTransactionScreen
import com.ledger.app.ui.screens.DashboardScreen
import com.ledger.app.ui.screens.HistoryScreen
import com.ledger.app.ui.screens.SettingsScreen
import com.ledger.app.ui.theme.LedgerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as LedgerApp
        val currencyPref = CurrencyPreference(applicationContext)

        setContent {
            LedgerTheme {
                val viewModel: FinanceViewModel =
                    viewModel(factory = FinanceViewModel.factory(app.repository, currencyPref))
                MainScaffold(viewModel)
            }
        }
    }
}

private data class BottomDest(val route: String, val label: String, val icon: ImageVector)

private const val ROUTE_DASHBOARD = "dashboard"
private const val ROUTE_HISTORY = "history"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_ADD = "add"

@Composable
private fun MainScaffold(viewModel: FinanceViewModel) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val destinations = listOf(
        BottomDest(ROUTE_DASHBOARD, "Home", Icons.Filled.Home),
        BottomDest(ROUTE_HISTORY, "History", Icons.Filled.ReceiptLong),
        BottomDest(ROUTE_SETTINGS, "Settings", Icons.Filled.Settings)
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBars = currentRoute != ROUTE_ADD

    Scaffold(
        bottomBar = {
            if (showBars) {
                NavigationBar {
                    destinations.forEach { dest ->
                        NavigationBarItem(
                            selected = currentRoute == dest.route,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showBars) {
                FloatingActionButton(onClick = { navController.navigate(ROUTE_ADD) }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add transaction")
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_DASHBOARD,
            modifier = Modifier.padding(padding)
        ) {
            composable(ROUTE_DASHBOARD) { DashboardScreen(state) }
            composable(ROUTE_HISTORY) { HistoryScreen(state, onDelete = viewModel::delete) }
            composable(ROUTE_SETTINGS) {
                SettingsScreen(
                    state = state,
                    onSetCurrency = viewModel::setCurrency,
                    onClearAll = viewModel::clearAll
                )
            }
            composable(ROUTE_ADD) {
                AddTransactionScreen(
                    currency = state.currency,
                    onSave = { title, cents, type, category, timestamp ->
                        viewModel.addTransaction(title, cents, type, category, timestamp)
                        navController.popBackStack()
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
        }
    }
}
