package project.repit.model.domain.useCase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import project.repit.model.domain.model.ChallengeDifficulty
import project.repit.model.domain.model.WeatherCondition
import project.repit.model.domain.model.WeatherInfo

class GenerateDailyChallengeUseCaseTest {

    private val useCase = GenerateDailyChallengeUseCase()

    @Test
    fun `quand la meteo est pluvieuse alors le defi n est pas exterieur`() {
        val rainy = WeatherInfo(temperatureCelsius = 9, condition = WeatherCondition.RAINY)

        val suggestion = useCase(
            difficulty = ChallengeDifficulty.MOYEN,
            weather = rainy,
            refreshToken = 1
        )

        assertNotNull(suggestion)
        assertFalse(suggestion!!.isOutdoor)
    }

    @Test
    fun `quand on change refresh token alors on peut obtenir un autre defi`() {
        val cloudy = WeatherInfo(temperatureCelsius = 14, condition = WeatherCondition.CLOUDY)

        val first = useCase(
            difficulty = ChallengeDifficulty.FACILE,
            weather = cloudy,
            refreshToken = 0
        )
        val second = useCase(
            difficulty = ChallengeDifficulty.FACILE,
            weather = cloudy,
            refreshToken = 10
        )

        assertNotNull(first)
        assertNotNull(second)
        assertTrue(first!!.id != second!!.id || first.title != second.title)
    }
}
