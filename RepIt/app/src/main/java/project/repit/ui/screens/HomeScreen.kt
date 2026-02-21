package project.repit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import project.repit.data.model.Routine
import project.repit.ui.components.FitBottomBar
import project.repit.ui.components.RoutineCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    // CORRECTION : La liste de routines est mise à jour pour correspondre
    // à la nouvelle définition de la data class 'Routine'.
    val routines: List<Routine> = listOf(
        Routine(
            id = 1,
            title = "Routine de la semaine",
            description = "Entraînement complet pour tous les jours.",
            frequency = "L, M, M, J, V",
            targetMinutes = 30
        ),
        Routine(
            id = 2,
            title = "Full Body Débutant",
            description = "Idéal pour commencer la musculation.",
            frequency = "M, V",
            targetMinutes = 45
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Rep-It · Mes routines") })
        },
        bottomBar = {
            FitBottomBar(navController = navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cette partie fonctionne maintenant car 'routines' et 'RoutineCard'
            // utilisent la même structure de données.
            routines.forEach { routine ->
                RoutineCard(routine)
            }
        }
    }
}
