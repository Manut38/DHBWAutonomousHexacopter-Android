package net.gyroinc.dhbwhexacopter.adapters

import net.gyroinc.dhbwhexacopter.models.Waypoint
import kotlin.reflect.KClass

class WaypointTypeListItem(val type: KClass<out Waypoint>, private val displayName: String) {
    override fun toString(): String {
        return displayName
    }
}