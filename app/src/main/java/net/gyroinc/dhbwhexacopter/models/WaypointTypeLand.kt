package net.gyroinc.dhbwhexacopter.models

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class WaypointTypeLand(wpNum: Int, latLng: LatLng, marker: Marker) :
    Waypoint(wpNum, latLng, marker) {
    override fun getTypeID(): Int {
        return 8
    }

    override fun getMarkerIcon(): BitmapDescriptor {
        return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)
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
        return getSpeedInCm()
    }

    override fun getP2(): Int {
        return elevationAdjustment
    }

    companion object {
        fun getTypeString(): String {
            return "LAND"
        }
    }
}