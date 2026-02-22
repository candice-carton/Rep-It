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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
                    RoutineHomeScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun RoutineHomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val routines = remember {
        mutableStateListOf<Routine>().apply {
            val loadedRoutines = runCatching { RoutineFileUtil.readRoutines(context) }
                .getOrElse { emptyList() }
            addAll(loadedRoutines.sortedBy { priorityWeight(it.priority) })
        }
    }

    var editedIndex by remember { mutableIntStateOf(-1) }
    var deletingIndex by remember { mutableIntStateOf(-1) }
    val routineFilePath = remember { context.getFileStreamPath("routines.txt").absolutePath }
    fun persistRoutines() {
        runCatching { RoutineFileUtil.saveRoutines(context, routines) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Routines enregistrées",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Text(
                text = "Fichier local : $routineFilePath",
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (routines.isEmpty()) {
            item {
                Text(
                    text = "Aucune routine trouvée dans le fichier local.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            itemsIndexed(routines) { index, routine ->
                RoutineBox(
                    routine = routine,
                    onEditClick = { editedIndex = index },
                    onDeleteClick = { deletingIndex = index }
                )
            }
        }
    }

    if (editedIndex in routines.indices) {
        EditRoutineDialog(
            routine = routines[editedIndex],
            onDismiss = { editedIndex = -1 },
            onSave = { updatedRoutine ->
                routines[editedIndex] = updatedRoutine
                val sortedRoutines = routines.sortedBy { priorityWeight(it.priority) }
                routines.clear()
                routines.addAll(sortedRoutines)
                persistRoutines()
                editedIndex = -1
            }
        )
    }

    if (deletingIndex in routines.indices) {
        DeleteRoutineDialog(
            routine = routines[deletingIndex],
            onDismiss = { deletingIndex = -1 },
            onConfirmDelete = {
                routines.removeAt(deletingIndex)
                persistRoutines()
                editedIndex = -1
                deletingIndex = -1
            }
        )
    }

}

@Composable
private fun RoutineBox(
    routine: Routine,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = routine.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = "Modifier la routine")
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Supprimer la routine")
                    }
                }
            }
            Text(text = routine.description, style = MaterialTheme.typography.bodyMedium)
            Text(text = "Catégorie : ${routine.category}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Priorité : ${routine.priority}", style = MaterialTheme.typography.bodySmall)
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
    var category by remember(routine) { mutableStateOf(routine.category) }
    var periodicity by remember(routine) { mutableStateOf(routine.periodicity) }
    var priority by remember(routine) { mutableStateOf(routine.priority) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier la routine") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Catégorie") })
                OutlinedTextField(value = periodicity, onValueChange = { periodicity = it }, label = { Text("Périodicité") })
                OutlinedTextField(value = priority, onValueChange = { priority = it }, label = { Text("Priorité") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        routine.copy(
                            name = name,
                            description = description,
                            category = category,
                            periodicity = periodicity,
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


@Composable
private fun DeleteRoutineDialog(
    routine: Routine,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Supprimer la routine") },
        text = { Text("Voulez-vous vraiment supprimer \"${routine.name}\" ?") },
        confirmButton = {
            Button(onClick = onConfirmDelete) {
                Text("Supprimer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
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
        RoutineHomeScreen()
    }
}
