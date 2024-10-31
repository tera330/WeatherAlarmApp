package com.example.weatheralarmapp.di

import com.example.weatheralarmapp.network.WeatherApi
import com.example.weatheralarmapp.network.WeatherApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    fun provideWeatherApiService(): WeatherApiService = WeatherApi.retrofitService
}
