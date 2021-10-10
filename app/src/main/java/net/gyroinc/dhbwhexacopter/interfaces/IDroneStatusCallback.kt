package net.gyroinc.dhbwhexacopter.interfaces

import net.gyroinc.dhbwhexacopter.models.DroneStatus

interface IDroneStatusCallback {
    fun onUpdateReceived(status: DroneStatus)
}