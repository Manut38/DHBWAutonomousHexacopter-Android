package net.gyroinc.dhbwhexacopter.models

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class WaypointTypeNormal(wpNum: Int, latLng: LatLng, marker: Marker) :
    Waypoint(wpNum, latLng, marker) {
    override fun getTypeID(): Int {
        return 1
    }

    override fun getMarkerIcon(): BitmapDescriptor {
        return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
    }

    override fun getTypeString(): String {
        return "WAYPOINT"
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

    override fun toString(): String {
        return "WAYPOINT"
    }
}
