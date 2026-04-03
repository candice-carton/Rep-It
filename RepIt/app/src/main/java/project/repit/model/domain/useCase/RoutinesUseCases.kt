package project.repit.model.domain.useCase

/**
 * Regroupe l'ensemble des Use Cases liés aux Routines pour simplifier l'injection dans les ViewModels.
 */
data class RoutinesUseCases(
    val getRoutines: GetRoutinesUseCase,
    val getRoutine: GetRoutineUseCase,
    val upsertRoutine: UpsertRoutineUseCase,
    val deleteRoutine: DeleteRoutineUseCase
)
