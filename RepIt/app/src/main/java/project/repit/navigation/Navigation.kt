package project.repit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import project.repit.ui.screens.HomeScreen

/**
 * Définit toutes les routes (écrans) possibles de l'application.
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home_screen")
    data object NewChallenge : Screen("new_challenge_screen")
    data object Notifications : Screen("notifications_screen")
    data object Statistics : Screen("statistics_screen")
    data object Profile : Screen("profile_screen")
}

/**
 * C'est le composant qui gère le graphe de navigation de l'application.
 * Il décide quel écran afficher en fonction de la route actuelle.
 */
@Composable
fun RepitNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Home.route // La route de départ
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Définit l'écran à afficher pour la route "home_screen"
        composable(route = Screen.Home.route) {
            // Appelle le Composable de l'écran d'accueil
            HomeScreen(navController = navController)
        }

        // --- DÉFINISSEZ VOS AUTRES ÉCRANS ICI ---
        // Exemple pour les autres écrans (actuellement vides)

        composable(route = Screen.NewChallenge.route) {
            // À remplacer par votre écran de défis, par exemple :
            // NewChallengeScreen(navController = navController)
        }

        composable(route = Screen.Notifications.route) {
            // NotificationsScreen(navController = navController)
        }

        composable(route = Screen.Statistics.route) {
            // StatisticsScreen(navController = navController)
        }

        composable(route = Screen.Profile.route) {
            // ProfileScreen(navController = navController)
        }
    }
}
