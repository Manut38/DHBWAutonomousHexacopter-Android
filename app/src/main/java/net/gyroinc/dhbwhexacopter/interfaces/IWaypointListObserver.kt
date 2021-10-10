package net.gyroinc.dhbwhexacopter.interfaces

interface IWaypointListObserver {
    fun onWaypointListUpdated()

    fun onWaypointIndexChanged(prevIndex: Int, newIndex: Int)

    fun onWaypointsCleared()

    fun onWaypointRemoved(index: Int)
}