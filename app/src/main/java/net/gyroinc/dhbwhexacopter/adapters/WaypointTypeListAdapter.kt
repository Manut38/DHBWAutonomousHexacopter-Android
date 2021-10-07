package net.gyroinc.dhbwhexacopter.adapters

import android.content.Context
import android.widget.ArrayAdapter
import net.gyroinc.dhbwhexacopter.models.*

class WaypointTypeListAdapter(context: Context, resource: Int) :
    ArrayAdapter<WaypointTypeListItem>(context, resource) {
    init {
        add(WaypointTypeListItem(WaypointTypeNormal::class, WaypointTypeNormal.getTypeString()))
        add(WaypointTypeListItem(WaypointTypePosholdUnlim::class, WaypointTypePosholdUnlim.getTypeString()))
        add(WaypointTypeListItem(WaypointTypePosholdTime::class, WaypointTypePosholdTime.getTypeString()))
        add(WaypointTypeListItem(WaypointTypeRth::class, WaypointTypeRth.getTypeString()))
        add(WaypointTypeListItem(WaypointTypeSetPoi::class, WaypointTypeSetPoi.getTypeString()))
        add(WaypointTypeListItem(WaypointTypeJump::class, WaypointTypeJump.getTypeString()))
        add(WaypointTypeListItem(WaypointTypeSetHead::class, WaypointTypeSetHead.getTypeString()))
        add(WaypointTypeListItem(WaypointTypeLand::class, WaypointTypeLand.getTypeString()))
    }
}