package net.gyroinc.dhbwhexacopter.models

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class WaypointTypePosholdUnlim(wpNum: Int, latLng: LatLng, marker: Marker) :
    Waypoint(wpNum, latLng, marker) {
    override fun getTypeID(): Int {
        return Companion.getTypeID()
    }

    override fun getMarkerHue(): Float {
        return BitmapDescriptorFactory.HUE_MAGENTA
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
        return false
    }

    override fun getP1(): Int {
        return waitTime
    }

    override fun getP2(): Int {
        return getSpeedInCm()
    }

    companion object {
        fun getTypeID(): Int {
            return 2
        }

        fun getTypeString(): String {
            return "POSHOLD_UNLIM"
        }
    }
}