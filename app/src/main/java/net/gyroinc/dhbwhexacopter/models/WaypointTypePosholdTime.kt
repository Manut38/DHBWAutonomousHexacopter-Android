package net.gyroinc.dhbwhexacopter.models

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class WaypointTypePosholdTime(wpNum: Int, latLng: LatLng, marker: Marker) :
    Waypoint(wpNum, latLng, marker) {
    override fun getTypeID(): Int {
        return 3
    }

    override fun getMarkerIcon(): BitmapDescriptor {
        return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
    }

    override fun getTypeString(): String {
        return "POSHOLD_TIME"
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
        return waitTime
    }

    override fun getP2(): Int {
        return getSpeedInCm()
    }
}