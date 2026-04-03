package project.repit.model.domain.useCase

import project.repit.model.domain.model.Routine
import project.repit.models.RoutineRepository

/**
 * Exception personnalisée pour les erreurs liées aux routines.
 */
class RoutineException(message: String) : Exception(message)

/**
 * Use Case pour ajouter ou mettre à jour une routine.
 * Applique les règles métier de validation avant l'enregistrement.
 */
class UpsertRoutineUseCase(private val repository: RoutineRepository) {
    
    @Throws(RoutineException::class)
    suspend operator fun invoke(routine: Routine) {
        // Validation des champs obligatoires
        if (routine.name.isBlank()) {
            throw RoutineException("Le nom de la routine ne peut pas être vide.")
        }
        if (routine.category.isBlank()) {
            throw RoutineException("Veuillez sélectionner une catégorie.")
        }
        if (routine.priority.isBlank()) {
            throw RoutineException("Veuillez sélectionner une priorité.")
        }
        
        // Validation spécifique pour le mode répétitif
        if (routine.isRepetitive && routine.repeatDays.isEmpty()) {
            throw RoutineException("Veuillez sélectionner au moins un jour de répétition.")
        }

        // Validation pour les objectifs quantifiables
        if (routine.isQuantifiable) {
            if (routine.targetValue <= 0) {
                throw RoutineException("La valeur cible doit être supérieure à zéro.")
            }
            if (routine.unit.isBlank()) {
                throw RoutineException("Veuillez définir une unité (ex: L, kg).")
            }
        }

        // On ne valide les horaires que si ce n'est pas "toute la journée" ET pas "quantifiable"
        if (!routine.isAllDay && !routine.isQuantifiable) {
            if (routine.startAt.isBlank()) {
                throw RoutineException("Veuillez définir une heure de début.")
            }
            if (routine.endAt.isBlank()) {
                throw RoutineException("Veuillez définir une heure de fin.")
            }
        }

        // Si tout est valide, on appelle le repository
        repository.upsert(routine)
    }
}
