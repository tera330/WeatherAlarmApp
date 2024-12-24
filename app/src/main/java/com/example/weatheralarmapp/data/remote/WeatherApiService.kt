package com.example.weatheralarmapp.data.remote

import com.example.weatheralarmapp.BuildConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

private const val weatherApiKey = BuildConfig.WEATHER_API_KEY
private const val BASE_URL = "https://api.openweathermap.org/"

@OptIn(ExperimentalSerializationApi::class)
private val retrofit =
    Retrofit
        .Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(Json.asConverterFactory(contentType = "application/json".toMediaType()))
        .build()

interface WeatherApiService {
    @GET("geo/1.0/direct?")
    suspend fun getCoordinate(
        @Query("q") cityName: String,
        @Query("limit") limit: Int = 5,
        @Query("appid") apiKey: String = weatherApiKey,
    ): List<CoordinateResponse?>

    @GET("data/2.5/forecast?")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("cnt") cnt: Int,
        @Query("lang") lang: String = "ja",
        @Query("appid") apiKey: String = weatherApiKey,
    ): WeatherForecastResponse
}

object WeatherApi {
    val retrofitService: WeatherApiService by lazy {
        retrofit.create(WeatherApiService::class.java)
    }
}
