package project.repit

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import project.repit.model.Routine
import project.repit.ui.theme.RepitTheme
import project.repit.util.RoutineFileUtil
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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
    val coroutineScope = rememberCoroutineScope()
    val routines = remember { mutableStateListOf<Routine>() }

    if (routines.isEmpty()) {
        routines.addAll(RoutineFileUtil.readRoutines(context))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Mes routines sportives",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        if (routines.isEmpty()) {
            item {
                Text(
                    text = "Aucune routine enregistrée pour le moment.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            items(routines) { routine ->
                RoutineCard(routine = routine)
            }
        }

        item { HorizontalDivider() }

        item {
            CreateRoutineSection(
                onCreateRoutine = { routine ->
                    routines.add(routine)
                    coroutineScope.launch {
                        RoutineFileUtil.saveRoutines(context, routines)
                    }
                }
            )
        }
    }
}

@Composable
private fun RoutineCard(routine: Routine) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(routine.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (routine.description.isNotBlank()) {
                Text(routine.description, style = MaterialTheme.typography.bodyMedium)
            }
            Text("Catégorie : ${routine.category}")
            Text("Début : ${routine.startAt.format(formatter)}")
            Text("Fin : ${routine.endAt.format(formatter)}")
            Text("Périodicité : ${routine.periodicity}")
            Text("Priorité : ${routine.priority}")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateRoutineSection(onCreateRoutine: (Routine) -> Unit) {
    val context = LocalContext.current

    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    val categories = listOf("Santé", "Cardio", "Musculation", "Mobilité", "Loisir")
    var category by rememberSaveable { mutableStateOf(categories.first()) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val periodicities = listOf("Quotidienne", "Hebdomadaire", "Mensuelle")
    var periodicity by rememberSaveable { mutableStateOf(periodicities.first()) }

    val priorities = listOf("Faible", "Moyenne", "Élevée")
    var priority by rememberSaveable { mutableStateOf(priorities[1]) }

    var startDateTime by remember { mutableStateOf(LocalDateTime.now().withSecond(0).withNano(0)) }
    var endDateTime by remember { mutableStateOf(LocalDateTime.now().plusHours(1).withSecond(0).withNano(0)) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Créer une routine", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nom de la routine *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = !categoryExpanded }
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                label = { Text("Catégorie") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                categories.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            category = option
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        Text("Horaires", style = MaterialTheme.typography.titleMedium)
        DateTimeField(
            label = "Début",
            dateTime = startDateTime,
            onDateTimeSelected = { startDateTime = it },
            formatter = formatter,
            context = context
        )
        DateTimeField(
            label = "Fin",
            dateTime = endDateTime,
            onDateTimeSelected = { endDateTime = it },
            formatter = formatter,
            context = context
        )

        Text("Périodicité", style = MaterialTheme.typography.titleMedium)
        OptionGroup(options = periodicities, selected = periodicity, onSelect = { periodicity = it })

        Text("Priorité", style = MaterialTheme.typography.titleMedium)
        OptionGroup(options = priorities, selected = priority, onSelect = { priority = it })

        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(
            onClick = {
                when {
                    name.isBlank() -> errorMessage = "Le nom de la routine est obligatoire."
                    endDateTime.isBefore(startDateTime) -> errorMessage = "La fin doit être après le début."
                    else -> {
                        onCreateRoutine(
                            Routine(
                                name = name.trim(),
                                description = description.trim(),
                                category = category,
                                startAt = startDateTime,
                                endAt = endDateTime,
                                periodicity = periodicity,
                                priority = priority
                            )
                        )
                        name = ""
                        description = ""
                        category = categories.first()
                        periodicity = periodicities.first()
                        priority = priorities[1]
                        startDateTime = LocalDateTime.now().withSecond(0).withNano(0)
                        endDateTime = LocalDateTime.now().plusHours(1).withSecond(0).withNano(0)
                        errorMessage = null
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enregistrer la routine")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun OptionGroup(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Column {
        options.forEach { option ->
            Row {
                RadioButton(selected = selected == option, onClick = { onSelect(option) })
                Text(option, modifier = Modifier.padding(top = 12.dp))
            }
        }
    }
}

@Composable
private fun DateTimeField(
    label: String,
    dateTime: LocalDateTime,
    onDateTimeSelected: (LocalDateTime) -> Unit,
    formatter: DateTimeFormatter,
    context: android.content.Context
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Text("$label : ${dateTime.format(formatter)}", modifier = Modifier.weight(1f))
        Button(onClick = {
            val initialDate = dateTime.toLocalDate()
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                    val currentTime = dateTime.toLocalTime()
                    TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            onDateTimeSelected(LocalDateTime.of(selectedDate, LocalTime.of(hourOfDay, minute)))
                        },
                        currentTime.hour,
                        currentTime.minute,
                        true
                    ).show()
                },
                initialDate.year,
                initialDate.monthValue - 1,
                initialDate.dayOfMonth
            ).show()
        }) {
            Text("Choisir")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RoutinePreview() {
    RepitTheme {
        RoutineHomeScreen()
    }
}
