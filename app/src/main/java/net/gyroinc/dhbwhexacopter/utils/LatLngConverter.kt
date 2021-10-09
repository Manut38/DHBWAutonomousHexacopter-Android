package net.gyroinc.dhbwhexacopter.utils

import com.google.android.gms.maps.model.LatLng
import net.gyroinc.dhbwhexacopter.models.DroneGpsStatus

class LatLngConverter {
    companion object {
        fun fromDroneGpsStatus(droneGpsStatus: DroneGpsStatus): LatLng{
            return LatLng(convertToLatLngDouble(droneGpsStatus.lon), convertToLatLngDouble(droneGpsStatus.lat))
        }

        fun convertToLatLngDouble(value: Int): Double {
            return value.toDouble() / 1000000
        }
    }
}