package project.repit.model.domain.useCase

import project.repit.model.domain.model.ChallengeDifficulty
import project.repit.model.domain.model.DailyChallengeSuggestion
import project.repit.model.domain.model.WeatherCondition
import project.repit.model.domain.model.WeatherInfo
import java.time.LocalDate
import kotlin.random.Random

/**
 * Génère un défi du jour selon difficulté + météo.
 *
 * Validations métier implémentées :
 * 1) si la météo est pluvieuse, on exclut les défis extérieurs.
 * 2) si la difficulté demandée n'a aucun défi compatible, on fallback sur tous les défis compatibles météo.
 */
class GenerateDailyChallengeUseCase(
    private val challengeCatalog: List<DailyChallengeSuggestion> = defaultCatalog()
) {
    operator fun invoke(
        difficulty: ChallengeDifficulty,
        weather: WeatherInfo?,
        refreshToken: Int = 0,
        date: LocalDate = LocalDate.now()
    ): DailyChallengeSuggestion? {
        if (challengeCatalog.isEmpty()) return null

        val weatherCompatible = challengeCatalog.filter { challenge ->
            if (weather?.condition == WeatherCondition.RAINY) !challenge.isOutdoor else true
        }

        if (weatherCompatible.isEmpty()) return null

        val difficultyCompatible = weatherCompatible.filter { it.difficulty == difficulty }
        val pool = if (difficultyCompatible.isNotEmpty()) difficultyCompatible else weatherCompatible

        val seed = (date.toEpochDay().toInt() * 31) + refreshToken
        return pool[Random(seed).nextInt(pool.size)]
    }

    companion object {
        fun defaultCatalog(): List<DailyChallengeSuggestion> = listOf(
            DailyChallengeSuggestion("easy_1", "Hydratation", "Bois 8 verres d'eau", ChallengeDifficulty.FACILE, false, 1),
            DailyChallengeSuggestion("easy_2", "Marche douce", "Marche 20 minutes", ChallengeDifficulty.FACILE, true, 1),
            DailyChallengeSuggestion("easy_3", "Respiration", "5 minutes de respiration consciente", ChallengeDifficulty.FACILE, false, 2),
            DailyChallengeSuggestion("mid_1", "Cardio maison", "15 minutes de cardio", ChallengeDifficulty.MOYEN, false, 2),
            DailyChallengeSuggestion("mid_2", "Objectif pas", "Atteins 8 000 pas aujourd'hui", ChallengeDifficulty.MOYEN, true, 2),
            DailyChallengeSuggestion("mid_3", "Focus", "2 sessions pomodoro de 25 minutes", ChallengeDifficulty.MOYEN, false, 3),
            DailyChallengeSuggestion("hard_1", "Run", "Cours 5 km", ChallengeDifficulty.DIFFICILE, true, 3),
            DailyChallengeSuggestion("hard_2", "Renforcement", "45 minutes de renforcement", ChallengeDifficulty.DIFFICILE, false, 3),
            DailyChallengeSuggestion("hard_3", "Détox digitale", "4h sans réseaux sociaux", ChallengeDifficulty.DIFFICILE, false, 2)
        )
    }
}
