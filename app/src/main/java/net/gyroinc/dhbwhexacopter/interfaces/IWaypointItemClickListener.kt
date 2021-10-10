package net.gyroinc.dhbwhexacopter.interfaces

import net.gyroinc.dhbwhexacopter.models.Waypoint

interface IWaypointItemClickListener {
    fun onClick(waypoint: Waypoint)
}