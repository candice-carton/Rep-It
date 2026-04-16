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
import project.repit.model.domain.model.Routine
import project.repit.model.domain.useCase.GetRoutinesUseCase

data class StatisticsUiState(
    val totalChallenges: Int = 0,
    val completedChallenges: Int = 0,
    val completionRate: Int = 0,
    val byCategory: Map<String, Int> = emptyMap()
)

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RoutineRepository(AppDatabase.getDatabase(application).routineDao())
    private val getRoutinesUseCase = GetRoutinesUseCase(repository)

    private val _uiState = mutableStateOf(StatisticsUiState())
    val uiState: State<StatisticsUiState> = _uiState

    init {
        observeStats()
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
        val grouped = groupBy { it.category }.mapValues { (_, values) -> values.size }

        return StatisticsUiState(
            totalChallenges = total,
            completedChallenges = completed,
            completionRate = rate,
            byCategory = grouped
        )
    }
}
