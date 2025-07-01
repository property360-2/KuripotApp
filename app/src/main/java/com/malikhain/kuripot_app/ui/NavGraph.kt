package com.malikhain.kuripot_app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.malikhain.kuripot_app.viewmodel.*

object Routes {
    const val PASSCODE = "passcode"
    const val NOTES = "notes"
    const val BUDGET = "budget"
    const val SETTINGS = "settings"
}

@Composable
fun KuripotNavGraph(
    navController: NavHostController,
    passcodeViewModel: PasscodeViewModel,
    notesViewModel: NotesViewModel,
    budgetViewModel: BudgetViewModel,
    settingsViewModel: SettingsViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Routes.PASSCODE
    ) {
        composable(Routes.PASSCODE) {
            PasscodeScreen(
                viewModel = passcodeViewModel,
                onUnlock = { navController.navigate(Routes.NOTES) { popUpTo(Routes.PASSCODE) { inclusive = true } } }
            )
        }
        composable(Routes.NOTES) {
            NotesScreen(
                viewModel = notesViewModel,
                onNavigateToBudget = { navController.navigate(Routes.BUDGET) }
            )
        }
        composable(Routes.BUDGET) {
            BudgetScreen(
                viewModel = budgetViewModel,
                onNavigateToNotes = { navController.navigate(Routes.NOTES) }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = settingsViewModel
            )
        }
    }
} 