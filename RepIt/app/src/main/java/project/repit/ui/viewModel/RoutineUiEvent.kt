package project.repit.ui.viewModel

/**
 * Liste des actions utilisateur possibles sur l'écran des routines.
 */
sealed class RoutineUiEvent {
    data class AddRoutine(val routine: RoutineVM) : RoutineUiEvent()
    data class UpdateRoutine(val routine: RoutineVM) : RoutineUiEvent()
    data class DeleteRoutine(val routine: RoutineVM) : RoutineUiEvent()
    data class FilterByCategory(val category: String) : RoutineUiEvent()
}
