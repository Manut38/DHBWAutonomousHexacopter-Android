package net.gyroinc.dhbwhexacopter.models

class DroneGpsStatus(
    val fix: Int,
    val numsat: Int,
    val lat: Int, //Latitude * 10^6
    val lon: Int, //Longitude * 10^6
    val alt: Int,         // m
    val speed: Int,       // cm/s
    val ground_course: Int,
    val hdop: Int
) {

    companion object {
        const val GPS_NO_FIX = 0
        const val GPS_FIX_2D = 1
        const val GPS_FIX_3D = 2
    }
}
