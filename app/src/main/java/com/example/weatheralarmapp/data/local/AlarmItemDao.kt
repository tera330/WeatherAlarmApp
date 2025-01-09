package com.example.weatheralarmapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmItemDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(alarmItem: AlarmItem)

    @Update
    suspend fun update(alarmItem: AlarmItem)

    @Delete
    suspend fun delete(alarmItem: AlarmItem)

    @Query("SELECT * from alarm_items WHERE id = :id")
    fun getAlarmItemById(id: Int): AlarmItem?

    @Query("SELECT * from alarm_items ORDER BY id ASC")
    fun getAllAlarmItems(): Flow<List<AlarmItem>>
}
