package project.repit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import project.repit.ui.components.AppPage
import project.repit.ui.views.HomeScreen
import project.repit.ui.views.NotificationsScreen
import project.repit.ui.views.ProfileScreen
import project.repit.ui.views.RoutineScreen
import project.repit.ui.views.StatisticsScreen
import project.repit.ui.views.TimerScreen

/**
 * Gère la navigation principale de l'application à l'aide d'un NavHost.
 *
 * @param navController Le contrôleur de navigation pour gérer les déplacements entre les écrans.
 * @param modifier Le modificateur à appliquer au NavHost.
 */
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
        
        composable(
            route = "${AppPage.Timer.name}/{routineId}",
            arguments = listOf(navArgument("routineId") { type = NavType.StringType })
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getString("routineId") ?: ""
            TimerScreen(navController, routineId)
        }
    }
}