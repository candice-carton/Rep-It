package project.repit.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import project.repit.model.data.AppDatabase
import project.repit.model.domain.useCase.GetRoutineUseCase
import project.repit.model.data.RoutineRepository
import project.repit.ui.theme.SoftViolet
import project.repit.ui.viewModel.TimerViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Écran affichant un minuteur pour une routine en cours.
 * Permet à l'utilisateur de suivre le temps passé sur une activité, de mettre en pause
 * et de marquer la routine comme terminée.
 *
 * @param navController Contrôleur de navigation.
 * @param routineId L'identifiant de la routine associée à ce minuteur.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    navController: NavController,
    routineId: String
) {
    val context = LocalContext.current
    
    // Initialisation manuelle du ViewModel avec ses dépendances (Injection simple)
    val repository = remember { RoutineRepository(AppDatabase.getDatabase(context).routineDao()) }
    val getRoutineUseCase = remember { GetRoutineUseCase(repository) }
    
    val viewModel: TimerViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return TimerViewModel(routineId, repository, getRoutineUseCase) as T
            }
        }
    )

    // Observation des états du ViewModel
    val routineName by viewModel.routineName
    val timeLeft by viewModel.timeLeft
    val isRunning by viewModel.isRunning

    // Formatage du temps restant en mm:ss
    val minutes = (timeLeft / 1000) / 60
    val seconds = (timeLeft / 1000) % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)

    Scaffold(
        topBar = {
            TimerTopBar(onBackClick = { navController.popBackStack() })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Nom de la routine
            Text(
                text = routineName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = SoftViolet
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Affichage du minuteur
            Text(
                text = timeString,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Contrôles du minuteur (Lecture/Pause)
            TimerControls(
                isRunning = isRunning,
                onToggle = { viewModel.toggleTimer() }
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Bouton pour finaliser la routine
            FinishButton(
                onClick = { 
                    viewModel.finishRoutine {
                        navController.popBackStack()
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Message d'aide
            Text(
                text = "Appuie sur terminer une fois que tu as fini !",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Barre supérieure de l'écran minuteur.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimerTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text("Session en cours", style = MaterialTheme.typography.titleMedium) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

/**
 * Bouton de contrôle principal pour démarrer ou mettre en pause le minuteur.
 */
@Composable
private fun TimerControls(isRunning: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)
    ) {
        LargeFloatingActionButton(
            onClick = onToggle,
            containerColor = SoftViolet,
            contentColor = Color.White,
            shape = androidx.compose.foundation.shape.CircleShape
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isRunning) "Pause" else "Reprendre",
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

/**
 * Bouton pour terminer la session de routine.
 */
@Composable
private fun FinishButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(60.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SoftViolet)
    ) {
        Text("Terminer le défi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}
