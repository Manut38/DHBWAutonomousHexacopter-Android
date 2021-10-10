package net.gyroinc.dhbwhexacopter.models

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class WaypointTypeSetHead(wpNum: Int, latLng: LatLng, marker: Marker) :
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
        return if (headingReset) -1 else heading
    }

    override fun getP2(): Int {
        return 0
    }

    companion object {
        fun getTypeID(): Int {
            return 7
        }

        fun getTypeString(): String {
            return "SET_HEAD"
        }
    }
}