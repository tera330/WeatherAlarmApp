package com.example.weatheralarmapp.ui.features.alarm

import android.app.AlarmManager
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatheralarmapp.data.local.AlarmItem
import com.example.weatheralarmapp.data.repository.AlarmItemRepository
import com.example.weatheralarmapp.domain.usecase.alarm.AddAlarmItemUseCase
import com.example.weatheralarmapp.domain.usecase.alarm.DeleteAlarmItemUseCase
import com.example.weatheralarmapp.domain.usecase.alarm.UpdateAlarmItemUseCase
import com.example.weatheralarmapp.domain.usecase.weather.FetchWeatherUseCase
import com.example.weatheralarmapp.util.dateformat.createHourString
import com.example.weatheralarmapp.util.dateformat.createMinuteString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel
    @Inject
    constructor(
        private val alarmItemRepository: AlarmItemRepository,
        private val addAlarmItemUseCase: AddAlarmItemUseCase,
        private val deleteAlarmItemUseCase: DeleteAlarmItemUseCase,
        private val updateAlarmItemUseCase: UpdateAlarmItemUseCase,
        private val fetchWeatherUseCase: FetchWeatherUseCase,
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

        fun addAlarmItem(
            alarmManager: AlarmManager,
            alarmItem: AlarmItem,
        ) {
            viewModelScope.launch {
                addAlarmItemUseCase.addAlarmItem(alarmManager, alarmItem)
            }
        }

        fun updateAlarmItem(
            alarmManager: AlarmManager,
            alarmItem: AlarmItem,
            isBadWeather: Boolean,
        ) {
            viewModelScope.launch {
                updateAlarmItemUseCase.updateAlarmItem(alarmManager, alarmItem, isBadWeather)
            }
        }

        fun deleteAlarmItem(
            alarmUiState: AlarmUiState,
            alarmManager: AlarmManager,
        ) {
            val alarmItem = alarmUiState.alarmItemState.toAlarmItem(alarmUiState.alarmItemState)
            viewModelScope.launch {
                deleteAlarmItemUseCase.deleteAlarmItem(alarmItem, alarmManager)
            }
        }

        fun getWeatherByCityName(
            id: Int,
            cityName: String,
            alarmTime: LocalTime,
        ) {
            viewModelScope.launch {
                updateAlarmUiState(id) {
                    it.copy(
                        weatherState = WeatherState.Loading,
                    )
                }
                val result =
                    fetchWeatherUseCase.getWeatherByCityName(
                        cityName,
                        alarmTime,
                    )
                updateAlarmUiState(id) {
                    it.copy(
                        weatherState = result,
                    )
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
