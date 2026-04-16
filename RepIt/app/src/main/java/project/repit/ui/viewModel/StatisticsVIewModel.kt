package project.repit.ui.viewModel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import project.repit.model.data.AppDatabase
import project.repit.model.data.RoutineRepository
import project.repit.model.data.UserPreferences
import project.repit.model.data.WaterLog
import project.repit.model.data.WeightLog
import project.repit.model.domain.model.Routine
import project.repit.model.domain.useCase.GetRoutinesUseCase
import java.time.LocalDate

data class StatisticsUiState(
    val totalChallenges: Int = 0,
    val completedChallenges: Int = 0,
    val completionRate: Int = 0,
    val byCategory: Map<String, Int> = emptyMap(),
    val averageProgress: Int = 0,
    val waterTodayLiters: Float = 0f,
    val waterWeeklyAverage: Float = 0f,
    val currentWeightKg: Float = 0f,
    val targetWeightKg: Float = 0f,
    val weightLogs: List<WeightLog> = emptyList(),
    val waterLogs: List<WaterLog> = emptyList()
)

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RoutineRepository(AppDatabase.getDatabase(application).routineDao())
    private val getRoutinesUseCase = GetRoutinesUseCase(repository)
    private val userPreferences = UserPreferences(application)

    private val _uiState = mutableStateOf(StatisticsUiState())
    val uiState: State<StatisticsUiState> = _uiState

    init {
        observeStats()
    }

    fun addWaterEntry(liters: Float) {
        if (liters <= 0f) return
        userPreferences.addWaterLog(liters)
        _uiState.value = _uiState.value.copy(
            waterLogs = userPreferences.getWaterLogs(),
            waterTodayLiters = userPreferences.getWaterLogs().lastOrNull { it.epochDay == LocalDate.now().toEpochDay() }?.liters ?: 0f,
            waterWeeklyAverage = computeWeeklyWaterAverage(userPreferences.getWaterLogs())
        )
    }

    fun addWeightEntry(weight: Float) {
        if (weight <= 0f) return
        userPreferences.addWeightLog(weight)
        _uiState.value = _uiState.value.copy(
            currentWeightKg = userPreferences.getCurrentWeightKg(),
            weightLogs = userPreferences.getWeightLogs()
        )
    }

    private fun observeStats() {
        getRoutinesUseCase().onEach { routines ->
            _uiState.value = routines.toStatsUiState()
        }.launchIn(viewModelScope)
    }

    private fun List<Routine>.toStatsUiState(): StatisticsUiState {
        val total = size
        val completed = count { it.progress >= 100 }
        val rate = if (total == 0) 0 else ((completed.toFloat() / total) * 100).toInt()
        val averageProgress = if (total == 0) 0 else map { it.progress }.average().toInt()
        val grouped = groupBy { it.category }.mapValues { (_, values) -> values.size }
        val waterLogs = userPreferences.getWaterLogs()
        val weightLogs = userPreferences.getWeightLogs()
        val todayWater = waterLogs.lastOrNull { it.epochDay == LocalDate.now().toEpochDay() }?.liters ?: 0f

        return StatisticsUiState(
            totalChallenges = total,
            completedChallenges = completed,
            completionRate = rate,
            byCategory = grouped,
            averageProgress = averageProgress,
            waterTodayLiters = todayWater,
            waterWeeklyAverage = computeWeeklyWaterAverage(waterLogs),
            currentWeightKg = userPreferences.getCurrentWeightKg(),
            targetWeightKg = userPreferences.getTargetWeightKg(),
            weightLogs = weightLogs,
            waterLogs = waterLogs
        )
    }

    private fun computeWeeklyWaterAverage(entries: List<WaterLog>): Float {
        val today = LocalDate.now().toEpochDay()
        val weekEntries = entries.filter { it.epochDay in (today - 6)..today }
        if (weekEntries.isEmpty()) return 0f
        return weekEntries.map { it.liters }.average().toFloat()
    }
}
