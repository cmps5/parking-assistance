package com.cdjr.parkassist.car_info_video.repository

import android.app.Application
import com.cdjr.parkassist.car_info_video.model.CarInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class CarInfoRepository(application: Application) {

    private val mqttClient = CustomMqttClient(context = application)
    private val carInfo = CarInfo()
    private var lastCarInfo = CarInfo()
    private var sameAsOldCounter = 0

    fun getCarInfo(): Flow<CarInfo> = channelFlow {
        launch {
            mqttClient.getCarInfoDtoUpdates().collect { response ->
                when (response.valueType) {
                    CarDataType.DISTANCE -> carInfo.distance = response.value
                    CarDataType.SPEED -> carInfo.speed = response.value
                    CarDataType.TIME_TO_HIT -> carInfo.timeToHit = response.value
                    CarDataType.UNKNOWN -> Unit
                }
            }
        }
        while (true) {
            delay(300L)
            if (carInfo == lastCarInfo) {
                sameAsOldCounter += 1
                if (sameAsOldCounter >= 50) {
                    carInfo.speed = 0F
                    carInfo.timeToHit = 0F
                    lastCarInfo = carInfo
                    sameAsOldCounter = 0
                }
            } else {
                lastCarInfo = carInfo
                sameAsOldCounter = 0
            }
            send(carInfo)
        }
    }

    fun dummyCarInfo(): Flow<CarInfo> = flow {
        fun randomFloat() = (-500..1000).random() / 100F

        while (true) {
            delay(500L)
            carInfo.speed = randomFloat()
            carInfo.distance = randomFloat()
            carInfo.timeToHit = randomFloat()
            emit(carInfo)
        }
    }
}
