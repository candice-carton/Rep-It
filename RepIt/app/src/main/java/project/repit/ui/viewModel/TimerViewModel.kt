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
import project.repit.model.data.RoutineRepository
import java.time.LocalTime
import java.time.Duration
import java.util.Calendar

/**
 * ViewModel gérant la logique d'un minuteur associé à une routine spécifique.
 * Permet de suivre le temps écoulé, de mettre en pause, de reprendre et de sauvegarder la progression.
 *
 * @param routineId L'identifiant unique de la routine à suivre.
 * @param repository Le dépôt pour accéder aux données des routines et de l'historique.
 * @param getRoutineUseCase Cas d'utilisation pour récupérer les détails de la routine.
 */
class TimerViewModel(
    private val routineId: String,
    private val repository: RoutineRepository,
    private val getRoutineUseCase: GetRoutineUseCase
) : ViewModel() {

    private val _routine = mutableStateOf<Routine?>(null)
    
    /**
     * Nom de la routine affiché dans l'interface.
     */
    private val _routineName = mutableStateOf("")
    val routineName: State<String> = _routineName

    /**
     * Temps restant en millisecondes.
     */
    private val _timeLeft = mutableLongStateOf(0L)
    val timeLeft: State<Long> = _timeLeft

    /**
     * État d'exécution du minuteur.
     */
    private val _isRunning = mutableStateOf(false)
    val isRunning: State<Boolean> = _isRunning

    private var timerJob: Job? = null
    private var originalTotalDuration: Long = 0

    init {
        loadRoutineData()
    }

    /**
     * Charge les données initiales de la routine et initialise le temps restant.
     */
    private fun loadRoutineData() {
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

    /**
     * Alterne entre le démarrage et la mise en pause du minuteur.
     */
    fun toggleTimer() {
        if (_isRunning.value) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    /**
     * Démarre le décompte du minuteur.
     */
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

    /**
     * Met le minuteur en pause et sauvegarde l'état actuel en base de données.
     */
    private fun pauseTimer() {
        _isRunning.value = false
        timerJob?.cancel()
        saveProgress(false)
    }

    /**
     * Sauvegarde l'état actuel (temps restant + progression calculée) dans le dépôt.
     *
     * @param isFinished Indique si la routine est considérée comme terminée aujourd'hui.
     */
    fun saveProgress(isFinished: Boolean = false) {
        viewModelScope.launch {
            _routine.value?.let { routine ->
                val elapsed = originalTotalDuration - _timeLeft.longValue
                var progress = if (originalTotalDuration > 0) {
                    ((elapsed.toFloat() / originalTotalDuration) * 100).toInt().coerceIn(0, 100)
                } else 0
                
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

    /**
     * Finalise la routine, enregistre une entrée dans l'historique et déclenche le callback de fin.
     *
     * @param onFinished Action à exécuter une fois la routine finalisée (ex: navigation).
     */
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

    /**
     * Retourne le timestamp correspondant au début de la journée actuelle (minuit).
     */
    private fun getTodayTimestamp(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
