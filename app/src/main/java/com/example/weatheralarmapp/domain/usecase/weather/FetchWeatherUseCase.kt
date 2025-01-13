package com.example.weatheralarmapp.domain.usecase.weather

import android.util.Log
import com.example.weatheralarmapp.data.repository.GetWeatherRepository
import com.example.weatheralarmapp.ui.features.alarm.WeatherState
import com.example.weatheralarmapp.util.dateformat.convertEpochToJST
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
                val cnt = calculateCnt(alarmTime)
                Log.d("result", "cnt: $cnt")
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
                Log.d("result", "time : ${convertEpochToJST(result.list.last().dt)}")

                WeatherState.Success(
                    result.list
                        .last()
                        .weather[0]
                        .description,
                )
            } catch (e: Exception) {
                WeatherState.Error(e.message ?: "Unknown error")
            }

        private fun calculateCnt(alarmTime: LocalTime): Int {
            val currentTime = LocalDateTime.now()

            val hourDifference =
                if (alarmTime.hour < currentTime.hour) {
                    alarmTime.hour + 24 - currentTime.hour
                } else {
                    alarmTime.hour - currentTime.hour
                }
            return (hourDifference / 3) + 1
        }
    }
