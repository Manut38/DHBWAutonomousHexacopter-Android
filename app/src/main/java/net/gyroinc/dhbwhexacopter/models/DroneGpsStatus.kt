package net.gyroinc.dhbwhexacopter.models

import com.google.android.gms.maps.model.LatLng

class DroneGpsStatus(
    val fix: Int,
    val numsat: Int,
    private val lat: Int, //Latitude * 10^6
    private val lon: Int, //Longitude * 10^6
    val alt: Int,         // m
    val speed: Int,         // cm/s
    private val ground_course: Int,
    private val hdop: Int
) {

    fun getLatLng(): LatLng {
        return LatLng((lat.toDouble() / 1000000), (lon.toDouble() / 1000000))
    }


    companion object {
        const val GPS_NO_FIX = 0
        const val GPS_FIX_2D = 1
        const val GPS_FIX_3D = 2
    }
}
