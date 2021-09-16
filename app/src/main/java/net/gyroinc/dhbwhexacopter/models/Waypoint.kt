package net.gyroinc.dhbwhexacopter.models

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import org.json.JSONObject

class Waypoint(type: Int, var wpNum: Int, private var latLng: LatLng, var marker: Marker) :
    ViewModel() {
    private var type: Int = 0
    var altitude: Float = 15f           // Altitude in m
    var speed: Float = 0.1f             // Speed in m/s
    var waitTime: Int = 0               // Wait Time in s
    var rthLand: Boolean = false        // Land when home?
    var jumpTarget: Int = 1             // Jump target waypoint
    var jumpRepeat: Int = 0             // Times jump repeated
    var elevationAdjustment: Int = 0    // Altitude difference from home in m
    var heading: Int = 0                // Rotation in deg
    var headingReset: Boolean = false   // Reset

    init {
        setWPNumber(wpNum)
        setType(type)
    }

    fun setType(type: Int) {
        this.type = type
        when (type) {
            TYPE_WAYPOINT -> {
                this.marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                this.marker.snippet = "Type: WAYPOINT"
            }
            TYPE_POSHOLD_UNLIM -> {
                this.marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                this.marker.snippet = "Type: POSHOLD_UNLIM"
            }
            TYPE_POSHOLD_TIME -> {
                this.marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                this.marker.snippet = "Type: POSHOLD_TIME"
            }
            TYPE_RTH -> {
                this.marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                this.marker.snippet = "Type: RTH"
            }
            TYPE_SET_POI -> {
                this.marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                this.marker.snippet = "Type: SET_POI"
            }
            TYPE_JUMP -> {
                this.marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                this.marker.snippet = "Type: JUMP"
            }
            TYPE_SET_HEAD -> {
                this.marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                this.marker.snippet = "Type: SET_HEAD"
            }
            TYPE_LAND -> {
                this.marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                this.marker.snippet = "Type: LAND"
            }
        }
        if (isVisibleOnMap()) {
            marker.isVisible = true
            refreshInfoWindow()
        } else {
            marker.isVisible = false
        }

    }

    fun refreshInfoWindow() {
        if (marker.isInfoWindowShown) {
            marker.hideInfoWindow()
            marker.showInfoWindow()
        }
    }

    fun getType(): Int {
        return type
    }

    fun setWPNumber(wpNum: Int) {
        this.wpNum = wpNum
        marker.title = "Waypoint #$wpNum"
        marker.tag = wpNum - 1
        refreshInfoWindow()
    }

    fun isJumpable(): Boolean {
        return listOf(
            TYPE_WAYPOINT,
            TYPE_POSHOLD_TIME,
            TYPE_POSHOLD_UNLIM,
            TYPE_LAND
        ).contains(type)
    }

    fun isVisibleOnMap(): Boolean {
        return listOf(
            TYPE_WAYPOINT,
            TYPE_POSHOLD_TIME,
            TYPE_POSHOLD_UNLIM,
            TYPE_SET_POI,
            TYPE_LAND
        ).contains(type)
    }

    private fun requiresPosition(): Boolean {
        return listOf(
            TYPE_WAYPOINT,
            TYPE_POSHOLD_TIME,
            TYPE_POSHOLD_UNLIM,
            TYPE_SET_POI,
            TYPE_LAND
        ).contains(type)
    }

    fun requiresSpeed(): Boolean {
        return listOf(
            TYPE_WAYPOINT,
            TYPE_POSHOLD_TIME,
            TYPE_LAND
        ).contains(type)
    }

    fun getJSONObject(last: Boolean): JSONObject {
        val json = JSONObject()
        json.put("wp_no", wpNum)
        json.put("action", type)
        json.put("lat", if (requiresPosition()) (latLng.latitude * 10000000).toInt() else 0)
        json.put("lon", if (requiresPosition()) (latLng.longitude * 10000000).toInt() else 0)
        json.put("altitude", if (requiresPosition()) (altitude * 100).toInt() else 0)
        var p1: Int = 0
        var p2: Int = 0
        val p3: Int = 0
        when (type) {
            TYPE_WAYPOINT -> {
                p1 = (speed * 100).toInt()
            }
            TYPE_POSHOLD_TIME -> {
                p1 = waitTime
                p2 = (speed * 100).toInt()
            }
            TYPE_RTH -> {
                p1 = if (rthLand) 1 else 0
            }
            TYPE_JUMP -> {
                p1 = jumpTarget
                p2 = jumpRepeat
            }
            TYPE_SET_HEAD -> {
                p1 = if (headingReset) -1 else heading
            }
            TYPE_LAND -> {
                p1 = (speed * 100).toInt()
                p2 = elevationAdjustment
            }
        }
        json.put("p1", p1)
        json.put("p2", p2)
        json.put("p3", p3)
        json.put("flag", if (last) 0xa5 else 0)
        return json
    }


    companion object {
        const val TYPE_WAYPOINT = 1
        const val TYPE_POSHOLD_UNLIM = 2
        const val TYPE_POSHOLD_TIME = 3
        const val TYPE_RTH = 4
        const val TYPE_SET_POI = 5
        const val TYPE_JUMP = 6
        const val TYPE_SET_HEAD = 7
        const val TYPE_LAND = 8

        @BindingAdapter("app:goneUnless")
        @JvmStatic
        fun goneUnless(view: View, visible: Boolean) {
            view.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }
}