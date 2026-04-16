package project.repit.ui.views

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import project.repit.model.domain.model.ChallengeDifficulty
import project.repit.model.domain.model.DailyChallengeSuggestion
import project.repit.ui.components.AppPage
import project.repit.ui.components.RoutineBox
import project.repit.ui.theme.SoftViolet
import project.repit.ui.viewModel.HomeViewModel
import project.repit.ui.viewModel.RoutineUiEvent
import project.repit.ui.viewModel.RoutineVM
import java.util.Calendar

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val todoRoutines by viewModel.todoRoutines
    val doneRoutines by viewModel.doneRoutines
    val upcomingHighlights by viewModel.upcomingHighlights
    val selectedDifficulty by viewModel.selectedDifficulty
    val dailyChallenge by viewModel.dailyChallenge
    val isCurrentChallengeAdded by viewModel.isCurrentChallengeAdded
    val dailyChallengeMessage by viewModel.dailyChallengeMessage
    val stepsToday by viewModel.stepsToday
    val isStepCounterAvailable by viewModel.isStepCounterAvailable
    val profileName by viewModel.profileName
    val profileAvatarUri by viewModel.profileAvatarUri
    val streakDays by viewModel.streakDays

    var updatingRoutineValue by remember { mutableStateOf<RoutineVM?>(null) }

    var hasActivityPermission by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val hasLocationPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    LaunchedEffect(hasActivityPermission) {
        viewModel.onActivityRecognitionPermissionChanged(hasActivityPermission)
    }
    LaunchedEffect(Unit) { viewModel.refreshProfileData() }

    val activityPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasActivityPermission = granted
            viewModel.onActivityRecognitionPermissionChanged(granted)
        }
    )

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { result ->
            val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            hasLocationPermission.value = granted
            if (granted) {
                fetchWeatherFromDeviceLocation(context, viewModel)
            }
        }
    )

    val todayTimestamp = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        HeaderSection(
            weatherText = viewModel.getWeatherLabel(),
            stepsToday = stepsToday,
            isStepCounterAvailable = isStepCounterAvailable,
            hasActivityPermission = hasActivityPermission,
            onRequestActivityPermission = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    activityPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                }
            },
            onRefreshWeather = {
                if (hasLocationPermission.value) {
                    fetchWeatherFromDeviceLocation(context, viewModel)
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    )
                }
            },
            profileName = profileName,
            profileAvatarUri = profileAvatarUri,
            streakDays = streakDays,
            onOpenProfile = { navController.navigate(AppPage.Profile.name) }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DailyChallengeCard(
                    selectedDifficulty = selectedDifficulty,
                    suggestion = dailyChallenge,
                    isAlreadyAdded = isCurrentChallengeAdded,
                    onDifficultySelected = viewModel::setChallengeDifficulty,
                    onRefresh = viewModel::refreshDailyChallenge,
                    onAddToMyChallenges = viewModel::addSelectedDailyChallengeToRoutines
                )
                if (!dailyChallengeMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = dailyChallengeMessage ?: "",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    LaunchedEffect(dailyChallengeMessage) { viewModel.clearDailyChallengeMessage() }
                }
            }

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
                    onEdit = { },
                    onDelete = { viewModel.onEvent(RoutineUiEvent.DeleteRoutine(routine)) },
                    isUpcoming = nextOcc > todayTimestamp,
                    forcedDate = nextOcc,
                    onStart = {
                        handleStartAction(routine, todayTimestamp, navController, viewModel) { updatingRoutineValue = it }
                    }
                )
            }

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
                    onEdit = { },
                    onDelete = { viewModel.onEvent(RoutineUiEvent.DeleteRoutine(routine)) }
                )
            }

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
                    onEdit = { },
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

private fun fetchWeatherFromDeviceLocation(context: Context, viewModel: HomeViewModel) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)

    val bestLocation = providers
        .filter { provider -> locationManager.isProviderEnabled(provider) }
        .mapNotNull { provider ->
            runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
        }
        .maxByOrNull { it.time }

    if (bestLocation != null) {
        viewModel.fetchWeatherAndRefreshChallenge(bestLocation.latitude, bestLocation.longitude)
    } else {
        viewModel.fetchWeatherAndRefreshChallenge()
    }
}

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
private fun DailyChallengeCard(
    selectedDifficulty: ChallengeDifficulty,
    suggestion: DailyChallengeSuggestion?,
    isAlreadyAdded: Boolean,
    onDifficultySelected: (ChallengeDifficulty) -> Unit,
    onRefresh: () -> Unit,
    onAddToMyChallenges: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Défi aléatoire du jour", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, contentDescription = "Rafraîchir") }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(ChallengeDifficulty.FACILE, ChallengeDifficulty.MOYEN, ChallengeDifficulty.DIFFICILE).forEach { difficulty ->
                    FilterChip(
                        selected = difficulty == selectedDifficulty,
                        onClick = { onDifficultySelected(difficulty) },
                        label = { Text(difficulty.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            if (suggestion == null) {
                Text("Aucun défi disponible pour ce contexte.", style = MaterialTheme.typography.bodySmall)
            } else {
                Text(suggestion.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(suggestion.description, style = MaterialTheme.typography.bodyMedium)
                Text("Importance: ${suggestion.importance}/3", style = MaterialTheme.typography.bodySmall)
                Button(onClick = onAddToMyChallenges, enabled = !isAlreadyAdded) {
                    Text(if (isAlreadyAdded) "Déjà ajouté" else "Choisir ce défi")
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Text(text, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        TextButton(onClick = onSeeAll) { Text("Voir tout", color = SoftViolet) }
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
                onSave(routine.copy(currentValue = total, progress = progress, lastCompletedDate = if (isCompleted) todayStart else routine.lastCompletedDate))
            }) { Text("Ajouter") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

@Composable
fun HeaderSection(
    weatherText: String,
    stepsToday: Int,
    isStepCounterAvailable: Boolean,
    hasActivityPermission: Boolean,
    onRequestActivityPermission: () -> Unit,
    onRefreshWeather: () -> Unit,
    profileName: String,
    profileAvatarUri: String?,
    streakDays: Int,
    onOpenProfile: () -> Unit
) {
    Surface(
        color = SoftViolet,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
    ) {
        Column(modifier = Modifier.padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 32.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Bonjour ${profileName.substringBefore(" ")} !", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Prêt pour ton défi du jour ?", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                }
                Box(modifier = Modifier.padding(4.dp)) {
                    ProfileIcon(
                        letter = profileName.firstOrNull()?.uppercase() ?: "R",
                        score = streakDays,
                        avatarUri = profileAvatarUri,
                        onClick = onOpenProfile
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            WeatherAndStepsInfo(
                weatherText = weatherText,
                stepsToday = stepsToday,
                isStepCounterAvailable = isStepCounterAvailable,
                hasActivityPermission = hasActivityPermission,
                onRequestActivityPermission = onRequestActivityPermission,
                onRefreshWeather = onRefreshWeather
            )
        }
    }
}

@Composable
private fun ProfileIcon(letter: String, score: Int, avatarUri: String?, onClick: () -> Unit) {
    val context = LocalContext.current
    val bitmap = remember(avatarUri) {
        avatarUri?.let { uri ->
            runCatching {
                context.contentResolver.openInputStream(Uri.parse(uri))?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }.getOrNull()
        }
    }

    Box(
        contentAlignment = Alignment.BottomEnd,
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onClick() }
            .padding(2.dp)
    ) {
        Surface(modifier = Modifier.size(50.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.2f)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Photo de profil",
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Text(letter, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
        }
        Surface(modifier = Modifier.size(24.dp), shape = CircleShape, color = Color(0xFFFBC02D)) {
            Box(contentAlignment = Alignment.Center) { Text(score.toString(), color = SoftViolet, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun WeatherAndStepsInfo(
    weatherText: String,
    stepsToday: Int,
    isStepCounterAvailable: Boolean,
    hasActivityPermission: Boolean,
    onRequestActivityPermission: () -> Unit,
    onRefreshWeather: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFFFD600), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(weatherText, color = Color.White, fontSize = 14.sp)
                TextButton(onClick = onRefreshWeather, contentPadding = PaddingValues(0.dp)) { Text("Actualiser météo", color = Color.White) }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(stepsToday.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            when {
                !isStepCounterAvailable -> Text("Capteur indisponible", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                hasActivityPermission -> Text("/ 10,000 pas", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                else -> TextButton(onClick = onRequestActivityPermission, contentPadding = PaddingValues(0.dp)) { Text("Autoriser podomètre", color = Color.White) }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, tag: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (tag.isNotBlank()) {
            Surface(color = if (tag == "Bravo") Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFF2DCE89).copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                Text(tag, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = if (tag == "Bravo") Color(0xFF2E7D32) else Color(0xFF2DCE89), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
