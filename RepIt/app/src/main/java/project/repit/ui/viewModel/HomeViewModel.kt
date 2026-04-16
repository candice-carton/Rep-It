package project.repit.ui.viewModel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import project.repit.model.data.AppDatabase
import project.repit.model.data.RoutineRepository
import project.repit.model.data.UserPreferences
import project.repit.model.data.remote.OpenMeteoWeatherRepository
import project.repit.model.data.sensor.PedometerDataSource
import project.repit.model.domain.model.ChallengeDifficulty
import project.repit.model.domain.model.DailyChallengeSuggestion
import project.repit.model.domain.model.Routine
import project.repit.model.domain.model.WeatherCondition
import project.repit.model.domain.model.WeatherInfo
import project.repit.model.domain.useCase.DeleteRoutineUseCase
import project.repit.model.domain.useCase.GenerateDailyChallengeUseCase
import project.repit.model.domain.useCase.GetRoutineUseCase
import project.repit.model.domain.useCase.GetRoutinesUseCase
import project.repit.model.domain.useCase.RoutinesUseCases
import project.repit.model.domain.useCase.UpsertRoutineUseCase
import java.util.Calendar
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val routinesUseCases: RoutinesUseCases
    private val weatherRepository = OpenMeteoWeatherRepository()
    private val pedometerDataSource = PedometerDataSource(application)
    private val generateDailyChallengeUseCase = GenerateDailyChallengeUseCase()
    private val userPreferences = UserPreferences(application)

    private var getRoutinesJob: Job? = null
    private val _routines = mutableStateOf<List<RoutineVM>>(emptyList())

    private val _todoRoutines = mutableStateOf<List<Pair<RoutineVM, Long>>>(emptyList())
    val todoRoutines: State<List<Pair<RoutineVM, Long>>> = _todoRoutines

    private val _doneRoutines = mutableStateOf<List<RoutineVM>>(emptyList())
    val doneRoutines: State<List<RoutineVM>> = _doneRoutines

    private val _upcomingHighlights = mutableStateOf<List<RoutineVM>>(emptyList())
    val upcomingHighlights: State<List<RoutineVM>> = _upcomingHighlights

    private val _weatherInfo = mutableStateOf<WeatherInfo?>(null)
    val weatherInfo: State<WeatherInfo?> = _weatherInfo

    private val _stepsToday = mutableIntStateOf(0)
    val stepsToday: State<Int> = _stepsToday

    private val _selectedDifficulty = mutableStateOf(ChallengeDifficulty.FACILE)
    val selectedDifficulty: State<ChallengeDifficulty> = _selectedDifficulty

    private val _dailyChallenge = mutableStateOf<DailyChallengeSuggestion?>(null)
    val dailyChallenge: State<DailyChallengeSuggestion?> = _dailyChallenge

    private val _isCurrentChallengeAdded = mutableStateOf(false)
    val isCurrentChallengeAdded: State<Boolean> = _isCurrentChallengeAdded
    private val _dailyChallengeMessage = mutableStateOf<String?>(null)
    val dailyChallengeMessage: State<String?> = _dailyChallengeMessage

    private val _profileName = mutableStateOf(userPreferences.getProfileName())
    val profileName: State<String> = _profileName

    private val _profileAvatarUri = mutableStateOf(userPreferences.getProfileAvatarUri())
    val profileAvatarUri: State<String?> = _profileAvatarUri

    private val _streakDays = mutableIntStateOf(0)
    val streakDays: State<Int> = _streakDays

    private val _isStepCounterAvailable = mutableStateOf(pedometerDataSource.hasStepCounter)
    val isStepCounterAvailable: State<Boolean> = _isStepCounterAvailable

    private var refreshToken = 0

    init {
        val dao = AppDatabase.getDatabase(application).routineDao()
        val repository = RoutineRepository(dao)
        routinesUseCases = RoutinesUseCases(
            getRoutines = GetRoutinesUseCase(repository),
            getRoutine = GetRoutineUseCase(repository),
            upsertRoutine = UpsertRoutineUseCase(repository),
            deleteRoutine = DeleteRoutineUseCase(repository)
        )
        observeRoutines()
        observeSteps()
        fetchWeatherAndRefreshChallenge()
    }

    fun onEvent(event: RoutineUiEvent) {
        when (event) {
            is RoutineUiEvent.DeleteRoutine -> viewModelScope.launch {
                routinesUseCases.deleteRoutine(event.routine.toEntity())
            }
            is RoutineUiEvent.UpdateRoutine -> viewModelScope.launch {
                routinesUseCases.upsertRoutine(event.routine.toEntity())
            }
            else -> Unit
        }
    }

    fun onActivityRecognitionPermissionChanged(granted: Boolean) {
        if (granted && _isStepCounterAvailable.value) {
            pedometerDataSource.start()
        } else {
            pedometerDataSource.stop()
            _stepsToday.intValue = 0
        }
    }

    fun setChallengeDifficulty(difficulty: ChallengeDifficulty) {
        _selectedDifficulty.value = difficulty
        refreshDailyChallenge()
    }

    fun refreshDailyChallenge() {
        refreshToken += 1
        _dailyChallenge.value = generateDailyChallengeUseCase(
            difficulty = adaptDifficultyToUserStats(_selectedDifficulty.value, _routines.value),
            weather = _weatherInfo.value,
            refreshToken = refreshToken
        )
        syncChallengeAddedState()
    }

    fun addSelectedDailyChallengeToRoutines() {
        val suggestion = _dailyChallenge.value ?: return
        val todayTs = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val todayEpochDay = LocalDate.now().toEpochDay()

        if (userPreferences.getLastDailyChallengeEpochDay() == todayEpochDay) {
            _dailyChallengeMessage.value = "Tu as déjà ajouté ton défi du jour."
            return
        }

        val category = when (suggestion.difficulty) {
            ChallengeDifficulty.FACILE -> "Bien-être"
            ChallengeDifficulty.MOYEN -> "Personnel"
            ChallengeDifficulty.DIFFICILE -> "Sport"
        }

        viewModelScope.launch {
            routinesUseCases.upsertRoutine(
                Routine(
                    name = suggestion.title,
                    description = suggestion.description,
                    category = category,
                    periodicity = "Ponctuelle",
                    priority = when (suggestion.importance) {
                        3 -> "Élevée"
                        2 -> "Moyenne"
                        else -> "Faible"
                    },
                    isAllDay = true,
                    scheduledDate = todayTs,
                    isDailySuggestion = true
                )
            )
            userPreferences.setLastDailyChallengeEpochDay(todayEpochDay)
            _dailyChallengeMessage.value = "Défi du jour ajouté ✅"
        }
    }

    fun fetchWeatherAndRefreshChallenge(latitude: Double? = null, longitude: Double? = null) {
        viewModelScope.launch {
            _weatherInfo.value = if (latitude != null && longitude != null) {
                weatherRepository.getCurrentWeather(latitude, longitude)
            } else {
                weatherRepository.getCurrentWeather(45.5017, -73.5673)
            }

            _dailyChallenge.value = generateDailyChallengeUseCase(
                difficulty = adaptDifficultyToUserStats(_selectedDifficulty.value, _routines.value),
                weather = _weatherInfo.value,
                refreshToken = refreshToken
            )
            syncChallengeAddedState()
        }
    }

    private fun observeSteps() {
        pedometerDataSource.stepsToday
            .onEach { _stepsToday.intValue = it }
            .launchIn(viewModelScope)
    }

    private fun observeRoutines() {
        getRoutinesJob?.cancel()
        getRoutinesJob = routinesUseCases.getRoutines().onEach { entities ->
            val vms = entities.map { RoutineVM.fromEntity(it) }
            _routines.value = vms
            computeHomeData(vms)
            syncChallengeAddedState()
        }.launchIn(viewModelScope)
    }

    private fun syncChallengeAddedState() {
        val suggestion = _dailyChallenge.value
        val addedToday = userPreferences.getLastDailyChallengeEpochDay() == LocalDate.now().toEpochDay()
        _isCurrentChallengeAdded.value = addedToday || (suggestion != null && _routines.value.any {
            it.name == suggestion.title && it.description == suggestion.description
        })
    }

    private fun computeHomeData(routines: List<RoutineVM>) {
        val now = Calendar.getInstance()
        val todayTs = now.apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        _todoRoutines.value = routines.map { routine ->
            val nextOcc = if (routine.lastCompletedDate == todayTs) {
                if (routine.isRepetitive) routine.getNextOccurrenceTimestamp(todayTs + 86400000) else null
            } else {
                routine.getNextOccurrenceTimestamp(todayTs)
            }
            if (nextOcc != null) routine to nextOcc else null
        }.filterNotNull().sortedWith(
            compareBy<Pair<RoutineVM, Long>> { it.second }
                .thenBy { priorityWeight(it.first.priority) }
        )

        _doneRoutines.value = routines.filter { it.lastCompletedDate == todayTs }
            .sortedWith(compareBy<RoutineVM> { priorityWeight(it.priority) }.thenBy { it.startAt })

        _upcomingHighlights.value = routines.map { routine ->
            val nextOccurrence = routine.getNextOccurrenceTimestamp(todayTs)
            if (routine.isRepetitive && nextOccurrence == todayTs) {
                routine to routine.getNextOccurrenceTimestamp(todayTs + 86400000)
            } else {
                routine to nextOccurrence
            }
        }.filter { (_, next) ->
            next > todayTs
        }.sortedWith(
            compareBy<Pair<RoutineVM, Long>> { it.second }
                .thenBy { priorityWeight(it.first.priority) }
        ).map { it.first }.take(5)

        _streakDays.intValue = computeStreakDays(routines)
    }

    private fun priorityWeight(priority: String): Int = when (priority) {
        "Élevée" -> 0
        "Moyenne" -> 1
        "Faible" -> 2
        else -> 3
    }

    fun getWeatherLabel(): String {
        val weather = _weatherInfo.value ?: return "Météo indisponible"
        val condition = when (weather.condition) {
            WeatherCondition.SUNNY -> "Ensoleillé"
            WeatherCondition.CLOUDY -> "Nuageux"
            WeatherCondition.RAINY -> "Pluvieux"
            WeatherCondition.UNKNOWN -> "Variable"
        }
        return "$condition, ${weather.temperatureCelsius}°C"
    }

    fun refreshProfileData() {
        _profileName.value = userPreferences.getProfileName()
        _profileAvatarUri.value = userPreferences.getProfileAvatarUri()
    }

    fun clearDailyChallengeMessage() {
        _dailyChallengeMessage.value = null
    }

    private fun adaptDifficultyToUserStats(
        requestedDifficulty: ChallengeDifficulty,
        routines: List<RoutineVM>
    ): ChallengeDifficulty {
        val completedHardCount = routines.count {
            it.lastCompletedDate != null && it.priority == "Élevée" && it.progress >= 100
        }

        return when {
            completedHardCount >= 20 -> ChallengeDifficulty.DIFFICILE
            completedHardCount >= 10 && requestedDifficulty == ChallengeDifficulty.FACILE -> ChallengeDifficulty.MOYEN
            else -> requestedDifficulty
        }
    }

    private fun computeStreakDays(routines: List<RoutineVM>): Int {
        val zone = ZoneId.systemDefault()
        val completedDays = routines.filter { it.isDailySuggestion }
            .mapNotNull { it.lastCompletedDate }
            .map { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
            .toSet()

        var streak = 0
        var cursor = LocalDate.now()
        while (completedDays.contains(cursor)) {
            streak += 1
            cursor = cursor.minusDays(1)
        }
        return streak
    }

    override fun onCleared() {
        super.onCleared()
        pedometerDataSource.stop()
    }
}
