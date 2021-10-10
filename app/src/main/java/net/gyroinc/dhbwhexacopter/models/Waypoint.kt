package net.gyroinc.dhbwhexacopter.models

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.primaryConstructor

abstract class Waypoint(var wpNum: Int, var latLng: LatLng, var marker: Marker) {
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
        setInfoWindowWPNumber(wpNum)
    }

    fun <T : Waypoint> convertTo(type: KClass<T>): T {
        return getInstanceOf(type, wpNum, latLng, marker)
    }

    private fun updateMarker() {
        with(marker) {
            setIcon(BitmapDescriptorFactory.defaultMarker(getMarkerHue()))
            snippet = "Type: " + getTypeString()
            if (isVisibleOnMap()) {
                isVisible = true
                refreshInfoWindow()
            } else {
                isVisible = false
            }
        }
    }

    private fun refreshInfoWindow() {
        if (marker.isInfoWindowShown) {
            marker.hideInfoWindow()
            marker.showInfoWindow()
        }
    }

    fun setInfoWindowWPNumber(wpNum: Int) {
        this.wpNum = wpNum
        marker.title = "Waypoint #$wpNum"
        marker.tag = wpNum - 1
        refreshInfoWindow()
    }

    fun getSpeedInCm(): Int {
        return (speed * 100).toInt()
    }

    fun getAltitudeInCm(): Int {
        return (altitude * 100).toInt()
    }

    abstract fun getTypeID(): Int

    abstract fun getMarkerHue(): Float

    abstract fun getTypeString(): String

    abstract fun isJumpable(): Boolean

    abstract fun isVisibleOnMap(): Boolean

    abstract fun requiresPosition(): Boolean

    abstract fun requiresSpeed(): Boolean

    abstract fun getP1(): Int

    abstract fun getP2(): Int

    companion object {
        fun <T : Waypoint> getInstanceOf(
            type: KClass<T>,
            wpNum: Int,
            latLng: LatLng,
            marker: Marker
        ): T {
            val actualRuntimeClassConstructor: KFunction<T> = type.primaryConstructor!!
            val waypoint = actualRuntimeClassConstructor.call(wpNum, latLng, marker)
            waypoint.updateMarker()
            return waypoint
        }
    }
}