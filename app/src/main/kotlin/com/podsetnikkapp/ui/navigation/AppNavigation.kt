package com.podsetnikkapp.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.podsetnikkapp.ui.screens.AddEditScreen
import com.podsetnikkapp.ui.screens.HomeScreen
import com.podsetnikkapp.ui.screens.SettingsScreen
import com.podsetnikkapp.viewmodel.ReminderViewModel
import com.podsetnikkapp.viewmodel.ThemeViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object AddEdit : Screen("add_edit/{reminderId}") {
        fun createRoute(id: Long = -1L) = "add_edit/$id"
    }
}

@Composable
fun AppNavigation(viewModel: ReminderViewModel, themeViewModel: ThemeViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onAddNew = { navController.navigate(Screen.AddEdit.createRoute()) },
                onEdit = { id -> navController.navigate(Screen.AddEdit.createRoute(id)) },
                onSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                themeViewModel = themeViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.AddEdit.route,
            arguments = listOf(navArgument("reminderId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStack ->
            val id = backStack.arguments?.getLong("reminderId") ?: -1L
            AddEditScreen(viewModel = viewModel, reminderId = id, onBack = { navController.popBackStack() })
        }
    }
}
