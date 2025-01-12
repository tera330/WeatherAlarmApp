package com.example.weatheralarmapp.domain.usecase.weather

import android.util.Log
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
        private val FIRST_FORECAST_TIME = 6

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
                // 6時から3時間おきに天気情報を取得する
                val cnt =
                    if (alarmTime.hour < currentTime.hour) {
                        (alarmTime.hour + 24 - FIRST_FORECAST_TIME) / 3 + 1
                    } else {
                        (alarmTime.hour - FIRST_FORECAST_TIME) / 3 + 1
                    }

                getWeatherByLocation(result.lat, result.lon, cnt)
            } catch (e: Exception) {
//                updateAlarmUiState(id) {
//                    it.copy(
//                        weatherState = WeatherState.Error(e.message ?: "Unknown error"),
//                    )
//                }
                WeatherState.Error(e.message ?: "Unknown error")
            }

        private suspend fun getWeatherByLocation(
            lat: Double,
            lon: Double,
            cnt: Int,
        ): WeatherState {
//            updateAlarmUiState(id) {
//                it.copy(
//                    weatherState = WeatherState.Loading,
//                )
//            }
            return try {
                val result =
                    withContext(Dispatchers.IO) {
                        getWeatherRepository.getWeather(lat, lon, cnt)
                    }
//                updateAlarmUiState(id) {
//                    it.copy(
//                        weatherState =
//                        WeatherState.Success(
//                            result.list
//                                .last()
//                                .weather[0]
//                                .description,
//                        ),
//                    )
//                }
                WeatherState.Success(
                    result.list
                        .last()
                        .weather[0]
                        .description,
                )
            } catch (e: Exception) {
                Log.d("result", e.message.toString())
//                updateAlarmUiState(id) {
//                    it.copy(
//                        weatherState = WeatherState.Error(e.message ?: "Unknown error"),
//                    )
//                }
                WeatherState.Error(e.message ?: "Unknown error")
            }
        }
    }
