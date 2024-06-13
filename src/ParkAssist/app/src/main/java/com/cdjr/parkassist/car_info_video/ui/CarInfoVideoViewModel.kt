package com.cdjr.parkassist.car_info_video.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cdjr.parkassist.car_info_video.repository.CarInfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.lang.Math.round

class CarInfoVideoViewModel(application: Application) : AndroidViewModel(application = application) {

    private val timeToHitUpperLimit = 2f

    private val carInfoRepository = CarInfoRepository(application = application)

    private val _showUI = MutableStateFlow(false)
    val showUI: Flow<Boolean> = _showUI

    private val _uiData: StateFlow<CarInfoUIData> =
        carInfoRepository.getCarInfo()
            .map { carInfo ->
                val timeToHit = (round(carInfo.timeToHit * 10) / 10F)
                val carInfoUIData = CarInfoUIData(
                    distance = carInfo.distance,
                    speed = carInfo.speed,
                    timeToHit = timeToHit,
                    showHittingWarning = timeToHit > 0 && timeToHit <= timeToHitUpperLimit,
                )
                carInfoUIData
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = CarInfoUIData(),
            )

    val uiData = _uiData

    fun showUI(show: Boolean = true) {
        _showUI.value = show
    }
}
