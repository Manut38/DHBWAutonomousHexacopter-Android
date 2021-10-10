package net.gyroinc.dhbwhexacopter.interfaces

import net.gyroinc.dhbwhexacopter.models.DroneNavStatus

interface INavStatusCallback {
    fun onUpdateReceived(status: DroneNavStatus)
}