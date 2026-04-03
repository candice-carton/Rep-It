package project.repit.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppPage(val label: String, val icon: ImageVector) {
    Home("Accueil", Icons.Default.Home),
    Routines("Défis", Icons.Default.FitnessCenter),
    Notifications("Notifications", Icons.Default.Notifications),
    Statistics("Statistiques", Icons.Default.BarChart),
    Profile("Profil", Icons.Default.Person),
    Timer("Timer", Icons.Default.FitnessCenter)
}
