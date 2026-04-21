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
    val importance: Int,
    val category: String = "Autre",
    val isQuantifiable: Boolean = false,
    val targetValue: Int = 0
)

enum class ChallengeDifficulty {
    FACILE,
    MOYEN,
    DIFFICILE
}
