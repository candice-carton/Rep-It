package project.repit.ui.views

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import project.repit.model.domain.model.ChallengeDifficulty
import project.repit.model.domain.useCase.ChallengeCatalog
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

// --- CONSTANTES ---
private val CATEGORIES = listOf("Santé", "Sport", "Bien-être", "Alimentation", "Travail", "Personnel", "Autre")
private val PRIORITIES = listOf("Élevée", "Moyenne", "Faible")
private val CREATION_MODES = listOf("Défi", "Série")
private val SOURCE_MODES = listOf("Base de données", "Personnalisé")
private val DIFFICULTY_OPTIONS = listOf(
    "Facile" to ChallengeDifficulty.FACILE,
    "Moyen" to ChallengeDifficulty.MOYEN,
    "Difficile" to ChallengeDifficulty.DIFFICILE
)

/**
 * Écran principal de gestion des routines.
 */
@Composable
fun RoutineScreen(
    navController: NavController,
    viewModel: RoutineViewModel = viewModel()
) {
    val context = LocalContext.current
    val todayRoutines by viewModel.todayRoutines
    val upcomingRoutines by viewModel.upcomingRoutines
    val selectedCategory by viewModel.selectedCategory
    
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

    Column(modifier = Modifier.fillMaxSize()) {
        CategoryTabs(
            categories = listOf("Tous") + CATEGORIES,
            selectedCategory = selectedCategory,
            onCategorySelected = { 
                viewModel.onEvent(RoutineUiEvent.FilterByCategory(it))
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { HeaderRow(onAddClick = { isAddingRoutine = true }) }
            item { SectionHeader("Aujourd'hui", MaterialTheme.colorScheme.primary) }
            if (todayRoutines.isEmpty()) {
                item { EmptyStateMessage("Aucun défi pour aujourd'hui.") }
            } else {
                items(todayRoutines, key = { it.id }) { routine ->
                    RoutineItem(
                        routine = routine,
                        onEdit = { editingRoutine = routine },
                        onDelete = { viewModel.onEvent(RoutineUiEvent.DeleteRoutine(routine)) },
                        onStart = {
                            handleRoutineStart(routine, hasNotificationPermission, permissionLauncher, navController, viewModel) { updatingRoutineValue = it }
                        }
                    )
                }
            }

            item { SectionHeader("À venir", MaterialTheme.colorScheme.secondary) }
            if (upcomingRoutines.isEmpty()) {
                item { EmptyStateMessage("Aucun défi à venir.") }
            } else {
                items(upcomingRoutines, key = { it.id }) { routine ->
                    RoutineItem(
                        routine = routine,
                        onEdit = { editingRoutine = routine },
                        onDelete = { viewModel.onEvent(RoutineUiEvent.DeleteRoutine(routine)) },
                        onStart = {
                            handleRoutineStart(routine, hasNotificationPermission, permissionLauncher, navController, viewModel) { updatingRoutineValue = it }
                        }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
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
}

@Composable
private fun HeaderRow(onAddClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Mes Défis", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Button(onClick = onAddClick) { Text("Nouveau") }
    }
}

@Composable
private fun EmptyStateMessage(message: String) {
    Text(text = message, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun CategoryTabs(categories: List<String>, selectedCategory: String, onCategorySelected: (String) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = selectedCategory == category
            AssistChip(
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String, color: Color) {
    Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
}

@Composable
private fun RoutineItem(routine: RoutineVM, onEdit: () -> Unit, onDelete: () -> Unit, onStart: () -> Unit) {
    RoutineBox(routine = routine, onEdit = onEdit, onDelete = onDelete, onStart = onStart)
}

private fun handleRoutineStart(routine: RoutineVM, hasNotificationPermission: Boolean, permissionLauncher: androidx.activity.result.ActivityResultLauncher<String>, navController: NavController, viewModel: RoutineViewModel, onUpdateQuantifiable: (RoutineVM) -> Unit) {
    if (routine.isQuantifiable) {
        onUpdateQuantifiable(routine)
    } else if (routine.isAllDay) {
        val today = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
        viewModel.onEvent(RoutineUiEvent.UpdateRoutine(routine.copy(lastCompletedDate = today, progress = 100)))
    } else {
        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            navController.navigate("${AppPage.Timer.name}/${routine.id}")
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
                Text("Entrez la valeur à ajouter (${routine.unit})")
                OutlinedTextField(value = newValue, onValueChange = { newValue = it }, label = { Text("Valeur") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val added = newValue.toFloatOrNull() ?: 0f
                val total = routine.currentValue + added
                val progress = ((total / routine.targetValue) * 100).toInt().coerceIn(0, 100)
                val todayStart = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
                val isCompleted = total >= routine.targetValue
                onSave(routine.copy(currentValue = total, progress = progress, lastCompletedDate = if (isCompleted) todayStart.timeInMillis else routine.lastCompletedDate))
            }) { Text("Ajouter") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

@Composable
private fun EditRoutineDialog(routine: RoutineVM, onDismiss: () -> Unit, onSave: (RoutineVM) -> Unit) {
    var name by remember(routine) { mutableStateOf(routine.name) }
    var nameError by remember { mutableStateOf(false) }
    var category by remember(routine) { mutableStateOf(routine.category) }
    var startAt by remember(routine) { mutableStateOf(if(routine.startAt.isEmpty()) "09:00" else routine.startAt) }
    var endAt by remember(routine) { mutableStateOf(if(routine.endAt.isEmpty()) "10:00" else routine.endAt) }
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
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    OutlinedTextField(
                        value = name, 
                        onValueChange = { name = it; nameError = false }, 
                        label = { Text(if (nameError) "Nom (requis)" else "Nom") }, 
                        modifier = Modifier.fillMaxWidth(),
                        isError = nameError
                    )
                }
                item { DropdownField(label = "Catégorie", options = CATEGORIES, selectedOption = category, onOptionSelected = { category = it }) }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Objectif quantifiable")
                        Switch(checked = isQuantifiable, onCheckedChange = { isQuantifiable = it })
                    }
                }
                if (isQuantifiable) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = targetValue, onValueChange = { targetValue = it }, label = { Text("Cible") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unité") }, modifier = Modifier.weight(1f))
                        }
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Répétitif")
                        Switch(checked = isRepetitive, onCheckedChange = { isRepetitive = it })
                    }
                }
                if (isRepetitive) { item { DaySelector(selectedDays = repeatDays, onDaysChanged = { repeatDays = it }) } }
                else { item { DatePickerField(label = "Date prévue", selectedDate = scheduledDate, onDateSelected = { scheduledDate = it }) } }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isAllDay, onCheckedChange = { isAllDay = it })
                        Text("Toute la journée")
                    }
                }
                if (!isAllDay) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TimePickerField(label = "Début", currentTime = startAt, onTimeSelected = { startAt = it }, modifier = Modifier.weight(1f))
                            TimePickerField(label = "Fin", currentTime = endAt, onTimeSelected = { endAt = it }, modifier = Modifier.weight(1f))
                        }
                    }
                }
                item { DropdownField(label = "Priorité", options = PRIORITIES, selectedOption = priority, onOptionSelected = { priority = it }) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank()) { nameError = true } else {
                    val target = targetValue.toFloatOrNull() ?: 0f
                    onSave(routine.copy(name = name, category = category, startAt = if (isAllDay) "" else startAt, endAt = if (isAllDay) "" else endAt, isAllDay = isAllDay, isRepetitive = isRepetitive, priority = priority, repeatDays = if (isRepetitive) repeatDays else emptyList(), scheduledDate = if (isRepetitive) null else scheduledDate, isQuantifiable = isQuantifiable, targetValue = target, unit = unit))
                }
            }) { Text("Enregistrer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

@Composable
private fun AddRoutineDialog(onDismiss: () -> Unit, onSave: (RoutineVM) -> Unit) {
    var creationMode by remember { mutableStateOf(CREATION_MODES.first()) }
    var sourceMode by remember { mutableStateOf(SOURCE_MODES.first()) }
    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf(CATEGORIES.first()) }
    var startAt by remember { mutableStateOf("09:00") }
    var endAt by remember { mutableStateOf("10:00") }
    var isAllDay by remember { mutableStateOf(false) }
    var isRepetitive by remember { mutableStateOf(false) }
    var priority by remember { mutableStateOf(PRIORITIES[1]) }
    var repeatDays by remember { mutableStateOf(listOf(1, 2, 3, 4, 5)) }
    var scheduledDate by remember { mutableStateOf<Long?>(System.currentTimeMillis()) }
    var isQuantifiable by remember { mutableStateOf(false) }
    var targetValue by remember { mutableStateOf("2") }
    var unit by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf(DIFFICULTY_OPTIONS.first().second) }
    var selectedCatalogCategory by remember { mutableStateOf("Tous") }
    var selectedCatalogId by remember { mutableStateOf<String?>(null) }
    val filteredCatalogEntries = remember(selectedDifficulty, selectedCatalogCategory) { ChallengeCatalog.entries.filter { entry -> entry.difficulty == selectedDifficulty && (selectedCatalogCategory == "Tous" || entry.category == selectedCatalogCategory) } }
    val selectedCatalogEntry = filteredCatalogEntries.firstOrNull { it.id == selectedCatalogId } ?: filteredCatalogEntries.firstOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false),
        title = { Text("Nouveau ${creationMode.lowercase()}") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { DropdownField(label = "Type", options = CREATION_MODES, selectedOption = creationMode, onOptionSelected = { creationMode = it }) }
                item { DropdownField(label = "Source", options = SOURCE_MODES, selectedOption = sourceMode, onOptionSelected = { sourceMode = it }) }
                if (sourceMode == "Base de données") {
                    item { DropdownField(label = "Difficulté", options = DIFFICULTY_OPTIONS.map { it.first }, selectedOption = DIFFICULTY_OPTIONS.first { it.second == selectedDifficulty }.first, onOptionSelected = { label -> selectedDifficulty = DIFFICULTY_OPTIONS.first { it.first == label }.second }) }
                    item { DropdownField(label = "Catégorie", options = listOf("Tous") + CATEGORIES, selectedOption = selectedCatalogCategory, onOptionSelected = { selectedCatalogCategory = it }) }
                    if (filteredCatalogEntries.isNotEmpty()) {
                        item { DropdownField(label = if (creationMode == "Série") "Série proposée" else "Défi proposé", options = filteredCatalogEntries.map { "${it.title} — ${it.description}" }, selectedOption = selectedCatalogEntry?.let { "${it.title} — ${it.description}" }.orEmpty(), onOptionSelected = { selectedLabel -> selectedCatalogId = filteredCatalogEntries.firstOrNull { "${it.title} — ${it.description}" == selectedLabel }?.id }) }
                    }
                } else {
                    item { OutlinedTextField(value = name, onValueChange = { name = it; nameError = false }, label = { Text(if (nameError) "Nom (requis)" else "Nom") }, modifier = Modifier.fillMaxWidth(), isError = nameError) }
                    item { DropdownField(label = "Catégorie", options = CATEGORIES, selectedOption = category, onOptionSelected = { category = it }) }
                }
                val effectiveSeries = creationMode == "Série"
                val effectiveRepetitive = effectiveSeries || isRepetitive
                val canConfigureQuantifiable = sourceMode == "Personnalisé"
                val effectiveQuantifiable = if (canConfigureQuantifiable) isQuantifiable else (selectedCatalogEntry?.isQuantifiable ?: false)
                if (canConfigureQuantifiable) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Objectif quantifiable")
                            Switch(checked = isQuantifiable, onCheckedChange = { isQuantifiable = it })
                        }
                    }
                }
                if (effectiveQuantifiable) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = if (canConfigureQuantifiable) targetValue else (selectedCatalogEntry?.targetValue ?: 1).toString(), onValueChange = { targetValue = it }, label = { Text("Cible") }, modifier = Modifier.weight(1f), enabled = canConfigureQuantifiable)
                            OutlinedTextField(value = if (canConfigureQuantifiable) unit else if (effectiveQuantifiable) "unités" else "", onValueChange = { unit = it }, label = { Text("Unité") }, modifier = Modifier.weight(1f), enabled = canConfigureQuantifiable)
                        }
                    }
                }
                if (!effectiveSeries) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Répétitif")
                            Switch(checked = isRepetitive, onCheckedChange = { isRepetitive = it })
                        }
                    }
                }
                if (effectiveRepetitive) { item { DaySelector(selectedDays = repeatDays, onDaysChanged = { repeatDays = it }) } }
                else { item { DatePickerField(label = "Date prévue", selectedDate = scheduledDate, onDateSelected = { scheduledDate = it }) } }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isAllDay, onCheckedChange = { isAllDay = it })
                        Text("Toute la journée")
                    }
                }
                if (!isAllDay) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TimePickerField(label = "Début", currentTime = startAt, onTimeSelected = { startAt = it }, modifier = Modifier.weight(1f))
                            TimePickerField(label = "Fin", currentTime = endAt, onTimeSelected = { endAt = it }, modifier = Modifier.weight(1f))
                        }
                    }
                }
                item { DropdownField(label = "Priorité", options = PRIORITIES, selectedOption = priority, onOptionSelected = { priority = it }) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val finalName = if (sourceMode == "Base de données") selectedCatalogEntry?.title.orEmpty() else name
                if (finalName.isBlank()) { nameError = true } else {
                    onSave(RoutineVM(name = finalName, description = if (sourceMode == "Base de données") (selectedCatalogEntry?.description ?: "") else "", category = if (sourceMode == "Base de données") selectedCatalogEntry?.category ?: category else category, startAt = if (isAllDay) "" else startAt, endAt = if (isAllDay) "" else endAt, isAllDay = isAllDay, isRepetitive = creationMode == "Série" || isRepetitive, periodicity = if (creationMode == "Série") "Série" else "Personnalisé", priority = if (sourceMode == "Base de données") (when(selectedDifficulty){ ChallengeDifficulty.FACILE->"Faible"; ChallengeDifficulty.MOYEN->"Moyenne"; else->"Élevée" }) else priority, repeatDays = if (creationMode == "Série" || isRepetitive) repeatDays else emptyList(), scheduledDate = if (creationMode == "Série" || isRepetitive) null else scheduledDate, isQuantifiable = if (sourceMode == "Base de données") selectedCatalogEntry?.isQuantifiable == true else isQuantifiable, targetValue = if (sourceMode == "Base de données") (selectedCatalogEntry?.targetValue ?: 1).toFloat() else (targetValue.toFloatOrNull() ?: 0f), unit = if (sourceMode == "Base de données") (if (selectedCatalogEntry?.isQuantifiable == true) "unités" else "") else unit))
                }
            }) { Text("Ajouter") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

@Composable
fun DatePickerField(label: String, selectedDate: Long?, onDateSelected: (Long) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply { if (selectedDate != null) timeInMillis = selectedDate }
    val datePickerDialog = DatePickerDialog(context, { _, y, m, d -> onDateSelected(Calendar.getInstance().apply { set(y, m, d, 0, 0, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis) }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
    OutlinedTextField(value = if (selectedDate != null) SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate) else "Sélectionner une date", onValueChange = {}, readOnly = true, label = { Text(label) }, modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant, disabledBorderColor = MaterialTheme.colorScheme.outline), shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
}

@Composable
fun TimePickerField(label: String, currentTime: String, onTimeSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val h = currentTime.split(":").getOrNull(0)?.toIntOrNull() ?: 9
    val m = currentTime.split(":").getOrNull(1)?.toIntOrNull() ?: 0
    val timePickerDialog = TimePickerDialog(context, { _, hour, min -> onTimeSelected(String.format("%02d:%02d", hour, min)) }, h, m, true)
    OutlinedTextField(value = currentTime, onValueChange = { }, readOnly = true, label = { Text(label) }, modifier = modifier.clickable { timePickerDialog.show() }, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant, disabledBorderColor = MaterialTheme.colorScheme.outline), shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
}
