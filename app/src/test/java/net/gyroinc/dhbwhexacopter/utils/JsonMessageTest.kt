package net.gyroinc.dhbwhexacopter.utils

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import io.mockk.mockk
import net.gyroinc.dhbwhexacopter.models.*
import org.json.JSONException
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class JsonMessageTest {

    @Nested
    inner class ToDroneNavStatus {
        @Test
        fun `with expected values`() {
            val json = JSONObject()
            json.put("gps_mode", 1)
            json.put("nav_state", 2)
            json.put("action", 3)
            json.put("wp_number", 4)
            json.put("nav_error", 5)
            json.put("target_bearing", 0)
            val status: DroneNavStatus = JsonMessage.toDroneNavStatus(json)
            assertEquals(1, status.gps_mode)
            assertEquals(2, status.nav_state)
            assertEquals(3, status.action)
            assertEquals(4, status.wp_number)
            assertEquals(5, status.nav_error)
            assertEquals(0, status.target_bearing)
        }

        @Test
        fun `throws exception with unexpected value types`() {
            val json = JSONObject()
            json.put("gps_mode", "none")
            json.put("nav_state", 0.5)
            json.put("action", 3)
            json.put("wp_number", 4)
            json.put("nav_error", 5)
            json.put("target_bearing", 0)
            assertThrows<JSONException> {
                JsonMessage.toDroneNavStatus(json)
            }
        }

        @Test
        fun `throws exception with missing values`() {
            val json = JSONObject()
            json.put("gps_mode", 1)
            json.put("nav_state", 2)
            json.put("action", 3)
            json.put("wp_number", 4)
            json.put("nav_error", 5)
//            json.put("target_bearing", 0)
            assertThrows<JSONException> {
                JsonMessage.toDroneNavStatus(json)
            }
        }
    }

    @Nested
    inner class ToDroneGpsStatus {
        @Test
        fun `with expected values`() {
            val json = JSONObject()
            json.put("fix", 1)
            json.put("numsat", 2)
            json.put("lat", 3)
            json.put("lon", 4)
            json.put("alt", 5)
            json.put("speed", 6)
            json.put("ground_course", 7)
            json.put("hdop", 8)
            val status: DroneGpsStatus = JsonMessage.toDroneGpsStatus(json)
            assertEquals(1, status.fix)
            assertEquals(2, status.numsat)
            assertEquals(3, status.lat)
            assertEquals(4, status.lon)
            assertEquals(5, status.alt)
            assertEquals(6, status.speed)
            assertEquals(7, status.ground_course)
            assertEquals(8, status.hdop)
        }

        @Test
        fun `throws exception with unexpected value types`() {
            val json = JSONObject()
            json.put("fix", "string")
            json.put("numsat", 0.5)
            json.put("lat", 3)
            json.put("lon", 4)
            json.put("alt", 5)
            json.put("speed", 6)
            json.put("ground_course", 7)
            json.put("hdop", 8)
            assertThrows<JSONException> {
                JsonMessage.toDroneGpsStatus(json)
            }
        }

        @Test
        fun `throws exception with missing values`() {
            val json = JSONObject()
            json.put("fix", "string")
            json.put("numsat", 0.5)
            json.put("lat", 3)
            json.put("lon", 4)
            json.put("alt", 5)
            json.put("speed", 6)
            json.put("ground_course", 7)
            //json.put("hdop", 8)
            assertThrows<JSONException> {
                JsonMessage.toDroneGpsStatus(json)
            }
        }
    }

    @Nested
    inner class ToDroneStatus {
        @Test
        fun `with expected values`() {
            val json = JSONObject()
            json.put("online", true)
            val status: DroneStatus = JsonMessage.toDroneStatus(json)
            assertEquals(true, status.online)
        }

        @Test
        fun `throws exception with unexpected value types`() {
            val json = JSONObject()
            json.put("online", "yes")
            assertThrows<JSONException> {
                JsonMessage.toDroneStatus(json)
            }
        }

        @Test
        fun `throws exception with missing values`() {
            val json = JSONObject()
//            json.put("online", "yes")
            assertThrows<JSONException> {
                JsonMessage.toDroneStatus(json)
            }
        }
    }

    @Nested
    inner class FromLedColors {
        @Test
        fun `returns correctly formatted led json object`() {
            val json = JsonMessage.fromLedColors(253, 254, 255, 128)
            assertEquals(253, json.getInt("r"))
            assertEquals(254, json.getInt("g"))
            assertEquals(255, json.getInt("b"))
            assertEquals(128, json.getInt("brightness"))
        }
    }

    @Nested
    inner class FromWaypoint {
        private var testWaypoint: Waypoint

        init {
            val latLng = LatLng(30.0, 25.0)
            val marker: Marker = mockk(relaxed = true)
            testWaypoint = WaypointTypeNormal(1, latLng, marker)
            testWaypoint.altitude = 10f
        }

        @Test
        fun `returns correctly formatted waypoint json object`() {
            val json = JsonMessage.fromWaypoint(testWaypoint, false)
            assertEquals(1, json.getInt("wp_no"))
            assertEquals(testWaypoint.getTypeID(), json.getInt("action"))
            assertEquals(300000000, json.getInt("lat"))
            assertEquals(250000000, json.getInt("lon"))
            assertEquals(1000, json.getInt("altitude"))
            assertEquals(testWaypoint.getP1(), json.getInt("p1"))
            assertEquals(testWaypoint.getP2(), json.getInt("p2"))
            assertEquals(0, json.getInt("p3"))
            assertEquals(0, json.getInt("flag"))
        }

        @Test
        fun `sets correct flag for last waypoint`() {
            val json = JsonMessage.fromWaypoint(testWaypoint, true)
            assertEquals(0xa5, json.getInt("flag"))
        }
    }

    @Nested
    inner class FromWaypoints {
        private var testWaypoint: Waypoint
        private var waypointList: List<Waypoint>

        init {
            val latLng = LatLng(30.0, 25.0)
            val marker: Marker = mockk(relaxed = true)
            testWaypoint = WaypointTypeNormal(1, latLng, marker)
            testWaypoint.altitude = 10f
            waypointList = listOf(testWaypoint, testWaypoint, testWaypoint)
        }

        @Test
        fun `returns json array with correct length`() {
            val jsonArray = JsonMessage.fromWaypoints(waypointList)
            assertEquals(waypointList.size, jsonArray.length())
        }

        @Test
        fun `sets correct flag for last waypoint`() {
            val jsonArray = JsonMessage.fromWaypoints(waypointList)
            val lastObject = jsonArray.get(2) as JSONObject
            assertEquals(0xa5, lastObject.getInt("flag"))
        }
    }
}