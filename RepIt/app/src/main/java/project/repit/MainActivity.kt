package project.repit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import project.repit.model.Routine
import project.repit.ui.theme.RepitTheme
import project.repit.util.RoutineFileUtil

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RepitTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RoutineScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun RoutineScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var routines by remember { mutableStateOf(emptyList<Routine>()) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var isAddingRoutine by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        routines = RoutineFileUtil.readRoutines(context).sortedBy { priorityWeight(it.priority) }
    }

    LazyColumn(
        modifier = modifier
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

        if (routines.isEmpty()) {
            item {
                Text("Aucune routine pour le moment.")
            }
        }

        itemsIndexed(routines) { index, routine ->
            RoutineBox(
                routine = routine,
                onEdit = { editingIndex = index },
                onDelete = {
                    routines = routines.filterIndexed { currentIndex, _ -> currentIndex != index }
                    RoutineFileUtil.saveRoutines(context, routines)
                }
            )
        }
    }

    editingIndex?.let { index ->
        EditRoutineDialog(
            routine = routines[index],
            onDismiss = { editingIndex = null },
            onSave = { updatedRoutine ->
                routines = routines.toMutableList().also { it[index] = updatedRoutine }
                    .sortedBy { priorityWeight(it.priority) }
                RoutineFileUtil.saveRoutines(context, routines)
                editingIndex = null
            }
        )
    }

    if (isAddingRoutine) {
        AddRoutineDialog(
            onDismiss = { isAddingRoutine = false },
            onSave = { newRoutine ->
                routines = (routines + newRoutine).sortedBy { priorityWeight(it.priority) }
                RoutineFileUtil.saveRoutines(context, routines)
                isAddingRoutine = false
            }
        )
    }
}

@Composable
private fun RoutineBox(routine: Routine, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = routine.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = routine.description, style = MaterialTheme.typography.bodyMedium)
            Text(text = "Priorité : ${routine.priority}", style = MaterialTheme.typography.bodySmall)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Modifier la routine")
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Supprimer la routine")
                }
            }
        }
    }
}

@Composable
private fun EditRoutineDialog(
    routine: Routine,
    onDismiss: () -> Unit,
    onSave: (Routine) -> Unit
) {
    var name by remember(routine) { mutableStateOf(routine.name) }
    var description by remember(routine) { mutableStateOf(routine.description) }
    var priority by remember(routine) { mutableStateOf(routine.priority) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier la routine") },
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
                OutlinedTextField(
                    value = priority,
                    onValueChange = { priority = it },
                    label = { Text("Priorité") }
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
                            priority = priority.trim()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun priorityWeight(priority: String): Int = when (priority) {
    "Élevée" -> 0
    "Moyenne" -> 1
    "Faible" -> 2
    else -> 3
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RoutinePreview() {
    RepitTheme {
        RoutineScreen()
    }
}
