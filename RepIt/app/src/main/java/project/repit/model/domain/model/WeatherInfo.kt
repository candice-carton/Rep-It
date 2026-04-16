package project.repit.model.domain.model

/**
 * Représente les données météo courantes utilisées dans l'application.
 */
data class WeatherInfo(
    val temperatureCelsius: Int,
    val condition: WeatherCondition
)

enum class WeatherCondition {
    SUNNY,
    CLOUDY,
    RAINY,
    UNKNOWN
}
