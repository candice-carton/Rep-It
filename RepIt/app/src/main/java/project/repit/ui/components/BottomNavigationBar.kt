package project.repit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import project.repit.ui.theme.DarkerGray
import project.repit.ui.theme.SoftViolet

/**
 * Barre de navigation inférieure personnalisée pour l'application.
 * Affiche les icônes et libellés des pages principales et gère la navigation entre elles.
 *
 * @param navController Contrôleur de navigation utilisé pour changer de destination.
 */
@Composable
fun BottomNavigationBar(navController: NavController) {
    // Observation de la route actuelle pour mettre en évidence l'icône active
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Liste des pages à afficher dans le menu
    val menuPages = listOf(
        AppPage.Home, 
        AppPage.Routines, 
        AppPage.Notifications, 
        AppPage.Statistics, 
        AppPage.Profile
    )

    Surface(tonalElevation = 4.dp, shadowElevation = 8.dp) {
        Column {
            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                menuPages.forEach { page ->
                    val isSelected = currentRoute == page.name
                    
                    NavigationItem(
                        page = page,
                        isSelected = isSelected,
                        modifier = Modifier.weight(1f),
                        onNavigate = {
                            navController.navigate(page.name) {
                                // Évite de créer une pile infinie d'écrans
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Élément individuel de la barre de navigation (Icône + Texte + Indicateur).
 */
@Composable
private fun NavigationItem(
    page: AppPage,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onNavigate: () -> Unit
) {
    val activeColor = SoftViolet
    val inactiveColor = DarkerGray

    Column(
        modifier = modifier
            .clickable { onNavigate() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = page.icon,
            contentDescription = page.label,
            tint = if (isSelected) activeColor else inactiveColor
        )
        Text(
            text = page.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) activeColor else inactiveColor
        )
        // Petit point indicateur sous l'icône sélectionnée
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(if (isSelected) 6.dp else 0.dp)
                .clip(CircleShape)
                .background(if (isSelected) activeColor else Color.Transparent)
        )
    }
}
