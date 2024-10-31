package com.example.weatheralarmapp.data

import com.example.weatheralarmapp.model.CoordinateResponse
import com.example.weatheralarmapp.model.WeatherResponse
import com.example.weatheralarmapp.network.WeatherApiService
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
        ): WeatherResponse = weatherApiService.getWeather(lat, lon)
    }
