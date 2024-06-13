package com.cdjr.parkassist.car_info_video.repository
/*
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import java.util.UUID
import java.util.concurrent.TimeUnit


class HiveMqttClient {

    private val client: Mqtt5BlockingClient = Mqtt5Client.builder()
        .identifier(UUID.randomUUID().toString())
        .serverHost("broker.hivemq.com")
        .buildBlocking()

    init {
        client.connect()
    }

    fun getCarInfoDtoUpdates(): Flow<CarInfoDto> = callbackFlow {
        try {
            client.publishes(MqttGlobalPublishFilter.ALL).use { publishes ->
                client.subscribeWith().topicFilter("test/topic").qos(MqttQos.AT_LEAST_ONCE).send()
                publishes.receive(1, TimeUnit.SECONDS).ifPresent { x: Mqtt5Publish? ->
                    x?.let {
                        val carInfoDto = Json.decodeFromString<CarInfoDto>(it.payload.toString())
                        trySend(carInfoDto)
                    }
                    println(x)
                }
                publishes.receive(100, TimeUnit.MILLISECONDS).ifPresent { x: Mqtt5Publish? -> println(x) }
            }
        } finally {
            client.disconnect()
        }
    }
}*/