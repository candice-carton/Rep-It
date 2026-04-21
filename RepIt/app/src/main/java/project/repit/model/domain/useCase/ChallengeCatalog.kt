package project.repit.model.domain.useCase

import project.repit.model.domain.model.ChallengeDifficulty
import project.repit.model.domain.model.DailyChallengeSuggestion

/**
 * Catalogue central des défis quotidiens.
 * Ce fichier est volontairement séparé pour faciliter les modifications de contenu.
 */
object ChallengeCatalog {
    val entries: List<DailyChallengeSuggestion> = listOf(
        DailyChallengeSuggestion("easy_1", "Hydratation", "Bois 8 verres d'eau", ChallengeDifficulty.FACILE, false, 1, "Santé", true, 8),
        DailyChallengeSuggestion("easy_2", "Marche douce", "Marche 20 minutes", ChallengeDifficulty.FACILE, true, 1, "Sport", true, 20),
        DailyChallengeSuggestion("easy_3", "Respiration", "5 minutes de respiration consciente", ChallengeDifficulty.FACILE, false, 2, "Bien-être", true, 5),
        DailyChallengeSuggestion("easy_4", "Étirements matin", "10 minutes d'étirements légers", ChallengeDifficulty.FACILE, false, 1, "Sport", true, 10),
        DailyChallengeSuggestion("easy_5", "Escaliers", "Prends les escaliers 5 fois aujourd'hui", ChallengeDifficulty.FACILE, true, 1, "Sport", true, 5),
        DailyChallengeSuggestion("easy_6", "Posture", "Fais 3 pauses posture de 2 minutes", ChallengeDifficulty.FACILE, false, 1, "Santé", true, 3),
        DailyChallengeSuggestion("easy_7", "Sommeil", "Évite les écrans 30 min avant de dormir", ChallengeDifficulty.FACILE, false, 1, "Bien-être", true, 30),
        DailyChallengeSuggestion("easy_8", "Pas actifs", "Atteins 5 000 pas aujourd'hui", ChallengeDifficulty.FACILE, true, 1, "Sport", true, 5000),
        DailyChallengeSuggestion("easy_9", "Mobilité", "Réalise 8 minutes de mobilité hanches/épaules", ChallengeDifficulty.FACILE, false, 2, "Sport", true, 8),
        DailyChallengeSuggestion("easy_10", "Snack sain", "Remplace un snack sucré par un fruit", ChallengeDifficulty.FACILE, false, 1, "Alimentation", true, 1),

        DailyChallengeSuggestion("mid_1", "Cardio maison", "15 minutes de cardio", ChallengeDifficulty.MOYEN, false, 2, "Sport", true, 15),
        DailyChallengeSuggestion("mid_2", "Objectif pas", "Atteins 8 000 pas aujourd'hui", ChallengeDifficulty.MOYEN, true, 2, "Sport", true, 8000),
        DailyChallengeSuggestion("mid_3", "Focus", "2 sessions pomodoro de 25 minutes", ChallengeDifficulty.MOYEN, false, 3, "Travail", true, 2),
        DailyChallengeSuggestion("mid_4", "Circuit poids du corps", "3 tours: 10 squats, 8 pompes, 20'' gainage", ChallengeDifficulty.MOYEN, false, 2, "Sport", true, 3),
        DailyChallengeSuggestion("mid_5", "Marche rapide", "Marche rapide 35 minutes", ChallengeDifficulty.MOYEN, true, 2, "Sport", true, 35),
        DailyChallengeSuggestion("mid_6", "Core training", "12 minutes d'exercices abdos/lombaires", ChallengeDifficulty.MOYEN, false, 2, "Sport", true, 12),
        DailyChallengeSuggestion("mid_7", "Sans ascenseur", "Aucun ascenseur de la journée", ChallengeDifficulty.MOYEN, true, 2, "Sport", false, 0),
        DailyChallengeSuggestion("mid_8", "Hydratation avancée", "Bois 2L d'eau dans la journée", ChallengeDifficulty.MOYEN, false, 2, "Santé", true, 2000),
        DailyChallengeSuggestion("mid_9", "Routine soir", "20 minutes de stretching + respiration", ChallengeDifficulty.MOYEN, false, 2, "Bien-être", true, 20),
        DailyChallengeSuggestion("mid_10", "Interval training", "10 x 30s effort / 30s repos", ChallengeDifficulty.MOYEN, false, 3, "Sport", true, 10),

        DailyChallengeSuggestion("hard_1", "Run", "Cours 5 km", ChallengeDifficulty.DIFFICILE, true, 3, "Sport", true, 5),
        DailyChallengeSuggestion("hard_2", "Renforcement", "45 minutes de renforcement", ChallengeDifficulty.DIFFICILE, false, 3, "Sport", true, 45),
        DailyChallengeSuggestion("hard_3", "Détox digitale", "4h sans réseaux sociaux", ChallengeDifficulty.DIFFICILE, false, 2, "Personnel", true, 4),
        DailyChallengeSuggestion("hard_4", "HIIT intense", "25 minutes de HIIT", ChallengeDifficulty.DIFFICILE, false, 3, "Sport", true, 25),
        DailyChallengeSuggestion("hard_5", "Objectif pas ++", "Atteins 12 000 pas aujourd'hui", ChallengeDifficulty.DIFFICILE, true, 3, "Sport", true, 12000),
        DailyChallengeSuggestion("hard_6", "Tractions/Pompes", "5 séries de pompes + tractions assistées", ChallengeDifficulty.DIFFICILE, false, 3, "Sport", true, 5),
        DailyChallengeSuggestion("hard_7", "Course fractionnée", "8 x 400m à allure soutenue", ChallengeDifficulty.DIFFICILE, true, 3, "Sport", true, 8),
        DailyChallengeSuggestion("hard_8", "Jambes", "40 minutes focus jambes (squats/fentes)", ChallengeDifficulty.DIFFICILE, false, 3, "Sport", true, 40),
        DailyChallengeSuggestion("hard_9", "Tabata", "6 blocs Tabata complets", ChallengeDifficulty.DIFFICILE, false, 3, "Sport", true, 6),
        DailyChallengeSuggestion("hard_10", "Rando urbaine", "90 minutes de marche active continue", ChallengeDifficulty.DIFFICILE, true, 2, "Sport", true, 90)
    )
}
