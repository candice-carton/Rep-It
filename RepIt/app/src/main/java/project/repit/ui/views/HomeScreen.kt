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
import project.repit.ui.viewModel.RoutineViewModel
import project.repit.ui.viewModel.RoutineUiEvent
import project.repit.ui.viewModel.RoutineVM
import java.util.Calendar

/**
 * Écran d'accueil affichant les routines du jour et les statistiques.
 */
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: RoutineViewModel = viewModel()
) {
    val routines by viewModel.routines
    var updatingRoutineValue by remember { mutableStateOf<RoutineVM?>(null) }
    
    // Date d'aujourd'hui (minuit)
    val now = Calendar.getInstance()
    val todayStart = now.clone() as Calendar
    todayStart.set(Calendar.HOUR_OF_DAY, 0)
    todayStart.set(Calendar.MINUTE, 0)
    todayStart.set(Calendar.SECOND, 0)
    todayStart.set(Calendar.MILLISECOND, 0)
    val todayTimestamp = todayStart.timeInMillis
    
    // Jour de la semaine (1=Lundi, ..., 7=Dimanche)
    val currentDayOfWeek = if (now.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) 7 else now.get(Calendar.DAY_OF_WEEK) - 1

    // Filtrage strict : Seulement ce qui est prévu pour AUJOURD'HUI
    val todayScheduled = routines.filter { routine ->
        if (routine.isRepetitive) {
            routine.repeatDays.contains(currentDayOfWeek)
        } else {
            routine.scheduledDate == todayTimestamp
        }
    }

    // On sépare en "À faire" et "Terminé" (exclusivement pour aujourd'hui)
    val todayTodo = todayScheduled.filter { it.lastCompletedDate != todayTimestamp }.sortedBy { it.startAt }
    val todayDone = todayScheduled.filter { it.lastCompletedDate == todayTimestamp }.sortedBy { it.startAt }

    // Prochains défis (après aujourd'hui) - Max 5
    val upcoming = routines.filter { routine ->
        val nextOccurrence = routine.getNextOccurrenceTimestamp(todayTimestamp + 24 * 3600 * 1000)
        nextOccurrence > todayTimestamp
    }.sortedBy { 
        it.getNextOccurrenceTimestamp(todayTimestamp + 24 * 3600 * 1000) 
    }.take(5)

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        HeaderSection()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Section défis à réaliser aujourd'hui
            if (todayTodo.isNotEmpty()) {
                item {
                    SectionHeader("Mes défis", "À faire")
                    Spacer(modifier = Modifier.height(4.dp))
                }
                items(todayTodo) { routine ->
                    RoutineBox(
                        routine = routine,
                        onEdit = { /* ... */ },
                        onDelete = { viewModel.onEvent(RoutineUiEvent.DeleteRoutine(routine)) },
                        onStart = {
                            if (routine.isQuantifiable) updatingRoutineValue = routine
                            else navController.navigate("${AppPage.Timer.name}/${routine.id}")
                        }
                    )
                }
            }

            // Section défis du jour terminés
            if (todayDone.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionHeader("Défis du jour terminés", "Bravo")
                    Spacer(modifier = Modifier.height(4.dp))
                }
                items(todayDone) { routine ->
                    RoutineBox(
                        routine = routine,
                        onEdit = { /* ... */ },
                        onDelete = { viewModel.onEvent(RoutineUiEvent.DeleteRoutine(routine)) }
                    )
                }
            }

            // Message si rien à faire
            if (todayTodo.isEmpty() && todayDone.isEmpty()) {
                item {
                    SectionHeader("Défi du jour", "Libre")
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Text("Aucun défi prévu pour aujourd'hui.", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Section Prochainement (max 5)
            if (upcoming.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Prochains défis", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { navController.navigate(AppPage.Routines.name) }) {
                            Text("Voir tout", color = SoftViolet)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                items(upcoming) { routine ->
                    RoutineBox(
                        routine = routine,
                        onEdit = { /* ... */ },
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
    }

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
                
                val now = Calendar.getInstance()
                val todayStart = now.clone() as Calendar
                todayStart.set(Calendar.HOUR_OF_DAY, 0); todayStart.set(Calendar.MINUTE, 0); todayStart.set(Calendar.SECOND, 0); todayStart.set(Calendar.MILLISECOND, 0)
                
                val isCompleted = total >= routine.targetValue
                
                onSave(routine.copy(
                    currentValue = total,
                    progress = progress,
                    lastCompletedDate = if (isCompleted) todayStart.timeInMillis else routine.lastCompletedDate
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
                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        modifier = Modifier.size(50.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("N", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = CircleShape,
                        color = Color(0xFFFBC02D)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("8", color = SoftViolet, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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
