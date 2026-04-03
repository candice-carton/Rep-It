package project.repit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import project.repit.ui.components.BottomNavigationBar
import project.repit.ui.components.AppPage
import project.repit.ui.navigation.AppNavigation
import project.repit.ui.theme.RepitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RepitTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // On cache la barre de navigation sur l'écran du Timer
    val showBottomBar = currentRoute != null && !currentRoute.startsWith(AppPage.Timer.name)

    Scaffold(
        bottomBar = { 
            if (showBottomBar) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) {
        AppNavigation(navController = navController, modifier = Modifier.padding(it))
    }
}
