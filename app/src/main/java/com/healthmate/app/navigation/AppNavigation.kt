package com.healthmate.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.healthmate.app.features.addrecord.AddEditRecordScreen
import com.healthmate.app.features.dashboard.DashboardScreen
import com.healthmate.app.features.records.RecordListScreen
import com.healthmate.app.features.settings.SettingsScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val RECORDS = "records"
    const val SETTINGS = "settings"
    const val ADD_EDIT = "add_edit"
    const val ARG_RECORD_ID = "recordId"

    /** recordId = -1 means "add new", any other id means "edit this record". */
    fun addEdit(recordId: Long = -1L) = "$ADD_EDIT?$ARG_RECORD_ID=$recordId"
}

@Composable
fun HealthMateNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.DASHBOARD) {

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                bottomBar = { HealthBottomBar(navController) }
            )
        }

        composable(Routes.RECORDS) {
            RecordListScreen(
                onAddRecord = { navController.navigate(Routes.addEdit()) },
                onRecordClick = { id -> navController.navigate(Routes.addEdit(id)) },
                bottomBar = { HealthBottomBar(navController) }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(bottomBar = { HealthBottomBar(navController) })
        }

        composable(
            route = "${Routes.ADD_EDIT}?${Routes.ARG_RECORD_ID}={${Routes.ARG_RECORD_ID}}",
            arguments = listOf(
                navArgument(Routes.ARG_RECORD_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            AddEditRecordScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}

/** Switch between bottom-bar tabs without stacking duplicate screens. */
fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
