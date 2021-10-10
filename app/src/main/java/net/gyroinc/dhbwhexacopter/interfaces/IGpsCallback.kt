package net.gyroinc.dhbwhexacopter.interfaces

import net.gyroinc.dhbwhexacopter.models.DroneGpsStatus

interface IGpsCallback {
    fun onUpdateReceived(status: DroneGpsStatus)
}