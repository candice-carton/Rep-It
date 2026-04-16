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
        fun defaultCatalog(): List<DailyChallengeSuggestion> = ChallengeCatalog.entries
    }
}
