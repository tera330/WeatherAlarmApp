package com.example.weatheralarmapp.data.repository

import com.example.weatheralarmapp.data.remote.CoordinateResponse
import com.example.weatheralarmapp.data.remote.WeatherForecastResponse

interface GetWeatherRepository {
    suspend fun getCoordinate(city: String): CoordinateResponse

    suspend fun getWeather(
        lat: Double,
        lon: Double,
        cnt: Int,
    ): WeatherForecastResponse
}
