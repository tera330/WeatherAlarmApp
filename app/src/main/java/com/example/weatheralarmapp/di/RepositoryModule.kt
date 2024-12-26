package com.example.weatheralarmapp.di

import com.example.weatheralarmapp.data.local.AlarmItemDao
import com.example.weatheralarmapp.data.remote.WeatherApiService
import com.example.weatheralarmapp.data.repository.AlarmItemRepository
import com.example.weatheralarmapp.data.repository.AlarmItemRepositoryImpl
import com.example.weatheralarmapp.data.repository.GetWeatherRepository
import com.example.weatheralarmapp.data.repository.GetWeatherRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideAlarmItemRepository(alarmItemDao: AlarmItemDao): AlarmItemRepository = AlarmItemRepositoryImpl(alarmItemDao)

    @Provides
    @Singleton
    fun provideGetWeatherRepository(weatherApiService: WeatherApiService): GetWeatherRepository =
        GetWeatherRepositoryImpl(weatherApiService)
}
