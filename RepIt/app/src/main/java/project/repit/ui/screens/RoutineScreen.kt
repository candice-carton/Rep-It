package project.repit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import project.repit.model.Routine
import project.repit.model.RoutineViewModel
import project.repit.ui.components.DropdownField
import project.repit.ui.components.RoutineBox
import project.repit.ui.theme.RepitTheme

/**
 * Affiche l'écran de gestion des routines.
 *
 * Cet écran affiche la liste des routines de l'utilisateur, permet d'en ajouter de nouvelles,
 * de modifier celles qui existent et de les supprimer.
 *
 * @param navController Le contrôleur de navigation, utilisé pour la navigation entre les écrans.
 * @param viewModel Le ViewModel pour gérer les données des routines.
 */
@Composable
fun RoutineScreen(
    navController: NavController,
    viewModel: RoutineViewModel = viewModel()
) {
    val routines by viewModel.allRoutines.collectAsState(initial = emptyList())
    var editingRoutine by remember { mutableStateOf<Routine?>(null) }
    var isAddingRoutine by remember { mutableStateOf(false) }

    val sortedRoutines = routines.sortedBy { priorityWeight(it.priority) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Mes Routines",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Button(onClick = { isAddingRoutine = true }) {
                    Text("Ajouter")
                }
            }
        }

        if (sortedRoutines.isEmpty()) {
            item {
                Text("Aucune routine pour le moment.")
            }
        }

        itemsIndexed(sortedRoutines) { _, routine ->
            RoutineBox(
                routine = routine,
                onEdit = { editingRoutine = routine },
                onDelete = {
                    viewModel.delete(routine)
                }
            )
        }
    }

    editingRoutine?.let { routine ->
        EditRoutineDialog(
            routine = routine,
            onDismiss = { editingRoutine = null },
            onSave = { updatedRoutine ->
                viewModel.update(updatedRoutine)
                editingRoutine = null
            }
        )
    }

    if (isAddingRoutine) {
        AddRoutineDialog(
            onDismiss = { isAddingRoutine = false },
            onSave = { newRoutine ->
                viewModel.insert(newRoutine)
                isAddingRoutine = false
            }
        )
    }
}

/**
 * Affiche une boîte de dialogue pour modifier une routine existante.
 *
 * @param routine La routine à modifier.
 * @param onDismiss Appelé lorsque l'utilisateur ferme la boîte de dialogue.
 * @param onSave Appelé avec la routine mise à jour lorsque l'utilisateur enregistre les modifications.
 */
@Composable
private fun EditRoutineDialog(
    routine: Routine,
    onDismiss: () -> Unit,
    onSave: (Routine) -> Unit
) {
    val categories = listOf("Santé", "Sport", "Bien-être", "Travail", "Personnel", "Autre")
    val priorities = listOf("Élevée", "Moyenne", "Faible")

    var name by remember(routine) { mutableStateOf(routine.name) }
    var description by remember(routine) { mutableStateOf(routine.description) }
    var category by remember(routine) { mutableStateOf(routine.category) }
    var startAt by remember(routine) { mutableStateOf(routine.startAt) }
    var endAt by remember(routine) { mutableStateOf(routine.endAt) }
    var periodicity by remember(routine) { mutableStateOf(routine.periodicity) }
    var priority by remember(routine) { mutableStateOf(routine.priority) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier la routine") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                DropdownField(
                    label = "Catégorie",
                    options = categories,
                    selectedOption = category,
                    onOptionSelected = { category = it }
                )
                OutlinedTextField(value = startAt, onValueChange = { startAt = it }, label = { Text("Heure de début") })
                OutlinedTextField(value = endAt, onValueChange = { endAt = it }, label = { Text("Heure de fin") })
                OutlinedTextField(value = periodicity, onValueChange = { periodicity = it }, label = { Text("Répétition") })
                DropdownField(
                    label = "Priorité",
                    options = priorities,
                    selectedOption = priority,
                    onOptionSelected = { priority = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        routine.copy(
                            name = name.trim(),
                            description = description.trim(),
                            category = category,
                            startAt = startAt.trim(),
                            endAt = endAt.trim(),
                            periodicity = periodicity.trim(),
                            priority = priority
                        )
                    )
                }
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

/**
 * Affiche une boîte de dialogue pour ajouter une nouvelle routine.
 *
 * @param onDismiss Appelé lorsque l'utilisateur ferme la boîte de dialogue.
 * @param onSave Appelé avec la nouvelle routine lorsque l'utilisateur l'enregistre.
 */
@Composable
private fun AddRoutineDialog(
    onDismiss: () -> Unit,
    onSave: (Routine) -> Unit
) {
    val categories = listOf("Santé", "Sport", "Bien-être", "Travail", "Personnel", "Autre")
    val priorities = listOf("Élevée", "Moyenne", "Faible")

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categories.first()) }
    var startAt by remember { mutableStateOf("") }
    var endAt by remember { mutableStateOf("") }
    var periodicity by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(priorities[1]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter une routine") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom") }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
                DropdownField(
                    label = "Catégorie",
                    options = categories,
                    selectedOption = category,
                    onOptionSelected = { category = it }
                )
                OutlinedTextField(
                    value = startAt,
                    onValueChange = { startAt = it },
                    label = { Text("Heure de début") }
                )
                OutlinedTextField(
                    value = endAt,
                    onValueChange = { endAt = it },
                    label = { Text("Heure de fin") }
                )
                OutlinedTextField(
                    value = periodicity,
                    onValueChange = { periodicity = it },
                    label = { Text("Répétition") }
                )
                DropdownField(
                    label = "Priorité",
                    options = priorities,
                    selectedOption = priority,
                    onOptionSelected = { priority = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) return@TextButton
                    onSave(
                        Routine(
                            name = name.trim(),
                            description = description.trim(),
                            category = category,
                            startAt = startAt.trim(),
                            endAt = endAt.trim(),
                            periodicity = periodicity.trim(),
                            priority = priority
                        )
                    )
                }
            ) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

/**
 * Calcule le poids d'une priorité pour le tri.
 * Une priorité "Élevée" a un poids plus faible pour apparaître en premier.
 *
 * @param priority La chaîne de caractères représentant la priorité.
 * @return Un entier représentant le poids de la priorité.
 */
private fun priorityWeight(priority: String): Int = when (priority) {
    "Élevée" -> 0
    "Moyenne" -> 1
    "Faible" -> 2
    else -> 3
}

/**
 * Composable de prévisualisation pour l'écran [RoutineScreen].
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RoutinePreview() {
    RepitTheme {
        RoutineScreen(navController = NavController(LocalContext.current))
    }
}
