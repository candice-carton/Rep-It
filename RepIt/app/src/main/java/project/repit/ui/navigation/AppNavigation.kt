package project.repit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import project.repit.ui.components.AppPage
import project.repit.ui.screens.HomeScreen
import project.repit.ui.screens.NotificationsScreen
import project.repit.ui.screens.ProfileScreen
import project.repit.ui.screens.RoutineScreen
import project.repit.ui.screens.StatisticsScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppPage.Home.name,
        modifier = modifier
    ) {
        composable(AppPage.Home.name) { HomeScreen(navController) }
        composable(AppPage.Routines.name) { RoutineScreen(navController) }
        composable(AppPage.Notifications.name) { NotificationsScreen(navController) }
        composable(AppPage.Statistics.name) { StatisticsScreen(navController) }
        composable(AppPage.Profile.name) { ProfileScreen(navController) }
    }
}