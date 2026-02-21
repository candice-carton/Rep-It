package project.repit.data.model
//
//enum class ChallengeCategory { RAPIDE, EXTERIEUR, FORCE, CARDIO }
//enum class NotificationType   { STEPS, WEATHER, CALORIES }
//
//data class User(
//    val name: String       = "Nathan willay",
//    val level: Int         = 8,
//    val levelLabel: String = "Intermédiaire",
//    val age: Int           = 28,
//    val heightCm: Int      = 175,
//    val weightKg: Int      = 72,
//    val totalChallenges: Int = 42,
//)
//
//data class Weather(
//    val description: String = "Ensoleillé",
//    val tempCelsius: Int    = 22,
//)
//
//data class StepData(
//    val current: Int = 4_832,
//    val goal: Int    = 10_000,
//) {
//    val progress: Float get() = current.toFloat() / goal
//}
//
//data class Challenge(
//    val id: String,
//    val title: String,
//    val durationMin: Int,
//    val subtitle: String,
//    val difficultyStars: Int,
//    val progressPercent: Int    = 0,
//    val category: ChallengeCategory = ChallengeCategory.FORCE,
//    val successRate: Int        = 0,
//    val preparationMin: Int     = 0,
//    val estimatedSteps: Int     = 0,
//    val estimatedKcal: Int      = 0,
//    val energyLevel: String     = "",
//)
//
//data class Series(
//    val title: String,
//    val currentDay: Int,
//    val totalDays: Int,
//    val progressPercent: Int,
//    val challengesCompleted: Int,
//    val isFinished: Boolean = false,
//) {
//    val streakDays: Int get() = currentDay
//}
//
//data class WeeklyStats(
//    val weekNumber: Int      = 49,
//    val progressPercent: Int = 75,
//    val vsLastWeek: Int      = 12,
//    val weeklyGoalDone: Int  = 15,
//    val weeklyGoalTotal: Int = 20,
//    val kcalBurned: Int      = 3_850,
//    val kcalChange: Int      = 8,
//    val totalSteps: Int      = 68_450,
//    val avgStepsPerDay: Int  = 9_778,
//    val stepsChange: Int     = 15,
//    val challengesDone: Int  = 42,
//    val challengesChange: Int = 5,
//)
//
//data class AppNotification(
//    val id: String,
//    val type: NotificationType,
//    val title: String,
//    val body: String,
//    val time: String,
//    val actionLabel: String? = null,
//)
