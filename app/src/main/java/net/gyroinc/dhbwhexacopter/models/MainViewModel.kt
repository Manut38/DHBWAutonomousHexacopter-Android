package net.gyroinc.dhbwhexacopter.models

import androidx.lifecycle.ViewModel
import java.util.*

class MainViewModel : ViewModel() {
    val waypoints: LinkedList<Waypoint> = LinkedList()
}