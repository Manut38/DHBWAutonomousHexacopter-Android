package net.gyroinc.dhbwhexacopter.models

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class WaypointTypeJump(wpNum: Int, latLng: LatLng, marker: Marker) :
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
        return false
    }

    override fun requiresSpeed(): Boolean {
        return false
    }

    override fun getP1(): Int {
        return jumpTarget
    }

    override fun getP2(): Int {
        return jumpRepeat
    }

    companion object {
        fun getTypeID(): Int {
            return 6
        }

        fun getTypeString(): String {
            return "JUMP"
        }
    }
}