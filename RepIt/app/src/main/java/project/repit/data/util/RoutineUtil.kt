package project.repit.data.util

import project.repit.data.model.Routine

object RoutineUtil {
    val defaultRoutines = listOf(
        Routine(
            id = 1,
            title = "Routine du matin",
            description = "Étirements + respiration pour bien commencer la journée.",
            frequency = "Tous les jours",
            targetMinutes = 15,
        ),
        Routine(
            id = 2,
            title = "Focus études",
            description = "Bloc de concentration de 45 min suivi d'une pause active.",
            frequency = "Lun - Ven",
            targetMinutes = 45,
        ),
        Routine(
            id = 3,
            title = "Marche du soir",
            description = "Marche légère pour atteindre l'objectif quotidien de pas.",
            frequency = "3 fois / semaine",
            targetMinutes = 30,
        ),
    )
}
