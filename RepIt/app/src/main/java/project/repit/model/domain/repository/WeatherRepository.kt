package project.repit.model.domain.repository

import project.repit.model.domain.model.WeatherInfo

interface WeatherRepository {
    suspend fun getCurrentWeather(latitude: Double, longitude: Double): WeatherInfo?
}
