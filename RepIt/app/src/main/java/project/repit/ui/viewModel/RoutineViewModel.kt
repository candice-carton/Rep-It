package project.repit.ui.viewModel

import android.app.Application
import androidx.compose.runtime.MutableState
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

/**
 * ViewModel pour gérer l'affichage et les actions sur les routines.
 */
class RoutineViewModel(application: Application) : AndroidViewModel(application) {
    
    private val routinesUseCases: RoutinesUseCases

    private val _routines: MutableState<List<RoutineVM>> = mutableStateOf(emptyList())
    val routines: State<List<RoutineVM>> = _routines
    
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
     * Point d'entrée unique pour tous les événements de l'UI.
     */
    fun onEvent(event: RoutineUiEvent) {
        when (event) {
            is RoutineUiEvent.AddRoutine -> {
                viewModelScope.launch {
                    routinesUseCases.upsertRoutine(event.routine.toEntity())
                }
            }
            is RoutineUiEvent.UpdateRoutine -> {
                viewModelScope.launch {
                    routinesUseCases.upsertRoutine(event.routine.toEntity())
                }
            }
            is RoutineUiEvent.DeleteRoutine -> {
                viewModelScope.launch {
                    routinesUseCases.deleteRoutine(event.routine.toEntity())
                }
            }
            is RoutineUiEvent.FilterByCategory -> {
                // Logique de filtrage si nécessaire au niveau VM
            }
        }
    }

    private fun loadRoutines() {
        getRoutinesJob?.cancel()
        getRoutinesJob = routinesUseCases.getRoutines().onEach { routineEntities ->
            _routines.value = routineEntities.map { RoutineVM.fromEntity(it) }
        }.launchIn(viewModelScope)
    }
}
