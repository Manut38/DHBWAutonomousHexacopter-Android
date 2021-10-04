package net.gyroinc.dhbwhexacopter.adapters

import android.content.Context
import android.widget.ArrayAdapter
import net.gyroinc.dhbwhexacopter.models.*

class WaypointTypeListAdapter(context: Context, resource: Int) :
    ArrayAdapter<WaypointTypeListItem>(context, resource) {
    init {
        add(WaypointTypeListItem(WaypointTypeNormal::class, "WAYPOINT"))
        add(WaypointTypeListItem(WaypointTypePosholdUnlim::class, "POSHOLD_UNLIM"))
        add(WaypointTypeListItem(WaypointTypePosholdTime::class, "POSHOLD_TIME"))
        add(WaypointTypeListItem(WaypointTypeRth::class, "RTH"))
        add(WaypointTypeListItem(WaypointTypeSetPoi::class, "SET_POI"))
        add(WaypointTypeListItem(WaypointTypeJump::class, "JUMP"))
        add(WaypointTypeListItem(WaypointTypeSetHead::class, "SET_HEAD"))
        add(WaypointTypeListItem(WaypointTypeLand::class, "LAND"))
    }
}