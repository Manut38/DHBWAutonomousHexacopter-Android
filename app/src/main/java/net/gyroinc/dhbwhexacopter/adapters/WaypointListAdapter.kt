package net.gyroinc.dhbwhexacopter.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter
import net.gyroinc.dhbwhexacopter.BR
import net.gyroinc.dhbwhexacopter.R
import net.gyroinc.dhbwhexacopter.databinding.WaypointListItemBinding
import net.gyroinc.dhbwhexacopter.interfaces.IWaypointItemClickListener
import net.gyroinc.dhbwhexacopter.models.Waypoint


class WaypointListAdapter(
    dataSet: List<Waypoint> = emptyList(),
    private val itemClickListener: IWaypointItemClickListener,
    private val locationClickListener: IWaypointItemClickListener,
) :
    DragDropSwipeAdapter<Waypoint, WaypointListAdapter.ViewHolder>(dataSet) {

    class ViewHolder(val binding: WaypointListItemBinding) :
        DragDropSwipeAdapter.ViewHolder(binding.root) {
        fun bind(obj: Any?) {
            binding.setVariable(BR.waypoint, obj)
            binding.executePendingBindings()
        }
    }

    override fun getViewHolder(itemView: View): ViewHolder {
        throw NotImplementedError()
    }

    override fun onBindViewHolder(
        item: Waypoint,
        viewHolder: ViewHolder,
        position: Int
    ) {
        viewHolder.bind(item)
        viewHolder.binding.waypointItemLayout.setOnClickListener { itemClickListener.onClick(item) }
        viewHolder.binding.locationIcon.setOnClickListener { locationClickListener.onClick(item) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.waypoint_list_item, parent, false
        ) as WaypointListItemBinding

        return ViewHolder(binding)
    }

    override fun getViewToTouchToStartDraggingItem(
        item: Waypoint,
        viewHolder: ViewHolder,
        position: Int
    ): View {
        return viewHolder.binding.dragIcon
    }
}