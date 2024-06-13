package com.cdjr.parkassist.car_info_video.repository

import android.content.Context
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.lang.Exception

private const val TAG = "MqttClient"

class CustomMqttClient(val context: Context) {

    private var mqttAndroidClient: MqttAndroidClient
    private val serverUri = "tcp://192.168.5.2:1883"
    private val topicDist = "sensor/dist"
    private val topicSpeed = "sensor/vel"
    private val topicTimeToHit = "topic/hit"
    private val clientId: String = MqttClient.generateClientId()
    fun setCallback(callback: MqttCallbackExtended?) {
        mqttAndroidClient.setCallback(callback)
    }

    init {
        mqttAndroidClient = MqttAndroidClient(context, serverUri, clientId)
    }

    fun getCarInfoDtoUpdates(): Flow<ResponseStatus> = callbackFlow {
        fun setCallBack() {
            mqttAndroidClient.setCallback(object : MqttCallbackExtended {
                override fun connectionLost(cause: Throwable?) {
                    Log.e(TAG, "MQTT connection lost")
                    trySend(ResponseStatus(error = Exception(cause), value = Float.NEGATIVE_INFINITY))
                }
                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    if (message != null) {
                        val value = message.toString().toFloatOrNull() ?: 0F
                        when (topic) {
                            topicDist -> {
                                trySend(element = ResponseStatus(value = value, valueType = CarDataType.DISTANCE))
                            }
                            topicSpeed -> {
                                trySend(element = ResponseStatus(value = value, valueType = CarDataType.SPEED))
                            }
                            topicTimeToHit -> {
                                trySend(element = ResponseStatus(value = value, valueType = CarDataType.TIME_TO_HIT))
                            }
                        }
                    }
                }
                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    Log.d(TAG, "message delivery complete")
                }
                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    serverURI?.let {
                        Log.w(TAG, it)
                    }
                }
            })
        }

        fun subscribe(subscriptionTopic: String, qos: Int = 0) {
            try {
                mqttAndroidClient.subscribe(subscriptionTopic, qos, context, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.w(TAG, "Subscribed to topic, $subscriptionTopic")
                    }
                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.e(TAG, "Subscription to topic $subscriptionTopic failed!")
                        trySend(ResponseStatus(error = Exception(exception), value = Float.NEGATIVE_INFINITY))
                    }
                })
            } catch (ex: MqttException) {
                Log.e(TAG, "Exception whilst subscribing to topic '$subscriptionTopic'", ex)
                ex.printStackTrace()
                trySend(ResponseStatus(error = ex, value = Float.NEGATIVE_INFINITY))
            }
        }

        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = true
        mqttConnectOptions.connectionTimeout = 3
        mqttConnectOptions.keepAliveInterval = 60
        try {
            mqttAndroidClient.connect(
                mqttConnectOptions, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d(TAG, "onSuccess: Successfully connected to the broker")
                        val disconnectBufferOptions = DisconnectedBufferOptions()
                        disconnectBufferOptions.isBufferEnabled = true
                        disconnectBufferOptions.bufferSize = 100
                        disconnectBufferOptions.isPersistBuffer = false
                        disconnectBufferOptions.isDeleteOldestMessages = false
                        mqttAndroidClient.setBufferOpts(disconnectBufferOptions)

                        setCallBack()

                        subscribe(subscriptionTopic = topicDist)
                        subscribe(subscriptionTopic = topicSpeed)
                        subscribe(subscriptionTopic = topicTimeToHit)
                    }
                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.e(TAG, "Failed to connect to: $serverUri; ${Log.getStackTraceString(exception)}")
                        trySend(ResponseStatus(error = Exception(exception), value = Float.NEGATIVE_INFINITY))
                    }
                }
            )
        } catch (ex: MqttException) {
            Log.e(TAG, ex.message, ex)
            ex.printStackTrace()
            trySend(ResponseStatus(error = ex, value = Float.NEGATIVE_INFINITY))
        }

        //subscribe(subscriptionTopic = topic)
        awaitClose {
            Log.d(TAG, "DANIELLE await close")
        }
    }
}