package project.repit.data.repository
//
//import project.repit.data.model.*
//
//object FitRepository {
//
//    fun getUser()    = User()
//    fun getWeather() = Weather()
//    fun getSteps()   = StepData()
//    fun getStats()   = WeeklyStats()
//
//    fun getDailyChallenge() = Challenge(
//        id              = "daily_1",
//        title           = "Course en extérieur",
//        durationMin     = 30,
//        subtitle        = "Météo idéale",
//        difficultyStars = 3,
//        category        = ChallengeCategory.EXTERIEUR,
//    )
//
//    fun getMyChallenge() = listOf(
//        Challenge(
//            id              = "c1",
//            title           = "Haut du corps",
//            durationMin     = 45,
//            subtitle        = "12 exercices",
//            difficultyStars = 3,
//            progressPercent = 75,
//            category        = ChallengeCategory.FORCE,
//        ),
//        Challenge(
//            id              = "c2",
//            title           = "Haut du corps",
//            durationMin     = 45,
//            subtitle        = "12 exercices",
//            difficultyStars = 3,
//            progressPercent = 75,
//            category        = ChallengeCategory.CARDIO,
//        ),
//    )
//
//    fun getSuggestedChallenges() = listOf(
//        Challenge(
//            id              = "s1",
//            title           = "Marche rapide",
//            durationMin     = 45,
//            subtitle        = "Zone verte",
//            difficultyStars = 4,
//            successRate     = 85,
//            preparationMin  = 5,
//            estimatedSteps  = 6_000,
//            category        = ChallengeCategory.EXTERIEUR,
//        ),
//        Challenge(
//            id              = "s2",
//            title           = "HIIT Cardio",
//            durationMin     = 20,
//            subtitle        = "Intense",
//            difficultyStars = 3,
//            successRate     = 78,
//            estimatedKcal   = 280,
//            energyLevel     = "Élevée",
//            category        = ChallengeCategory.CARDIO,
//        ),
//    )
//
//    fun getSeries() = listOf(
//        Series(
//            title               = "Challenge 30 jours",
//            currentDay          = 12,
//            totalDays           = 30,
//            progressPercent     = 40,
//            challengesCompleted = 8,
//        ),
//        Series(
//            title               = "Marche quotidienne",
//            currentDay          = 7,
//            totalDays           = 7,
//            progressPercent     = 100,
//            challengesCompleted = 8,
//            isFinished          = true,
//        ),
//    )
//
//    fun getNotifications() = listOf(
//        AppNotification(
//            id          = "n1",
//            type        = NotificationType.STEPS,
//            title       = "Objectifs de pas",
//            body        = "Vous avez parcouru 8 450 pas aujourd'hui. Atteignez 10 000 avant 20h !",
//            time        = "11:30",
//            actionLabel = "Marcher 15 min",
//        ),
//        AppNotification(
//            id          = "n2",
//            type        = NotificationType.WEATHER,
//            title       = "Météo idéale",
//            body        = "Belle journée ensoleillée ! Parfait pour un défi en extérieur",
//            time        = "10:15",
//            actionLabel = "Défi extérieur suggéré",
//        ),
//        AppNotification(
//            id    = "n3",
//            type  = NotificationType.CALORIES,
//            title = "Objectifs de calories brulées",
//            body  = "Vous avez brûlé 320 kcal aujourd'hui. Atteignez 500 kcal avant 20h ! 🔥",
//            time  = "8:30",
//        ),
//    )
//}
