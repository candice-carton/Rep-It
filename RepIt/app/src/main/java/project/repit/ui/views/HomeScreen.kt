package project.repit.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import project.repit.ui.components.AppPage
import project.repit.ui.components.RoutineBox
import project.repit.ui.theme.SoftViolet
import project.repit.ui.viewModel.HomeViewModel
import project.repit.ui.viewModel.RoutineUiEvent
import project.repit.ui.viewModel.RoutineVM
import java.util.Calendar

/**
 * Écran d'accueil de l'application.
 * Affiche un message de bienvenue personnalisé, les défis du jour à réaliser et ceux déjà terminés.
 *
 * Toutes les données sont désormais préparées par le [HomeViewModel].
 *
 * @param navController Contrôleur de navigation.
 * @param viewModel ViewModel de l'accueil.
 */
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    // Observation des listes pré-calculées par le ViewModel
    val todoRoutines by viewModel.todoRoutines
    val doneRoutines by viewModel.doneRoutines
    val upcomingHighlights by viewModel.upcomingHighlights

    var updatingRoutineValue by remember { mutableStateOf<RoutineVM?>(null) }
    
    val todayTimestamp = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        HeaderSection()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section "Mes défis" - À faire
            item {
                SectionHeader("Mes défis", "À faire")
                Spacer(modifier = Modifier.height(8.dp))
                if (todoRoutines.isEmpty()) {
                    EmptyStateCard("Aucun défi à réaliser.")
                }
            }
            items(todoRoutines) { (routine, nextOcc) ->
                RoutineBox(
                    routine = routine,
                    onEdit = { /* Navigation vers édition */ },
                    onDelete = { viewModel.onEvent(RoutineUiEvent.DeleteRoutine(routine)) },
                    isUpcoming = nextOcc > todayTimestamp,
                    forcedDate = nextOcc,
                    onStart = {
                        handleStartAction(routine, todayTimestamp, navController, viewModel) { updatingRoutineValue = it }
                    }
                )
            }

            // Section "Défis terminés"
            item {
                Spacer(modifier = Modifier.height(12.dp))
                SectionHeader("Défis du jour terminés", if (doneRoutines.isNotEmpty()) "Bravo" else "")
                Spacer(modifier = Modifier.height(4.dp))
                if (doneRoutines.isEmpty()) {
                    EmptyStateCard("Vous n'avez pas encore terminé de défi aujourd'hui.")
                }
            }
            items(doneRoutines) { routine ->
                RoutineBox(
                    routine = routine,
                    onEdit = { /* Navigation vers édition */ },
                    onDelete = { viewModel.onEvent(RoutineUiEvent.DeleteRoutine(routine)) }
                )
            }

            // Section "Prochainement" - Highlight
            item {
                Spacer(modifier = Modifier.height(24.dp))
                UpcomingSectionHeader(onSeeAll = { navController.navigate(AppPage.Routines.name) })
                Spacer(modifier = Modifier.height(8.dp))
                if (upcomingHighlights.isEmpty()) {
                    EmptyStateCard("Rien de prévu prochainement.")
                }
            }
            items(upcomingHighlights) { routine ->
                RoutineBox(
                    routine = routine,
                    onEdit = { /* Navigation vers édition */ },
                    onDelete = { viewModel.onEvent(RoutineUiEvent.DeleteRoutine(routine)) },
                    isUpcoming = true,
                    onStart = {
                        if (routine.isQuantifiable) updatingRoutineValue = routine
                        else navController.navigate("${AppPage.Timer.name}/${routine.id}")
                    }
                )
            }
        }
    }

    // Dialogue pour les routines quantifiables
    updatingRoutineValue?.let { routine ->
        UpdateValueDialog(
            routine = routine,
            onDismiss = { updatingRoutineValue = null },
            onSave = { updatedRoutine ->
                viewModel.onEvent(RoutineUiEvent.UpdateRoutine(updatedRoutine))
                updatingRoutineValue = null
            }
        )
    }
}

/**
 * Gère l'action de démarrage ou de progression d'une routine.
 */
private fun handleStartAction(
    routine: RoutineVM,
    todayTimestamp: Long,
    navController: NavController,
    viewModel: HomeViewModel,
    onShowUpdateValue: (RoutineVM) -> Unit
) {
    if (routine.isQuantifiable) {
        onShowUpdateValue(routine)
    } else if (routine.isAllDay) {
        viewModel.onEvent(RoutineUiEvent.UpdateRoutine(routine.copy(lastCompletedDate = todayTimestamp, progress = 100)))
    } else {
        navController.navigate("${AppPage.Timer.name}/${routine.id}")
    }
}

@Composable
private fun EmptyStateCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Text(
            text,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UpcomingSectionHeader(onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Prochains défis", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        TextButton(onClick = onSeeAll) {
            Text("Voir tout", color = SoftViolet)
        }
    }
}

@Composable
private fun UpdateValueDialog(routine: RoutineVM, onDismiss: () -> Unit, onSave: (RoutineVM) -> Unit) {
    var newValue by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter une donnée") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Entrez la valeur à ajouter (${routine.unit}) pour ${routine.name}")
                OutlinedTextField(
                    value = newValue,
                    onValueChange = { newValue = it },
                    label = { Text("Valeur") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val added = newValue.toFloatOrNull() ?: 0f
                val total = routine.currentValue + added
                val progress = ((total / routine.targetValue) * 100).toInt().coerceIn(0, 100)
                
                val todayStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                
                val isCompleted = total >= routine.targetValue
                
                onSave(routine.copy(
                    currentValue = total,
                    progress = progress,
                    lastCompletedDate = if (isCompleted) todayStart else routine.lastCompletedDate
                ))
            }) { Text("Ajouter") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

@Composable
fun HeaderSection() {
    Surface(
        color = SoftViolet,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
    ) {
        Column(
            modifier = Modifier.padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Bonjour, Nathan !", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Prêt pour ton défi du jour ?", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                }
                ProfileIcon(letter = "N", score = 8)
            }
            Spacer(modifier = Modifier.height(24.dp))
            WeatherAndStepsInfo()
        }
    }
}

@Composable
private fun ProfileIcon(letter: String, score: Int) {
    Box(contentAlignment = Alignment.BottomEnd) {
        Surface(
            modifier = Modifier.size(50.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.2f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(letter, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }
        Surface(
            modifier = Modifier.size(24.dp),
            shape = CircleShape,
            color = Color(0xFFFBC02D)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(score.toString(), color = SoftViolet, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun WeatherAndStepsInfo() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFFFD600), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ensoleillé, 22°C", color = Color.White, fontSize = 14.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("7,200", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("/ 10,000 pas", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        }
    }
}

@Composable
fun SectionHeader(title: String, tag: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (tag.isNotBlank()) {
            Surface(
                color = if (tag == "Bravo") Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFF2DCE89).copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    tag, 
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = if (tag == "Bravo") Color(0xFF2E7D32) else Color(0xFF2DCE89),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
