package project.repit.model.domain.useCase

import project.repit.model.domain.model.Routine
import project.repit.model.data.RoutineRepository

class GetRoutineUseCase (private val repository: RoutineRepository) {
    suspend operator fun invoke(id: String): Routine? {
        return repository.getRoutineById(id)
    }
}