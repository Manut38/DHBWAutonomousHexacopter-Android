package net.gyroinc.dhbwhexacopter.utils

import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONArray
import org.json.JSONObject

class MqttMessageBuilder {
    companion object {
        fun fromJsonObject(json: JSONObject): MqttMessage {
            return MqttMessage(
                json.toString().toByteArray(Charsets.UTF_8)
            )
        }

        fun fromJsonArray(json: JSONArray): MqttMessage {
            return MqttMessage(
                json.toString().toByteArray(Charsets.UTF_8)
            )
        }
    }
}