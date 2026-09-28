package com.reminder.loanmanager.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.reminder.loanmanager.ui.screens.AddLoanScreen
import com.reminder.loanmanager.ui.screens.AddReminderScreen
import com.reminder.loanmanager.ui.screens.BackupScreen
import com.reminder.loanmanager.ui.screens.DashboardScreen
import com.reminder.loanmanager.ui.screens.InstallmentsScreen
import com.reminder.loanmanager.ui.screens.LoanDetailScreen
import com.reminder.loanmanager.ui.screens.LoansScreen
import com.reminder.loanmanager.ui.screens.RemindersScreen
import com.reminder.loanmanager.ui.screens.ReportsScreen
import com.reminder.loanmanager.ui.screens.SettingsScreen
import com.reminder.loanmanager.ui.viewmodel.MainViewModel

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("dashboard", "داشبورد", Icons.Default.Dashboard),
    Tab("loans", "وام‌ها", Icons.Default.Payments),
    Tab("reminders", "یادآورها", Icons.Default.Notifications),
    Tab("reports", "گزارش‌ها", Icons.Default.Assessment),
    Tab("settings", "تنظیمات", Icons.Default.Settings)
)

@Composable
fun AppNavHost(vm: MainViewModel) {
    val nav = rememberNavController()
    val backEntry by nav.currentBackStackEntryAsState()
    val route = backEntry?.destination?.route
    val snackbar = remember { SnackbarHostState() }
    val message by vm.message.collectAsStateWithLifecycle()

    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            vm.consumeMessage()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (tabs.any { it.route == route }) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(nav, startDestination = "dashboard", modifier = Modifier.padding(padding)) {
            composable("dashboard") { DashboardScreen(vm, onOpenInstallments = { nav.navigate("installments") }) }
            composable("loans") {
                LoansScreen(vm, onAdd = { nav.navigate("add_loan") }, onOpen = { nav.navigate("loan/$it") })
            }
            composable("reminders") { RemindersScreen(vm, onAdd = { nav.navigate("add_reminder") }) }
            composable("reports") { ReportsScreen(vm) }
            composable("settings") { SettingsScreen(vm, onOpenBackup = { nav.navigate("backup") }) }
            composable("add_loan") { AddLoanScreen(vm, onDone = { nav.popBackStack() }) }
            composable("add_reminder") { AddReminderScreen(vm, onDone = { nav.popBackStack() }) }
            composable("installments") { InstallmentsScreen(vm, onBack = { nav.popBackStack() }) }
            composable("backup") { BackupScreen(vm, onBack = { nav.popBackStack() }) }
            composable(
                "loan/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                LoanDetailScreen(vm, entry.arguments?.getLong("id") ?: 0L, onBack = { nav.popBackStack() })
            }
        }
    }
}
