package com.example.weatheralarmapp.data

import com.example.weatheralarmapp.model.CoordinateResponse
import com.example.weatheralarmapp.model.WeatherResponse

interface GetWeatherRepository {
    suspend fun getCoordinate(city: String): CoordinateResponse

    suspend fun getWeather(
        lat: Double,
        lon: Double,
    ): WeatherResponse
}
