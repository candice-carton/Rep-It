package project.repit.ui.viewModel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import project.repit.model.domain.model.Routine
import project.repit.model.domain.model.RoutineHistory
import project.repit.model.domain.useCase.GetRoutineUseCase
import project.repit.models.RoutineRepository
import java.time.LocalTime
import java.time.Duration
import java.util.Calendar

class TimerViewModel(
    private val routineId: String,
    private val repository: RoutineRepository,
    private val getRoutineUseCase: GetRoutineUseCase
) : ViewModel() {

    private val _routine = mutableStateOf<Routine?>(null)
    
    private val _routineName = mutableStateOf("")
    val routineName: State<String> = _routineName

    private val _timeLeft = mutableLongStateOf(0L)
    val timeLeft: State<Long> = _timeLeft

    private val _isRunning = mutableStateOf(false)
    val isRunning: State<Boolean> = _isRunning

    private var timerJob: Job? = null
    private var originalTotalDuration: Long = 0

    init {
        viewModelScope.launch {
            getRoutineUseCase(routineId)?.let { routine ->
                _routine.value = routine
                _routineName.value = routine.name
                
                // Calculer la durée totale originelle basée sur les heures de début/fin
                val calculatedBase = try {
                    val start = LocalTime.parse(routine.startAt)
                    val end = LocalTime.parse(routine.endAt)
                    var d = Duration.between(start, end).toMillis()
                    if (d <= 0) d += Duration.ofDays(1).toMillis()
                    d
                } catch (e: Exception) {
                    30 * 60 * 1000L // 30 mins par défaut
                }
                
                originalTotalDuration = calculatedBase
                
                // Reprendre là où on s'était arrêté si possible
                _timeLeft.longValue = if (routine.remainingMillis != null && routine.remainingMillis > 0) {
                    routine.remainingMillis
                } else {
                    calculatedBase
                }
            }
        }
    }

    fun toggleTimer() {
        if (_isRunning.value) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _isRunning.value = true
        timerJob = viewModelScope.launch {
            while (_timeLeft.longValue > 0) {
                delay(1000)
                if (_isRunning.value) {
                    _timeLeft.longValue -= 1000
                }
            }
            _isRunning.value = false
        }
    }

    private fun pauseTimer() {
        _isRunning.value = false
        timerJob?.cancel()
        saveProgress(false)
    }

    /**
     * Sauvegarde l'état actuel (temps restant + progression calculée)
     */
    fun saveProgress(isFinished: Boolean = false) {
        viewModelScope.launch {
            _routine.value?.let { routine ->
                val elapsed = originalTotalDuration - _timeLeft.longValue
                var progress = if (originalTotalDuration > 0) {
                    ((elapsed.toFloat() / originalTotalDuration) * 100).toInt().coerceIn(0, 100)
                } else 0
                
                // Si on force "Terminer", on garde le progrès actuel (ex: 50% si fait 30s/1min)
                // Sauf si le timer est arrivé à bout (progress sera 100)
                
                val updatedRoutine = routine.copy(
                    remainingMillis = if (isFinished) null else _timeLeft.longValue,
                    progress = progress,
                    lastCompletedDate = if (isFinished) getTodayTimestamp() else routine.lastCompletedDate
                )
                repository.upsert(updatedRoutine)
                _routine.value = updatedRoutine
            }
        }
    }

    fun finishRoutine(onFinished: () -> Unit) {
        viewModelScope.launch {
            val elapsed = originalTotalDuration - _timeLeft.longValue
            val progress = if (originalTotalDuration > 0) {
                ((elapsed.toFloat() / originalTotalDuration) * 100).toInt().coerceIn(0, 100)
            } else 100
            
            val history = RoutineHistory(
                routineId = routineId,
                durationMillis = elapsed,
                isCompleted = true,
                progress = progress
            )
            repository.insertHistory(history)
            
            saveProgress(true)
            onFinished()
        }
    }

    private fun getTodayTimestamp(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
