package project.repit.model.domain.useCase

import kotlinx.coroutines.flow.Flow
import project.repit.model.domain.model.Routine
import project.repit.models.RoutineRepository

class GetRoutinesUseCase (private val repository: RoutineRepository){
    operator fun invoke(): Flow<List<Routine>> {
        return repository.allRoutines
    }
}