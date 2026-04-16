package project.repit.model.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import project.repit.model.domain.model.WeatherCondition
import project.repit.model.domain.model.WeatherInfo
import project.repit.model.domain.repository.WeatherRepository
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

/**
 * Implémentation simple de [WeatherRepository] basée sur l'API Open-Meteo.
 */
class OpenMeteoWeatherRepository : WeatherRepository {

    override suspend fun getCurrentWeather(latitude: Double, longitude: Double): WeatherInfo? = withContext(Dispatchers.IO) {
        runCatching {
            val endpoint =
                "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current=temperature_2m,weather_code"
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
            }

            connection.inputStream.bufferedReader().use { reader ->
                val json = JSONObject(reader.readText())
                val current = json.getJSONObject("current")
                val temperature = current.optDouble("temperature_2m", Double.NaN)
                val code = current.optInt("weather_code", -1)

                WeatherInfo(
                    temperatureCelsius = if (temperature.isNaN()) 0 else temperature.roundToInt(),
                    condition = mapWeatherCode(code)
                )
            }
        }.getOrNull()
    }

    private fun mapWeatherCode(code: Int): WeatherCondition = when (code) {
        0, 1 -> WeatherCondition.SUNNY
        2, 3, 45, 48 -> WeatherCondition.CLOUDY
        51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> WeatherCondition.RAINY
        else -> WeatherCondition.UNKNOWN
    }
}
