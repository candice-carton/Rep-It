package project.repit.navigation
//
//import androidx.compose.runtime.Composable
//import androidx.navigation.NavHostController
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import project.repit.ui.screens.*
//
//sealed class Screen(val route: String) {
//    object Home          : Screen("home")
//    object NewChallenge  : Screen("new_challenge")
//    object Notifications : Screen("notifications")
//    object Statistics    : Screen("statistics")
//    object Profile       : Screen("profile")
//}
//
//@Composable
//fun RepitNavHost(
//    navController: NavHostController = rememberNavController(),
//) {
//    NavHost(
//        navController    = navController,
//        startDestination = Screen.Home.route,
//    ) {
//        composable(Screen.Home.route)          { HomeScreen(navController) }
//        composable(Screen.NewChallenge.route)  { ChallengeScreen(navController) }
//        composable(Screen.Notifications.route) { NotificationsScreen(navController) }
//        composable(Screen.Statistics.route)    { StatisticsScreen(navController) }
//        composable(Screen.Profile.route)       { ProfileScreen(navController) }
//    }
//}
//
