package project.repit.model.domain.model

/**
 * Défi suggéré dynamiquement selon la difficulté et le contexte météo.
 */
data class DailyChallengeSuggestion(
    val id: String,
    val title: String,
    val description: String,
    val difficulty: ChallengeDifficulty,
    val isOutdoor: Boolean,
    val importance: Int
)

enum class ChallengeDifficulty {
    FACILE,
    MOYEN,
    DIFFICILE
}
