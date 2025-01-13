package com.example.weatheralarmapp.di

import android.content.Context
import com.example.weatheralarmapp.data.repository.AlarmItemRepository
import com.example.weatheralarmapp.domain.AlarmHelper
import com.example.weatheralarmapp.domain.usecase.alarm.AddAlarmItemUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    @Singleton
    fun provideAddAlarmItemUseCase(
        alarmHelper: AlarmHelper,
        alarmItemRepository: AlarmItemRepository,
    ): AddAlarmItemUseCase = AddAlarmItemUseCase(alarmHelper, alarmItemRepository)

    @Module
    @InstallIn(SingletonComponent::class)
    object AlarmHelperModule {
        @Provides
        @Singleton
        fun provideAlarmHelper(
            @ApplicationContext context: Context,
        ): AlarmHelper = AlarmHelper(context)
    }
}
