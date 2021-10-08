package net.gyroinc.dhbwhexacopter.utils

import net.gyroinc.dhbwhexacopter.models.DroneGpsStatus
import net.gyroinc.dhbwhexacopter.models.DroneNavStatus
import net.gyroinc.dhbwhexacopter.models.DroneStatus
import net.gyroinc.dhbwhexacopter.models.Waypoint
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONArray
import org.json.JSONObject

class JsonMessage {
    companion object {
        fun toDroneNavStatus(json: JSONObject): DroneNavStatus {
            return DroneNavStatus(
                json.getInt("gps_mode"),
                json.getInt("nav_state"),
                json.getInt("action"),
                json.getInt("wp_number"),
                json.getInt("nav_error"),
                json.getInt("target_bearing")
            )
        }

        fun toDroneGpsStatus(json: JSONObject): DroneGpsStatus {
            return DroneGpsStatus(
                json.getInt("fix"),
                json.getInt("numsat"),
                json.getInt("lat"),
                json.getInt("lon"),
                json.getInt("alt"),
                json.getInt("speed"),
                json.getInt("ground_course"),
                json.getInt("hdop")
            )
        }

        fun toDroneStatus(json: JSONObject): DroneStatus {
            return DroneStatus(json.getBoolean("online"))
        }

        fun fromLedColors(
            r: Int,
            g: Int,
            b: Int,
            brightness: Int
        ): JSONObject {
            val jsonObject = JSONObject()
            jsonObject.put("r", r)
            jsonObject.put("g", g)
            jsonObject.put("b", b)
            jsonObject.put("brightness", brightness)
            return jsonObject
        }

        fun fromWaypoint(waypoint: Waypoint, last: Boolean): JSONObject {
            val json = JSONObject()
            json.put("wp_no", waypoint.wpNum)
            json.put("action", waypoint.getTypeID())
            json.put(
                "lat",
                if (waypoint.requiresPosition()) (waypoint.latLng.latitude * 10000000).toInt() else 0
            )
            json.put(
                "lon",
                if (waypoint.requiresPosition()) (waypoint.latLng.longitude * 10000000).toInt() else 0
            )
            json.put(
                "altitude",
                if (waypoint.requiresPosition()) (waypoint.altitude * 100).toInt() else 0
            )
            json.put("p1", waypoint.getP1())
            json.put("p2", waypoint.getP2())
            json.put("p3", 0)
            json.put("flag", if (last) 0xa5 else 0)
            return json
        }

        fun fromWaypoints(waypoints: List<Waypoint>): JSONArray {
            val jsonArray = JSONArray()
            waypoints.forEachIndexed { i, waypoint: Waypoint ->
                val jsonWaypoint =
                    if (waypoints.size == i + 1)
                        fromWaypoint(waypoint, true)
                    else
                        fromWaypoint(waypoint, false)
                jsonArray.put(jsonWaypoint)
            }
            return jsonArray
        }

        fun fromMqttMessage(message: MqttMessage): JSONObject {
            return  JSONObject(String(message.payload))
        }
    }
}