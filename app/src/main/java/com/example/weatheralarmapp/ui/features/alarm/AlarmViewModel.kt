package com.example.weatheralarmapp.ui.features.alarm

import android.app.AlarmManager
import android.util.Log
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatheralarmapp.data.local.AlarmItem
import com.example.weatheralarmapp.data.repository.AlarmItemRepository
import com.example.weatheralarmapp.data.repository.GetWeatherRepository
import com.example.weatheralarmapp.domain.usecase.AddAlarmItemUseCase
import com.example.weatheralarmapp.domain.usecase.DeleteAlarmItemUseCase
import com.example.weatheralarmapp.domain.usecase.UpdateAlarmItemUseCase
import com.example.weatheralarmapp.util.dateformat.createHourString
import com.example.weatheralarmapp.util.dateformat.createMinuteString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel
    @Inject
    constructor(
        private val alarmItemRepository: AlarmItemRepository,
        private val getWeatherRepository: GetWeatherRepository,
        private val addAlarmItemUseCase: AddAlarmItemUseCase,
        private val deleteAlarmItemUseCase: DeleteAlarmItemUseCase,
        private val updateAlarmItemUseCase: UpdateAlarmItemUseCase,
    ) : ViewModel() {
        private val currentTime = LocalDateTime.now()
        private val hourStr = createHourString(currentTime.hour)
        private val minutesStr = createMinuteString(currentTime.minute)

        private var _alarmUiState: MutableStateFlow<AlarmUiState> =
            MutableStateFlow(
                AlarmUiState(
                    alarmItemState =
                        AlarmItemState(
                            alarmTime = "$hourStr:$minutesStr",
                            changedAlarmTImeByWeather = "$hourStr:$minutesStr",
                        ),
                ),
            )
        val alarmUiState: StateFlow<AlarmUiState>
            get() = _alarmUiState.asStateFlow()

        private var _homeUiState: MutableStateFlow<HomeUiState> = MutableStateFlow(HomeUiState())
        val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

        // 　検証ように保存
//        init {
//            viewModelScope.launch {
//                alarmItemRepository
//                    .getAllAlarmItemsStream()
//                    .map { alarmItems ->
//                        alarmItems.map { alarmItem ->
//                            AlarmUiState(
//                                alarmItemState = AlarmItemState(
//                                    id = alarmItem.id,
//                                    alarmTime = alarmItem.alarmTime,
//                                    selectedEarlyAlarmTime = alarmItem.selectedEarlyAlarmTime,
//                                    changedAlarmTImeByWeather = alarmItem.changedAlarmTImeByWeather,
//                                    isAlarmOn = alarmItem.isAlarmOn,
//                                    isWeatherForecastOn = alarmItem.isWeatherForecastOn,
//                                )
//                            )
//                        }.toMutableStateList()
//                    }
//                    // .distinctUntilChanged() // 同じリストなら更新しない
//                    .collect { updatedAlarmItemList ->
//                        _homeUiState.update { currentState ->
//                            currentState.copy(alarmItemList = updatedAlarmItemList)
//                        }
//                    }
//            }
//        }

        // AlarmUiState自体をいじっていないので、WeatherStateの不要な初期化が発生しない可能性
        init {
            // Roomからのストリームデータと毎回計算が必要なUIデータを統合
            // ストリームが更新されるたびこのブロックが呼び出されるが、計算が必要な一時的なUIデータの初期化を防ぐ
            // ストリームから得られたデータと現在の状態を比較し、新しい状態として更新する
            viewModelScope.launch {
                alarmItemRepository.getAllAlarmItemsStream().collect { alarmItems ->
                    val newItemsMap = alarmItems.associateBy { it.id }
                    _homeUiState.update { currentState ->
                        val updatedList = currentState.alarmItemList.toMutableList()
                        // 削除されたアイテムを検出して削除
                        val newIds: Set<Int> = newItemsMap.keys
                        // ストリームから取得したアイテムのIDが現在のリストに存在しない場合、削除
                        val itemsToRemove = updatedList.filter { it.alarmItemState.id !in newIds }
                        updatedList.removeAll(itemsToRemove)

                        // 既存のアイテムを更新
                        updatedList.forEachIndexed { index, currentItem ->
                            val newItem = newItemsMap[currentItem.alarmItemState.id]
                            if (newItem != null) {
                                val newAlarmItemState =
                                    AlarmItemState(
                                        id = newItem.id,
                                        alarmTime = newItem.alarmTime,
                                        selectedEarlyAlarmTime = newItem.selectedEarlyAlarmTime,
                                        changedAlarmTImeByWeather = newItem.changedAlarmTImeByWeather,
                                        isAlarmOn = newItem.isAlarmOn,
                                        isWeatherForecastOn = newItem.isWeatherForecastOn,
                                    )

                                if (newAlarmItemState != currentItem.alarmItemState) {
                                    updatedList[index] = currentItem.copy(alarmItemState = newAlarmItemState)
                                }
                            }
                        }

                        // 新しいアイテムを追加
                        val currentIds = updatedList.map { it.alarmItemState.id }.toSet()
                        val itemsToAdd =
                            newIds.subtract(currentIds).map { id ->
                                val alarmItem = newItemsMap[id]!!
                                AlarmUiState(
                                    alarmItemState =
                                        AlarmItemState(
                                            id = alarmItem.id,
                                            alarmTime = alarmItem.alarmTime,
                                            selectedEarlyAlarmTime = alarmItem.selectedEarlyAlarmTime,
                                            changedAlarmTImeByWeather = alarmItem.changedAlarmTImeByWeather,
                                            isAlarmOn = alarmItem.isAlarmOn,
                                            isWeatherForecastOn = alarmItem.isWeatherForecastOn,
                                        ),
                                )
                            }
                        updatedList.addAll(itemsToAdd)

                        // 更新したリストを新しい HomeUiState に反映
                        currentState.copy(alarmItemList = updatedList.toMutableStateList())
                    }
                }
            }
        }

        private val FIRST_FORECAST_TIME = 6

//        fun expandedAlarmItem() {
//            _homeUiState.update {
//                it.copy(
//                    alarmItemList =
//                        it.alarmItemList.map { alarmUiState ->
//                            alarmUiState.copy(
//                                expandedAlarmItem = !alarmUiState.expandedAlarmItem,
//                            )
//                        },
//                )
//            }
//        }

        fun updateUntilAlarmTime(
            id: Int,
            hoursUntilAlarm: Long,
            minutesUntilAlarm: Long,
        ) {
            val index =
                _homeUiState.value.alarmItemList.indexOfFirst { it.alarmItemState.id == id }
            if (index != -1) {
                _homeUiState.value.alarmItemList[index] =
                    _homeUiState.value.alarmItemList[index].copy(
                        hoursUntilAlarm = hoursUntilAlarm,
                        minutesUntilAlarm = minutesUntilAlarm,
                    )
            }
        }

        fun updateUntilAlarmTimeByWeather(
            id: Int,
            earlyHoursUntilAlarm: Long,
            earlyMinutesUntilAlarm: Long,
        ) {
            val index =
                _homeUiState.value.alarmItemList.indexOfFirst { it.alarmItemState.id == id }
            if (index != -1) {
                _homeUiState.value.alarmItemList[index] =
                    _homeUiState.value.alarmItemList[index].copy(
                        earlyHoursUntilAlarmByWeather = earlyHoursUntilAlarm,
                        earlyMinutesUntilAlarmByWeather = earlyMinutesUntilAlarm,
                    )
            } else {
                alarmUiState
            }
        }

        suspend fun addAlarmItem(
            alarmManager: AlarmManager,
            alarmItem: AlarmItem,
        ) {
            addAlarmItemUseCase.addAlarmItem(alarmManager, alarmItem)
        }

        suspend fun updateAlarmItem(
            alarmManager: AlarmManager,
            alarmItem: AlarmItem,
            isBadWeather: Boolean,
        ) {
            updateAlarmItemUseCase.updateAlarmItem(alarmManager, alarmItem, isBadWeather)
        }

        suspend fun deleteAlarmItem(
            alarmUiState: AlarmUiState,
            alarmManager: AlarmManager,
        ) {
            val alarmItem = alarmUiState.alarmItemState.toAlarmItem(alarmUiState.alarmItemState)
            deleteAlarmItemUseCase.deleteAlarmItem(alarmItem, alarmManager)
        }

        private fun AlarmItemState.toAlarmItem(alarmItemState: AlarmItemState): AlarmItem =
            AlarmItem(
                id = alarmItemState.id,
                alarmTime = alarmItemState.alarmTime,
                changedAlarmTImeByWeather = alarmItemState.changedAlarmTImeByWeather,
                selectedEarlyAlarmTime = alarmItemState.selectedEarlyAlarmTime,
                isAlarmOn = alarmItemState.isAlarmOn,
                isWeatherForecastOn = alarmItemState.isWeatherForecastOn,
            )

        fun getWeatherByCityName(
            id: Int,
            cityName: String,
            alarmTime: LocalTime,
        ) {
            viewModelScope.launch {
                updateAlarmUiState(id) {
                    it.copy(
                        coordinateState = CoordinateState.Loading,
                    )
                }
                try {
                    val result =
                        withContext(Dispatchers.IO) {
                            getWeatherRepository.getCoordinate(cityName)
                        }
                    updateAlarmUiState(id) {
                        it.copy(
                            coordinateState = CoordinateState.Success(result.lat, result.lon),
                        )
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
                    getWeatherByLocation(id, result.lat, result.lon, cnt)
                } catch (e: Exception) {
                    updateAlarmUiState(id) {
                        it.copy(
                            coordinateState = CoordinateState.Error(e.message ?: "Unknown error"),
                        )
                    }
                }
            }
        }

        private fun getWeatherByLocation(
            id: Int,
            lat: Double,
            lon: Double,
            cnt: Int,
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                updateAlarmUiState(id) {
                    it.copy(
                        weatherState = WeatherState.Loading,
                    )
                }
                try {
                    val result = getWeatherRepository.getWeather(lat, lon, cnt)
                    updateAlarmUiState(id) {
                        it.copy(
                            weatherState =
                                WeatherState.Success(
                                    result.list
                                        .last()
                                        .weather[0]
                                        .description,
                                ),
                        )
                    }
                } catch (e: Exception) {
                    Log.d("result", e.message.toString())
                    updateAlarmUiState(id) {
                        it.copy(
                            weatherState = WeatherState.Error(e.message ?: "Unknown error"),
                        )
                    }
                }
            }
        }

        private fun updateAlarmUiState(
            id: Int,
            updateAlarmUiState: (AlarmUiState) -> AlarmUiState,
        ) {
            val index =
                _homeUiState.value.alarmItemList.indexOfFirst { it.alarmItemState.id == id }
            if (index != -1) {
                val updatedAlarmUiState = updateAlarmUiState(_homeUiState.value.alarmItemList[index])
                _homeUiState.value.alarmItemList[index] = updatedAlarmUiState
            }
        }
    }
