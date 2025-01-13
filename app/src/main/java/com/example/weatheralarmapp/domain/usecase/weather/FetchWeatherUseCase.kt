package com.example.weatheralarmapp.domain.usecase.weather

import com.example.weatheralarmapp.data.repository.GetWeatherRepository
import com.example.weatheralarmapp.ui.features.alarm.WeatherState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

class FetchWeatherUseCase
    @Inject
    constructor(
        private val getWeatherRepository: GetWeatherRepository,
    ) {
        private val currentTime = LocalDateTime.now()
        private val FIRST_FORECAST_TIME = 15

        suspend fun getWeatherByCityName(
            cityName: String,
            alarmTime: LocalTime,
        ): WeatherState =
            try {
                val result =
                    withContext(Dispatchers.IO) {
                        getWeatherRepository.getCoordinate(cityName)
                    }

                // TODO 取得開始時刻が不安定のため調査必要。それに応じてcntの計算を修正。
                // アラームの時間が現在時刻よりも前であれば次の日の時刻とする
                // cnt = 1で午後１５時から3時間おきに天気情報を取得する
                val cnt =
                    if (alarmTime.hour < currentTime.hour) {
                        (alarmTime.hour + 24 - FIRST_FORECAST_TIME) / 3 + 1
                    } else {
                        (alarmTime.hour - FIRST_FORECAST_TIME) / 3 + 1
                    }

                getWeatherByLocation(result.lat, result.lon, cnt)

            } catch (e: Exception) {
                WeatherState.Error(e.message ?: "Unknown error")
            }

        private suspend fun getWeatherByLocation(
            lat: Double,
            lon: Double,
            cnt: Int,
        ): WeatherState =
            try {
                val result =
                    withContext(Dispatchers.IO) {
                        getWeatherRepository.getWeather(lat, lon, cnt)
                    }

                WeatherState.Success(
                    result.list
                        .last()
                        .weather[0]
                        .description,
                )
            } catch (e: Exception) {
                WeatherState.Error(e.message ?: "Unknown error")
            }
    }
