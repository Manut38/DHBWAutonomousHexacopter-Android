package net.gyroinc.dhbwhexacopter.models

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class WaypointTypeNormal(wpNum: Int, latLng: LatLng, marker: Marker) :
    Waypoint(wpNum, latLng, marker) {
    override fun getTypeID(): Int {
        return Companion.getTypeID()
    }

    override fun getMarkerHue(): Float {
        return BitmapDescriptorFactory.HUE_RED
    }

    override fun getTypeString(): String {
        return Companion.getTypeString()
    }

    override fun isJumpable(): Boolean {
        return true
    }

    override fun isVisibleOnMap(): Boolean {
        return true
    }

    override fun requiresPosition(): Boolean {
        return true
    }

    override fun requiresSpeed(): Boolean {
        return true
    }

    override fun getP1(): Int {
        return getSpeedInCm()
    }

    override fun getP2(): Int {
        return 0
    }

    companion object {
        fun getTypeID(): Int {
            return 1
        }

        fun getTypeString(): String {
            return "WAYPOINT"
        }
    }
}
