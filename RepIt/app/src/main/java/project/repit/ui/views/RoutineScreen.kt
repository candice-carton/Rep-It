package project.repit.ui.views

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import project.repit.ui.viewModel.RoutineVM
import project.repit.ui.viewModel.RoutineViewModel
import project.repit.ui.viewModel.RoutineUiEvent
import project.repit.ui.components.DaySelector
import project.repit.ui.components.DropdownField
import project.repit.ui.components.RoutineBox
import project.repit.ui.components.AppPage
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Écran de gestion des routines suivant l'architecture MVVM avec UiEvents.
 */
@Composable
fun RoutineScreen(
    navController: NavController,
    viewModel: RoutineViewModel = viewModel()
) {
    val context = LocalContext.current
    val routines by viewModel.routines
    
    var editingRoutine by remember { mutableStateOf<RoutineVM?>(null) }
    var updatingRoutineValue by remember { mutableStateOf<RoutineVM?>(null) }
    var isAddingRoutine by remember { mutableStateOf(false) }
    
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasNotificationPermission = isGranted }
    )

    val categories = listOf("Tous", "Santé", "Sport", "Alimentation", "Bien-être", "Travail")
    var selectedCategory by remember { mutableStateOf("Tous") }

    val filteredRoutines = (if (selectedCategory == "Tous") {
        routines
    } else {
        routines.filter { it.category == selectedCategory }
    }).sortedWith(compareByDescending<RoutineVM> { it.createdAt }.thenBy { priorityWeight(it.priority) })

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                AssistChip(
                    onClick = { 
                        selectedCategory = category
                        viewModel.onEvent(RoutineUiEvent.FilterByCategory(category))
                    },
                    label = { Text(category) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Mes Défis", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Button(onClick = { isAddingRoutine = true }) { Text("Nouveau") }
                }
            }

            if (filteredRoutines.isEmpty()) {
                item { Text("Aucun défi trouvé.") }
            }

            itemsIndexed(filteredRoutines) { _, routine ->
                RoutineBox(
                    routine = routine,
                    onEdit = { editingRoutine = routine },
                    onDelete = { viewModel.onEvent(RoutineUiEvent.DeleteRoutine(routine)) },
                    onStart = {
                        if (routine.isQuantifiable) {
                            updatingRoutineValue = routine
                        } else if (!routine.isAllDay) {
                            if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                navController.navigate("${AppPage.Timer.name}/${routine.id}")
                            }
                        }
                    }
                )
            }
        }
    }

    editingRoutine?.let { routine ->
        EditRoutineDialog(
            routine = routine,
            onDismiss = { editingRoutine = null },
            onSave = { updatedRoutine ->
                viewModel.onEvent(RoutineUiEvent.UpdateRoutine(updatedRoutine))
                editingRoutine = null
            }
        )
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

    if (isAddingRoutine) {
        AddRoutineDialog(
            onDismiss = { isAddingRoutine = false },
            onSave = { newRoutine ->
                viewModel.onEvent(RoutineUiEvent.AddRoutine(newRoutine))
                isAddingRoutine = false
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
                Text("Entrez la valeur à ajouter (${routine.unit})")
                OutlinedTextField(
                    value = newValue,
                    onValueChange = { newValue = it },
                    label = { Text("Valeur") },
                    modifier = Modifier.fillMaxWidth()
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
private fun EditRoutineDialog(routine: RoutineVM, onDismiss: () -> Unit, onSave: (RoutineVM) -> Unit) {
    val categories = listOf("Santé", "Sport", "Bien-être", "Alimentation", "Travail", "Autre")
    val priorities = listOf("Élevée", "Moyenne", "Faible")

    var name by remember(routine) { mutableStateOf(routine.name) }
    var category by remember(routine) { mutableStateOf(routine.category) }
    var startAt by remember(routine) { mutableStateOf(routine.startAt) }
    var endAt by remember(routine) { mutableStateOf(routine.endAt) }
    var isAllDay by remember(routine) { mutableStateOf(routine.isAllDay) }
    var isRepetitive by remember(routine) { mutableStateOf(routine.isRepetitive) }
    var priority by remember(routine) { mutableStateOf(routine.priority) }
    var repeatDays by remember(routine) { mutableStateOf(routine.repeatDays) }
    var scheduledDate by remember(routine) { mutableStateOf(routine.scheduledDate) }
    
    var isQuantifiable by remember(routine) { mutableStateOf(routine.isQuantifiable) }
    var targetValue by remember(routine) { mutableStateOf(routine.targetValue.toString()) }
    var unit by remember(routine) { mutableStateOf(routine.unit) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false),
        title = { Text("Modifier le défi") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom") }, modifier = Modifier.fillMaxWidth())
                DropdownField(label = "Catégorie", options = categories, selectedOption = category, onOptionSelected = { category = it })
                
                if (category == "Alimentation" || isQuantifiable) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Objectif quantifiable")
                        Switch(checked = isQuantifiable, onCheckedChange = { isQuantifiable = it })
                    }
                    if (isQuantifiable) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = targetValue, onValueChange = { targetValue = it }, label = { Text("Cible") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unité") }, modifier = Modifier.weight(1f))
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Répétitif")
                    Switch(checked = isRepetitive, onCheckedChange = { isRepetitive = it })
                }
                
                if (isRepetitive) {
                    DaySelector(selectedDays = repeatDays, onDaysChanged = { repeatDays = it })
                } else {
                    DatePickerField(label = "Date prévue", selectedDate = scheduledDate, onDateSelected = { scheduledDate = it })
                }
                
                if (!isQuantifiable) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isAllDay, onCheckedChange = { isAllDay = it })
                        Text("Toute la journée")
                    }
                    if (!isAllDay) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TimePickerField(label = "Début", currentTime = startAt, onTimeSelected = { startAt = it }, modifier = Modifier.weight(1f))
                            TimePickerField(label = "Fin", currentTime = endAt, onTimeSelected = { endAt = it }, modifier = Modifier.weight(1f))
                        }
                    }
                }
                DropdownField(label = "Priorité", options = priorities, selectedOption = priority, onOptionSelected = { priority = it })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val target = targetValue.toFloatOrNull() ?: 0f
                onSave(routine.copy(
                    name = name, category = category, startAt = if (isAllDay || isQuantifiable) "" else startAt, 
                    endAt = if (isAllDay || isQuantifiable) "" else endAt, isAllDay = isAllDay, 
                    isRepetitive = isRepetitive, priority = priority, 
                    repeatDays = if (isRepetitive) repeatDays else emptyList(),
                    scheduledDate = if (isRepetitive) null else scheduledDate,
                    isQuantifiable = isQuantifiable, targetValue = target, unit = unit
                ))
            }) { Text("Enregistrer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

@Composable
private fun AddRoutineDialog(onDismiss: () -> Unit, onSave: (RoutineVM) -> Unit) {
    val categories = listOf("Santé", "Sport", "Bien-être", "Alimentation", "Travail", "Autre")
    val priorities = listOf("Élevée", "Moyenne", "Faible")
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categories.first()) }
    var startAt by remember { mutableStateOf("09:00") }
    var endAt by remember { mutableStateOf("10:00") }
    var isAllDay by remember { mutableStateOf(false) }
    var isRepetitive by remember { mutableStateOf(false) }
    var priority by remember { mutableStateOf(priorities[1]) }
    var repeatDays by remember { mutableStateOf(listOf(1, 2, 3, 4, 5)) }
    var scheduledDate by remember { mutableStateOf<Long?>(System.currentTimeMillis()) }
    
    var isQuantifiable by remember { mutableStateOf(false) }
    var targetValue by remember { mutableStateOf("2") }
    var unit by remember { mutableStateOf("") }

    LaunchedEffect(category) {
        if (category == "Alimentation") {
            isQuantifiable = true
            if (unit.isEmpty()) unit = "L"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false),
        title = { Text("Nouveau défi") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom du défi") }, modifier = Modifier.fillMaxWidth())
                DropdownField(label = "Catégorie", options = categories, selectedOption = category, onOptionSelected = { category = it })
                
                if (category == "Alimentation") {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Objectif quantifiable")
                        Switch(checked = isQuantifiable, onCheckedChange = { isQuantifiable = it })
                    }
                    if (isQuantifiable) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = targetValue, onValueChange = { targetValue = it }, label = { Text("Cible") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unité") }, modifier = Modifier.weight(1f))
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Répétitif")
                    Switch(checked = isRepetitive, onCheckedChange = { isRepetitive = it })
                }

                if (isRepetitive) {
                    DaySelector(selectedDays = repeatDays, onDaysChanged = { repeatDays = it })
                } else {
                    DatePickerField(label = "Date prévue", selectedDate = scheduledDate, onDateSelected = { scheduledDate = it })
                }

                if (!isQuantifiable) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isAllDay, onCheckedChange = { isAllDay = it })
                        Text("Toute la journée")
                    }
                    if (!isAllDay) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TimePickerField(label = "Début", currentTime = startAt, onTimeSelected = { startAt = it }, modifier = Modifier.weight(1f))
                            TimePickerField(label = "Fin", currentTime = endAt, onTimeSelected = { endAt = it }, modifier = Modifier.weight(1f))
                        }
                    }
                }
                DropdownField(label = "Priorité", options = priorities, selectedOption = priority, onOptionSelected = { priority = it })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onSave(RoutineVM(
                        name = name, description = "", category = category, startAt = if (isAllDay || isQuantifiable) "" else startAt,
                        endAt = if (isAllDay || isQuantifiable) "" else endAt, isAllDay = isAllDay, 
                        isRepetitive = isRepetitive, periodicity = "Personnalisé", priority = priority, 
                        repeatDays = if (isRepetitive) repeatDays else emptyList(),
                        scheduledDate = if (isRepetitive) null else scheduledDate,
                        isQuantifiable = isQuantifiable, 
                        targetValue = targetValue.toFloatOrNull() ?: 0f, unit = unit
                    ))
                }
            }) { Text("Ajouter") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

@Composable
fun DatePickerField(label: String, selectedDate: Long?, onDateSelected: (Long) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    if (selectedDate != null) calendar.timeInMillis = selectedDate
    
    val datePickerDialog = DatePickerDialog(context, { _, year, month, day ->
        val result = Calendar.getInstance().apply {
            set(year, month, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        onDateSelected(result.timeInMillis)
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dateDisplay = if (selectedDate != null) sdf.format(selectedDate) else "Sélectionner une date"

    OutlinedTextField(
        value = dateDisplay, onValueChange = {}, readOnly = true, label = { Text(label) },
        modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }, enabled = false,
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledBorderColor = MaterialTheme.colorScheme.outline
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    )
}

@Composable
fun TimePickerField(label: String, currentTime: String, onTimeSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val initialHour = currentTime.split(":").getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.HOUR_OF_DAY)
    val initialMinute = currentTime.split(":").getOrNull(1)?.toIntOrNull() ?: calendar.get(Calendar.MINUTE)

    val timePickerDialog = TimePickerDialog(context, { _, hour, minute ->
        onTimeSelected(String.format("%02d:%02d", hour, minute))
    }, initialHour, initialMinute, true)

    OutlinedTextField(
        value = currentTime, onValueChange = { }, readOnly = true, label = { Text(label) },
        modifier = modifier.clickable { timePickerDialog.show() }, enabled = false,
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledBorderColor = MaterialTheme.colorScheme.outline
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    )
}

private fun priorityWeight(priority: String): Int = when (priority) {
    "Élevée" -> 0
    "Moyenne" -> 1
    "Faible" -> 2
    else -> 3
}
