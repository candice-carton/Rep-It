package project.repit.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class RoutineViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RoutineRepository
    val allRoutines: Flow<List<Routine>>

    init {
        val routineDao = AppDatabase.getDatabase(application).routineDao()
        repository = RoutineRepository(routineDao)
        allRoutines = repository.allRoutines
    }

    fun insert(routine: Routine) = viewModelScope.launch {
        repository.insert(routine)
    }

    fun update(routine: Routine) = viewModelScope.launch {
        repository.update(routine)
    }

    fun delete(routine: Routine) = viewModelScope.launch {
        repository.delete(routine)
    }
}
