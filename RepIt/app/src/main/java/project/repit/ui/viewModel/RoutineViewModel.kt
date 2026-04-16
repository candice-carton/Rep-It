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
import project.repit.model.domain.useCase.RoutinesUseCases
import project.repit.model.domain.useCase.GetRoutinesUseCase
import project.repit.model.domain.useCase.GetRoutineUseCase
import project.repit.model.domain.useCase.UpsertRoutineUseCase
import project.repit.model.domain.useCase.DeleteRoutineUseCase
import project.repit.model.data.RoutineRepository
import java.util.Calendar
 
/**
 * ViewModel responsable de la gestion de la logique métier et de l'état de l'interface
 * pour l'écran des routines.
 *
 * Il gère la récupération, le filtrage par catégorie et la partition des routines
 * en deux groupes : "Aujourd'hui" et "À venir".
 *
 * @param application L'instance de l'application nécessaire pour initialiser la base de données.
 */
class RoutineViewModel(application: Application) : AndroidViewModel(application) {
 
    private val routinesUseCases: RoutinesUseCases
 
    /**
     * Liste brute de toutes les routines récupérées depuis la base de données.
     */
    private val _routines = mutableStateOf<List<RoutineVM>>(emptyList())
    val routines: State<List<RoutineVM>> = _routines
 
    /**
     * Catégorie actuellement sélectionnée pour filtrer la liste des routines.
     * Valeur par défaut : "Tous".
     */
    private val _selectedCategory = mutableStateOf("Tous")
    val selectedCategory: State<String> = _selectedCategory
 
    /**
     * Liste des routines prévues pour aujourd'hui (ou répétitives le jour actuel).
     */
    private val _todayRoutines = mutableStateOf<List<RoutineVM>>(emptyList())
    val todayRoutines: State<List<RoutineVM>> = _todayRoutines
 
    /**
     * Liste des routines prévues pour les jours futurs.
     */
    private val _upcomingRoutines = mutableStateOf<List<RoutineVM>>(emptyList())
    val upcomingRoutines: State<List<RoutineVM>> = _upcomingRoutines
 
    private var getRoutinesJob: Job? = null
 
    init {
        val dao = AppDatabase.getDatabase(application).routineDao()
        val repository = RoutineRepository(dao)
        routinesUseCases = RoutinesUseCases(
            getRoutines = GetRoutinesUseCase(repository),
            getRoutine = GetRoutineUseCase(repository),
            upsertRoutine = UpsertRoutineUseCase(repository),
            deleteRoutine = DeleteRoutineUseCase(repository)
        )
        loadRoutines()
    }
 
    /**
     * Point d'entrée pour tous les événements utilisateur provenant de l'interface.
     *
     * @param event L'événement utilisateur à traiter (Ajout, Mise à jour, Suppression, Filtre).
     */
    fun onEvent(event: RoutineUiEvent) {
        when (event) {
            is RoutineUiEvent.AddRoutine -> viewModelScope.launch {
                routinesUseCases.upsertRoutine(event.routine.toEntity())
            }
            is RoutineUiEvent.UpdateRoutine -> viewModelScope.launch {
                routinesUseCases.upsertRoutine(event.routine.toEntity())
            }
            is RoutineUiEvent.DeleteRoutine -> viewModelScope.launch {
                routinesUseCases.deleteRoutine(event.routine.toEntity())
            }
            is RoutineUiEvent.FilterByCategory -> {
                _selectedCategory.value = event.category
                // Recalculer les listes avec le nouveau filtre
                updatePartitionedLists(_routines.value)
            }
        }
    }
 
    /**
     * S'abonne au flux de données des routines provenant du repository.
     * Met à jour l'état local dès qu'une modification intervient en base de données.
     */
    private fun loadRoutines() {
        getRoutinesJob?.cancel()
        getRoutinesJob = routinesUseCases.getRoutines().onEach { routineEntities ->
            val vms = routineEntities.map { RoutineVM.fromEntity(it) }
            _routines.value = vms
            updatePartitionedLists(vms)
        }.launchIn(viewModelScope)
    }
 
    /**
     * Recalcule todayRoutines et upcomingRoutines à partir de la liste fournie.
     * Appelé à chaque fois que _routines ou _selectedCategory change.
     * Le tri est effectué par date d'occurrence, puis par poids de priorité.
     *
     * @param allRoutines La liste complète des routines à traiter.
     */
    private fun updatePartitionedLists(allRoutines: List<RoutineVM>) {
        val todayTs = Calendar.getInstance().run {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            timeInMillis
        }
        val tomorrowTs = todayTs + 24L * 60L * 60L * 1000L

        val filtered = if (_selectedCategory.value == "Tous") {
            allRoutines
        } else {
            allRoutines.filter { it.category == _selectedCategory.value }
        }

        val withNextOccurrence = filtered.map { routine ->
            routine to routine.getNextOccurrenceTimestamp(todayTs)
        }

        _todayRoutines.value = withNextOccurrence
            .filter { (_, nextOccurrence) -> nextOccurrence == todayTs }
            .sortedWith(
                compareBy<Pair<RoutineVM, Long>> { it.second }
                    .thenBy { priorityWeight(it.first.priority) }
            )
            .map { it.first }

        _upcomingRoutines.value = withNextOccurrence
            .map { (routine, nextOccurrence) ->
                if (routine.isRepetitive && nextOccurrence == todayTs) {
                    routine to routine.getNextOccurrenceTimestamp(tomorrowTs)
                } else {
                    routine to nextOccurrence
                }
            }
            .filter { (_, nextOccurrence) -> nextOccurrence > todayTs }
            .sortedWith(
                compareBy<Pair<RoutineVM, Long>> { it.second }
                    .thenBy { priorityWeight(it.first.priority) }
            )
            .map { it.first }
    }
 
    /**
     * Associe une valeur numérique à chaque niveau de priorité pour faciliter le tri.
     * Une valeur plus faible indique une priorité plus élevée.
     *
     * @param priority Le libellé de la priorité ("Élevée", "Moyenne", "Faible").
     * @return Le poids numérique correspondant.
     */
    private fun priorityWeight(priority: String): Int = when (priority) {
        "Élevée" -> 0
        "Moyenne" -> 1
        "Faible" -> 2
        else -> 3
    }
}
