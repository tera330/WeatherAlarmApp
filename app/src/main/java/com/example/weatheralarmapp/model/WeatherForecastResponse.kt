package com.example.weatheralarmapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherForecastResponse(
    val cod: String,
    val message: Int,
    val cnt: Int,
    val list: List<WeatherData>,
    val city: City,
)

@Serializable
data class WeatherData(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>,
    val clouds: Clouds,
    val wind: Wind,
    val visibility: Int,
    val pop: Double,
    val rain: Rain? = null,
    val sys: Sys,
    @SerialName("dt_txt") val dtTxt: String,
)

@Serializable
data class Main(
    val temp: Double,
    @SerialName("feels_like") val feelsLike: Double,
    @SerialName("temp_min") val tempMin: Double,
    @SerialName("temp_max") val tempMax: Double,
    val pressure: Int,
    @SerialName("sea_level") val seaLevel: Int,
    @SerialName("grnd_level") val grndLevel: Int,
    val humidity: Int,
    @SerialName("temp_kf") val tempKf: Double,
)

@Serializable
data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String,
)

@Serializable
data class Clouds(
    val all: Int,
)

@Serializable
data class Wind(
    val speed: Double,
    val deg: Int,
    val gust: Double,
)

@Serializable
data class Rain(
    @SerialName("3h") val threeHourVolume: Double,
)

@Serializable
data class Sys(
    val pod: String,
)

@Serializable
data class City(
    val id: Int,
    val name: String,
    val coord: Coord,
    val country: String,
    val population: Int,
    val timezone: Int,
    val sunrise: Long,
    val sunset: Long,
)

@Serializable
data class Coord(
    val lat: Double,
    val lon: Double,
)
