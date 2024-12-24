package com.example.weatheralarmapp.data.repository

import com.example.weatheralarmapp.data.remote.CoordinateResponse
import com.example.weatheralarmapp.data.remote.WeatherApiService
import com.example.weatheralarmapp.data.remote.WeatherForecastResponse
import javax.inject.Inject

class GetWeatherRepositoryImpl
    @Inject
    constructor(
        private val weatherApiService: WeatherApiService,
    ) : GetWeatherRepository {
        override suspend fun getCoordinate(city: String): CoordinateResponse {
            val result = weatherApiService.getCoordinate(city)
            return result.first() ?: throw Exception("City not found")
        }

        override suspend fun getWeather(
            lat: Double,
            lon: Double,
            cnt: Int,
        ): WeatherForecastResponse = weatherApiService.getWeather(lat, lon, cnt)
    }
