package net.gyroinc.dhbwhexacopter

import android.content.Context
import androidx.preference.PreferenceManager
import net.gyroinc.dhbwhexacopter.models.DroneGpsStatus
import net.gyroinc.dhbwhexacopter.models.DroneNavStatus
import net.gyroinc.dhbwhexacopter.models.DroneStatus
import net.gyroinc.dhbwhexacopter.models.Waypoint
import net.gyroinc.dhbwhexacopter.utils.JsonMessage
import net.gyroinc.dhbwhexacopter.utils.MqttMessageBuilder
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import javax.net.ssl.SSLSocketFactory

class MQTTConnection(val context: Context) {
    private lateinit var mqttClient: MqttAndroidClient
    private lateinit var navStatusCallback: INavStatusCallback
    private lateinit var droneStatusCallback: IDroneStatusCallback
    private lateinit var gpsCallback: IGpsCallback
    private lateinit var mqttStatusCallback: IMqttStatusCallback

    fun connect(callback: IMqttActionListener) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val serverAddress = sharedPreferences.getString("mqtt_server", "")
        val port = sharedPreferences.getString("mqtt_port", "1883")
        val username = sharedPreferences.getString("mqtt_username", "")
        val password = sharedPreferences.getString("mqtt_password", "")
        val useTls = sharedPreferences.getBoolean("mqtt_use_tls", false)
        val serverAddressUri =
            if (useTls) "ssl://$serverAddress:$port" else "tcp://$serverAddress:$port"

        mqttClient = MqttAndroidClient(context, serverAddressUri, username)
        val options = MqttConnectOptions()
        options.userName = username
        options.password = password?.toCharArray()
        if (useTls) options.socketFactory = SSLSocketFactory.getDefault()

        setMqttCallbacks()
        mqttClient.connect(options).also { token -> token.actionCallback = callback }
    }

    private fun setMqttCallbacks() {
        mqttClient.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                mqttStatusCallback.onConnectionLost()
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                processMqttMessage(topic, message)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
            }
        })
    }

    private fun processMqttMessage(
        topic: String,
        message: MqttMessage
    ) {
        val json = JsonMessage.fromMqttMessage(message)
        when (topic) {
            TOPIC_GET_STATUS -> {
                droneStatusCallback.onUpdateReceived(JsonMessage.toDroneStatus(json))
            }
            TOPIC_NAV_STATUS -> {
                navStatusCallback.onUpdateReceived(JsonMessage.toDroneNavStatus(json))
            }
            TOPIC_GET_GPS -> {
                gpsCallback.onUpdateReceived(JsonMessage.toDroneGpsStatus(json))
            }
        }
    }

    fun disconnect(): IMqttToken {
        return mqttClient.disconnect()
    }

    fun unregisterResources() {
        return mqttClient.unregisterResources()
    }

    fun isConnected(): Boolean {
        return if (this::mqttClient.isInitialized) {
            mqttClient.isConnected
        } else false
    }

    fun setNavStatusCallback(callback: INavStatusCallback) {
        navStatusCallback = callback
    }

    fun setDroneStatusCallback(callback: IDroneStatusCallback) {
        droneStatusCallback = callback
    }

    fun subscribeToTopics(callback: IMqttActionListener) {
        mqttClient.subscribe(TOPIC_GET_STATUS, 1).also { token -> token.actionCallback = callback }
        mqttClient.subscribe(TOPIC_NAV_STATUS, 1).also { token -> token.actionCallback = callback }
        mqttClient.subscribe(TOPIC_GET_GPS, 1).also { token -> token.actionCallback = callback }
    }

    fun setGPSCallback(callback: IGpsCallback) {
        gpsCallback = callback
    }

    fun setMQTTStatusCallback(callback: IMqttStatusCallback) {
        mqttStatusCallback = callback
    }

    fun publishWaypoints(waypoints: List<Waypoint>) {
        val message = MqttMessageBuilder.fromJsonArray(JsonMessage.fromWaypoints(waypoints))
        mqttClient.publish(TOPIC_SET_WP, message)
    }

    fun publishLEDColor(r: Int, g: Int, b: Int, brightness: Int) {
        val message =
            MqttMessageBuilder.fromJsonObject(JsonMessage.fromLedColors(r, g, b, brightness))
        mqttClient.publish(TOPIC_SET_LED, message)
    }

    companion object {
        const val TAG = "MQTTConnection"

        private const val TOPIC_GET_STATUS = "airstation/status"
        private const val TOPIC_NAV_STATUS = "airstation/nav_status"
        private const val TOPIC_SET_WP = "app/waypoints"
        private const val TOPIC_GET_WP = "airstation/waypoints"
        private const val TOPIC_GET_GPS = "airstation/gps"
        private const val TOPIC_SET_LED = "app/led"
        private const val TOPIC_GET_LED = "airstation/led"
    }
}

interface IMqttStatusCallback {
    fun onConnectionLost()
}

interface IDroneStatusCallback {
    fun onUpdateReceived(status: DroneStatus)
}

interface INavStatusCallback {
    fun onUpdateReceived(status: DroneNavStatus)
}

interface IGpsCallback {
    fun onUpdateReceived(status: DroneGpsStatus)
}
