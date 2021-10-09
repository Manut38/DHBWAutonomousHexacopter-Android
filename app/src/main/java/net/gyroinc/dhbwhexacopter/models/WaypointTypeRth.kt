package net.gyroinc.dhbwhexacopter.models

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class WaypointTypeRth(wpNum: Int, latLng: LatLng, marker: Marker) :
    Waypoint(wpNum, latLng, marker) {
    override fun getTypeID(): Int {
        return Companion.getTypeID()
    }

    override fun getMarkerIcon(): BitmapDescriptor {
        return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
    }

    override fun getTypeString(): String {
        return Companion.getTypeString()
    }

    override fun isJumpable(): Boolean {
        return false
    }

    override fun isVisibleOnMap(): Boolean {
        return false
    }

    override fun requiresPosition(): Boolean {
        return true
    }

    override fun requiresSpeed(): Boolean {
        return false
    }

    override fun getP1(): Int {
        return if (rthLand) 1 else 0
    }

    override fun getP2(): Int {
        return 0
    }

    companion object {
        fun getTypeID(): Int {
            return 4
        }

        fun getTypeString(): String {
            return "RTH"
        }
    }
}