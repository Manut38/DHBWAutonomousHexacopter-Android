package net.gyroinc.dhbwhexacopter.utils

import com.google.android.gms.maps.model.LatLng
import net.gyroinc.dhbwhexacopter.models.DroneGpsStatus

class LatLngConverter {
    companion object {
        fun fromDroneGpsStatus(droneGpsStatus: DroneGpsStatus): LatLng{
            return LatLng(toLatLngDouble(droneGpsStatus.lon), toLatLngDouble(droneGpsStatus.lat))
        }

        fun toLatLngDouble(value: Int): Double {
            return value.toDouble() / 1000000
        }

        fun toLatLngInt(value: Double): Int {
            return (value * 10000000).toInt()
        }
    }
}