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
import project.repit.models.RoutineRepository
import java.util.Calendar
 
class RoutineViewModel(application: Application) : AndroidViewModel(application) {
 
    private val routinesUseCases: RoutinesUseCases
 
    // Liste brute de toutes les routines
    private val _routines = mutableStateOf<List<RoutineVM>>(emptyList())
    val routines: State<List<RoutineVM>> = _routines
 
    // Catégorie sélectionnée pour le filtre
    private val _selectedCategory = mutableStateOf("Tous")
    val selectedCategory: State<String> = _selectedCategory
 
    // Routines partitionnées — recalculées explicitement à chaque changement
    private val _todayRoutines = mutableStateOf<List<RoutineVM>>(emptyList())
    val todayRoutines: State<List<RoutineVM>> = _todayRoutines
 
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
     */
    private fun updatePartitionedLists(allRoutines: List<RoutineVM>) {
        val calendar = Calendar.getInstance()
        val todayTs = calendar.run {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            timeInMillis
        }
        val currentDayOfWeek = if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) 7
                               else calendar.get(Calendar.DAY_OF_WEEK) - 1
 
        val filtered = if (_selectedCategory.value == "Tous") allRoutines
                       else allRoutines.filter { it.category == _selectedCategory.value }
 
        val (today, upcoming) = filtered.partition { routine ->
            if (routine.isRepetitive) {
                routine.repeatDays.contains(currentDayOfWeek)
            } else {
                val sched = routine.scheduledDate ?: routine.createdAt
                val cal = Calendar.getInstance().apply {
                    timeInMillis = sched
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                cal.timeInMillis <= todayTs
            }
        }
 
        _todayRoutines.value = today.sortedWith(
            compareBy<RoutineVM> { it.getNextOccurrenceTimestamp() }
                .thenBy { priorityWeight(it.priority) }
        )
 
        _upcomingRoutines.value = upcoming.sortedWith(
            compareBy<RoutineVM> { it.getNextOccurrenceTimestamp() }
                .thenBy { priorityWeight(it.priority) }
        )
    }
 
    private fun priorityWeight(priority: String): Int = when (priority) {
        "Élevée" -> 0
        "Moyenne" -> 1
        "Faible" -> 2
        else -> 3
    }
}
