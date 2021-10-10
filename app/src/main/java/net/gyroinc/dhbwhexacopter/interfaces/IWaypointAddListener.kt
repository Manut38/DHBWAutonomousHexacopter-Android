package net.gyroinc.dhbwhexacopter.interfaces

import net.gyroinc.dhbwhexacopter.models.Waypoint
import kotlin.reflect.KClass

interface IWaypointAddListener {
    fun <T : Waypoint> addWaypoint(type: KClass<T>): Waypoint
}