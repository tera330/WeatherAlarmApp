package com.example.weatheralarmapp.di

import android.content.Context
import com.example.weatheralarmapp.data.local.AlarmDatabase
import com.example.weatheralarmapp.data.local.AlarmItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAlarmDatabase(
        @ApplicationContext context: Context,
    ): AlarmDatabase = AlarmDatabase.getDatabase(context)

    @Provides
    @Singleton
    fun provideAlarmItemDao(alarmDatabase: AlarmDatabase): AlarmItemDao = alarmDatabase.alarmItemDao()
}
