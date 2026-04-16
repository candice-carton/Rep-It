package project.repit.ui.viewModel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import project.repit.model.data.AppDatabase
import project.repit.model.domain.useCase.GetRoutinesUseCase
import project.repit.model.domain.useCase.RoutinesUseCases
import project.repit.model.domain.useCase.GetRoutineUseCase
import project.repit.model.domain.useCase.UpsertRoutineUseCase
import project.repit.model.domain.useCase.DeleteRoutineUseCase
import project.repit.model.data.RoutineRepository
import java.util.Calendar

/**
 * ViewModel dédié à l'écran d'accueil (Home).
 * Il prépare les données spécifiques à l'affichage de la synthèse quotidienne :
 * défis à réaliser, défis terminés et aperçu des prochains jours.
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val routinesUseCases: RoutinesUseCases
    private var getRoutinesJob: Job? = null

    // État brut des routines
    private val _routines = mutableStateOf<List<RoutineVM>>(emptyList())

    /**
     * Liste des paires (Routine, Prochaine occurrence) pour les défis à réaliser.
     */
    private val _todoRoutines = mutableStateOf<List<Pair<RoutineVM, Long>>>(emptyList())
    val todoRoutines: State<List<Pair<RoutineVM, Long>>> = _todoRoutines

    /**
     * Liste des routines terminées aujourd'hui.
     */
    private val _doneRoutines = mutableStateOf<List<RoutineVM>>(emptyList())
    val doneRoutines: State<List<RoutineVM>> = _doneRoutines

    /**
     * Aperçu des 5 prochains défis à venir (après aujourd'hui).
     */
    private val _upcomingHighlights = mutableStateOf<List<RoutineVM>>(emptyList())
    val upcomingHighlights: State<List<RoutineVM>> = _upcomingHighlights

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
    }

    /**
     * Gère les événements utilisateur (mise à jour, suppression) depuis l'accueil.
     */
    fun onEvent(event: RoutineUiEvent) {
        when (event) {
            is RoutineUiEvent.DeleteRoutine -> viewModelScope.launch {
                routinesUseCases.deleteRoutine(event.routine.toEntity())
            }
            is RoutineUiEvent.UpdateRoutine -> viewModelScope.launch {
                routinesUseCases.upsertRoutine(event.routine.toEntity())
            }
            else -> { /* Les autres événements sont gérés par RoutineViewModel */ }
        }
    }

    /**
     * S'abonne aux modifications des routines et déclenche le recalcul des listes d'accueil.
     */
    private fun observeRoutines() {
        getRoutinesJob?.cancel()
        getRoutinesJob = routinesUseCases.getRoutines().onEach { entities ->
            val vms = entities.map { RoutineVM.fromEntity(it) }
            _routines.value = vms
            computeHomeData(vms)
        }.launchIn(viewModelScope)
    }

    /**
     * Calcule et partitionne les données pour les différentes sections de l'accueil.
     */
    private fun computeHomeData(routines: List<RoutineVM>) {
        val now = Calendar.getInstance()
        val todayTs = now.apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // 1. Calcul des défis à faire (Aujourd'hui + Futur)
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

        // 2. Défis terminés aujourd'hui
        _doneRoutines.value = routines.filter { it.lastCompletedDate == todayTs }
            .sortedWith(compareBy<RoutineVM> { priorityWeight(it.priority) }.thenBy { it.startAt })

        // 3. Highlight "Prochainement"
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
}
