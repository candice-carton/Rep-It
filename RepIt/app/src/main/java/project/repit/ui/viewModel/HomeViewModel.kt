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
import project.repit.model.data.remote.OpenMeteoWeatherRepository
import project.repit.model.data.sensor.PedometerDataSource
import project.repit.model.domain.model.ChallengeDifficulty
import project.repit.model.domain.model.DailyChallengeSuggestion
import project.repit.model.domain.model.WeatherCondition
import project.repit.model.domain.model.WeatherInfo
import project.repit.model.domain.useCase.DeleteRoutineUseCase
import project.repit.model.domain.useCase.GenerateDailyChallengeUseCase
import project.repit.model.domain.useCase.GetRoutineUseCase
import project.repit.model.domain.useCase.GetRoutinesUseCase
import project.repit.model.domain.useCase.RoutinesUseCases
import project.repit.model.domain.useCase.UpsertRoutineUseCase
import java.util.Calendar

/**
 * ViewModel dédié à l'écran d'accueil (Home).
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val routinesUseCases: RoutinesUseCases
    private val weatherRepository = OpenMeteoWeatherRepository()
    private val pedometerDataSource = PedometerDataSource(application)
    private val generateDailyChallengeUseCase = GenerateDailyChallengeUseCase()

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
        pedometerDataSource.start()
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

    fun setChallengeDifficulty(difficulty: ChallengeDifficulty) {
        _selectedDifficulty.value = difficulty
        refreshDailyChallenge()
    }

    fun refreshDailyChallenge() {
        refreshToken += 1
        _dailyChallenge.value = generateDailyChallengeUseCase(
            difficulty = _selectedDifficulty.value,
            weather = _weatherInfo.value,
            refreshToken = refreshToken
        )
    }

    fun fetchWeatherAndRefreshChallenge(latitude: Double = 45.5017, longitude: Double = -73.5673) {
        viewModelScope.launch {
            _weatherInfo.value = weatherRepository.getCurrentWeather(latitude, longitude)
            _dailyChallenge.value = generateDailyChallengeUseCase(
                difficulty = _selectedDifficulty.value,
                weather = _weatherInfo.value,
                refreshToken = refreshToken
            )
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
        }.launchIn(viewModelScope)
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

        _upcomingHighlights.value = routines.filter {
            val next = it.getNextOccurrenceTimestamp(todayTs + 86400000)
            next > todayTs
        }.sortedWith(
            compareBy<RoutineVM> { it.getNextOccurrenceTimestamp(todayTs + 86400000) }
                .thenBy { priorityWeight(it.priority) }
        ).take(5)
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

    override fun onCleared() {
        super.onCleared()
        pedometerDataSource.stop()
    }
}
