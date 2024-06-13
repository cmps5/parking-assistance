package com.cdjr.parkassist.car_info_video.repository

data class ResponseStatus(
    val value: Float,
    val valueType: CarDataType = CarDataType.UNKNOWN,
    val error: Exception? = null,
)

enum class CarDataType {
    DISTANCE, SPEED, TIME_TO_HIT, UNKNOWN,
}
