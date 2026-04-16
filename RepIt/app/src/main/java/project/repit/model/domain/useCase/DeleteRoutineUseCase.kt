package project.repit.model.domain.useCase

import project.repit.model.domain.model.Routine
import project.repit.model.data.RoutineRepository

class DeleteRoutineUseCase (private val repository: RoutineRepository) {
    suspend operator fun invoke(routine: Routine) {
        repository.delete(routine)
    }
}