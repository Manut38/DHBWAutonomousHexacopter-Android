package net.gyroinc.dhbwhexacopter

import android.content.Context
import android.widget.Toast
import androidx.preference.PreferenceManager
import net.gyroinc.dhbwhexacopter.models.DroneGpsStatus
import net.gyroinc.dhbwhexacopter.models.DroneNavStatus
import net.gyroinc.dhbwhexacopter.models.DroneStatus
import net.gyroinc.dhbwhexacopter.models.Waypoint
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONArray
import org.json.JSONObject
import javax.net.ssl.SSLSocketFactory

class MQTTConnection(val context: Context) {
    lateinit var mqttClient: MqttAndroidClient
    private lateinit var navStatusCallback: INavStatusCallback
    private lateinit var droneStatusCallback: IDroneStatusCallback
    private lateinit var gpsCallback: IGpsCallback
    private lateinit var mqttStatusCallback: IMqttStatusCallback

    fun connect(): IMqttToken {
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

        mqttClient.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                mqttStatusCallback.onConnectionLost()
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                when (topic) {
                    TOPIC_GET_STATUS -> {
                        val json = JSONObject(String(message.payload))
                        val droneStatus = DroneStatus(json.getBoolean("online"))
                        droneStatusCallback.onUpdateReceived(droneStatus)
                    }
                    TOPIC_NAV_STATUS -> {
                        val json = JSONObject(String(message.payload))
                        navStatusCallback.onUpdateReceived(
                            DroneNavStatus(
                                json.getInt("gps_mode"),
                                json.getInt("nav_state"),
                                json.getInt("action"),
                                json.getInt("wp_number"),
                                json.getInt("nav_error"),
                                json.getInt("target_bearing")
                            )
                        )
                    }
                    TOPIC_GET_GPS -> {
                        val json = JSONObject(String(message.payload))
                        gpsCallback.onUpdateReceived(
                            DroneGpsStatus(
                                json.getInt("fix"),
                                json.getInt("numsat"),
                                json.getInt("lat"),
                                json.getInt("lon"),
                                json.getInt("alt"),
                                json.getInt("speed"),
                                json.getInt("ground_course"),
                                json.getInt("hdop")
                            )
                        )
                    }
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
//                TODO("Not yet implemented")
            }

        })

        return mqttClient.connect(options)
    }

    fun disconnect(): IMqttToken {
        return mqttClient.disconnect()
    }

    fun isConnected(): Boolean {
        return if (this::mqttClient.isInitialized) {
            mqttClient.isConnected
        } else false
    }

    fun setNavStatusCallback(callback: INavStatusCallback) {
        navStatusCallback = callback
        val token = mqttClient.subscribe(TOPIC_NAV_STATUS, 1)
        token.actionCallback = object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {}
            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Toast.makeText(
                    context,
                    "Failed to subscribe to drone status updates!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun setDroneStatusCallback(callback: IDroneStatusCallback) {
        droneStatusCallback = callback
        val token = mqttClient.subscribe(TOPIC_GET_STATUS, 1)
        token.actionCallback = object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {}
            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Toast.makeText(
                    context,
                    "Failed to subscribe to drone status updates!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun setGPSCallback(callback: IGpsCallback) {
        gpsCallback = callback
        val token = mqttClient.subscribe(TOPIC_GET_GPS, 1)
        token.actionCallback = object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {}
            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Toast.makeText(
                    context,
                    "Failed to subscribe to drone status updates!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun setMQTTStatusCallback(callback: IMqttStatusCallback) {
        mqttStatusCallback = callback
    }

    fun publishWaypoints(waypoints: List<Waypoint>) {
        val jsonArray = JSONArray()
        waypoints.forEachIndexed { i, waypoint: Waypoint ->
            val jsonWaypoint =
                if (waypoints.size == i + 1)
                    waypoint.getJSONObject(true)
                else
                    waypoint.getJSONObject(false)
            jsonArray.put(jsonWaypoint)
        }
        val message = MqttMessage(jsonArray.toString().toByteArray(Charsets.UTF_8))
        mqttClient.publish(TOPIC_SET_WP, message)
    }

    fun publishLEDColor(r: Int, g: Int, b: Int, brightness: Int) {
        val jsonObject = JSONObject()
        jsonObject.put("r", r)
        jsonObject.put("g", g)
        jsonObject.put("b", b)
        jsonObject.put("brightness", brightness)
        val message = MqttMessage(jsonObject.toString().toByteArray(Charsets.UTF_8))
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
