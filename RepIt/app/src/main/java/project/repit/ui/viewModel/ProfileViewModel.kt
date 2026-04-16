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
import project.repit.model.domain.useCase.GetRoutinesUseCase

data class ProfileSeriesUi(
    val id: String,
    val name: String,
    val completedSessions: Int,
    val targetSessions: Int,
    val completionRate: Int
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferences = UserPreferences(application)
    private val getRoutinesUseCase = GetRoutinesUseCase(
        RoutineRepository(AppDatabase.getDatabase(application).routineDao())
    )

    private val _name = mutableStateOf(userPreferences.getProfileName())
    val name: State<String> = _name

    private val _avatarUri = mutableStateOf(userPreferences.getProfileAvatarUri())
    val avatarUri: State<String?> = _avatarUri

    private val _age = mutableStateOf(userPreferences.getAge())
    val age: State<Int> = _age

    private val _heightCm = mutableStateOf(userPreferences.getHeightCm())
    val heightCm: State<Int> = _heightCm

    private val _weightKg = mutableStateOf(userPreferences.getCurrentWeightKg())
    val weightKg: State<Float> = _weightKg

    private val _targetWeightKg = mutableStateOf(userPreferences.getTargetWeightKg())
    val targetWeightKg: State<Float> = _targetWeightKg

    private val _activeSeries = mutableStateOf<List<ProfileSeriesUi>>(emptyList())
    val activeSeries: State<List<ProfileSeriesUi>> = _activeSeries

    init {
        observeStartedSeries()
    }

    fun updateName(value: String) {
        _name.value = value
        userPreferences.setProfileName(value.trim().ifBlank { "Rép-it User" })
    }

    fun updateAvatarUri(value: String?) {
        _avatarUri.value = value
        userPreferences.setProfileAvatarUri(value)
    }

    fun updateAge(value: Int) {
        _age.value = value
        userPreferences.setAge(value)
    }

    fun updateHeightCm(value: Int) {
        _heightCm.value = value
        userPreferences.setHeightCm(value)
    }

    fun updateWeightKg(value: Float) {
        _weightKg.value = value
        userPreferences.setCurrentWeightKg(value)
        userPreferences.addWeightLog(value)
    }

    fun updateTargetWeightKg(value: Float) {
        _targetWeightKg.value = value
        userPreferences.setTargetWeightKg(value)
    }

    private fun observeStartedSeries() {
        getRoutinesUseCase().onEach { routines ->
            _activeSeries.value = routines
                .filter { it.isRepetitive }
                .sortedByDescending { it.progress }
                .map { routine ->
                    val target = if (routine.repeatDays.isNotEmpty()) routine.repeatDays.size * 4 else 30
                    val completed = ((routine.progress / 100f) * target).toInt().coerceIn(0, target)
                    ProfileSeriesUi(
                        id = routine.id,
                        name = routine.name,
                        completedSessions = completed,
                        targetSessions = target,
                        completionRate = routine.progress.coerceIn(0, 100)
                    )
                }
        }.launchIn(viewModelScope)
    }
}
