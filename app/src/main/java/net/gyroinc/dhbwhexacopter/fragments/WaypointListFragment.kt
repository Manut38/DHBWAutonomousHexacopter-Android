package net.gyroinc.dhbwhexacopter.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemDragListener
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemSwipeListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import net.gyroinc.dhbwhexacopter.R
import net.gyroinc.dhbwhexacopter.adapters.WaypointListAdapter
import net.gyroinc.dhbwhexacopter.interfaces.IWaypointAddListener
import net.gyroinc.dhbwhexacopter.interfaces.IWaypointItemClickListener
import net.gyroinc.dhbwhexacopter.interfaces.IWaypointListObserver
import net.gyroinc.dhbwhexacopter.models.MainViewModel
import net.gyroinc.dhbwhexacopter.models.Waypoint
import net.gyroinc.dhbwhexacopter.models.WaypointTypeJump
import net.gyroinc.dhbwhexacopter.models.WaypointTypeRth

class WaypointListFragment(
    private val waypointListObserver: IWaypointListObserver,
    private val waypointLocationClickListener: IWaypointItemClickListener,
    private val waypointAddListener: IWaypointAddListener
) : BottomSheetDialogFragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var waypointList: DragDropSwipeRecyclerView
    private lateinit var waypointListAdapter: WaypointListAdapter
    private lateinit var buttonClear: ImageView
    private lateinit var buttonAddRth: ImageView
    private lateinit var buttonAddJump: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.waypoint_list_fragment, container, false)
        bindViews(view)
        setupButtonActions(view)
        setupWaypointList()
        return view
    }

    private fun setupWaypointList() {
        waypointList.layoutManager = LinearLayoutManager(context)
        waypointList.orientation =
            DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING

        val onItemClickListener = object : IWaypointItemClickListener {
            override fun onClick(waypoint: Waypoint) {
                val dialog = WaypointPropertiesFragment(waypointListObserver).also { dialog ->
                    dialog.arguments = Bundle().also { bundle ->
                        bundle.putInt(
                            "waypointIndex",
                            waypoint.marker.tag as Int
                        )
                    }
                    dialog.onDismissListener =
                        DialogInterface.OnDismissListener {
                            waypointListAdapter.dataSet = viewModel.waypoints
                        }
                }
                dialog.show(
                    (context as AppCompatActivity).supportFragmentManager,
                    WaypointPropertiesFragment.TAG
                )
            }
        }

        val onLocationClickListener = object : IWaypointItemClickListener {
            override fun onClick(waypoint: Waypoint) {
                dismiss()
                waypointLocationClickListener.onClick(waypoint)
            }
        }

        waypointListAdapter =
            WaypointListAdapter(
                viewModel.waypoints,
                onItemClickListener,
                onLocationClickListener
            )
        waypointList.adapter = waypointListAdapter

        waypointList.swipeListener =
            object : OnItemSwipeListener<Waypoint> {
                override fun onItemSwiped(
                    position: Int,
                    direction: OnItemSwipeListener.SwipeDirection,
                    item: Waypoint
                ): Boolean {
                    waypointListObserver.onWaypointRemoved(position)
                    return false
                }
            }
        waypointList.dragListener =
            object : OnItemDragListener<Waypoint> {
                override fun onItemDragged(
                    previousPosition: Int,
                    newPosition: Int,
                    item: Waypoint
                ) {
                }

                override fun onItemDropped(
                    initialPosition: Int,
                    finalPosition: Int,
                    item: Waypoint
                ) {
                    waypointListObserver.onWaypointIndexChanged(initialPosition, finalPosition)
                    waypointListAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun setupButtonActions(view: View) {
        view.findViewById<ImageView>(R.id.close_button).setOnClickListener { dismiss() }

        buttonClear.setOnClickListener {
            val builder: AlertDialog.Builder? = activity?.let {
                AlertDialog.Builder(it)
            }
            builder?.setMessage(R.string.waypoint_list_clear_confirm)
            builder?.apply {
                setPositiveButton(R.string.dialog_ok) { _, _ ->
                    waypointListObserver.onWaypointsCleared()
                    for (i in 0 until waypointListAdapter.itemCount) {
                        waypointListAdapter.removeItem(0)
                    }
                }
                setNegativeButton(R.string.dialog_cancel) { dialog, _ ->
                    dialog.cancel()
                }
            }
            builder?.create()?.show()
        }

        buttonAddRth.setOnClickListener {
            val wp = waypointAddListener.addWaypoint(WaypointTypeRth::class)
            waypointListAdapter.addItem(wp)
            waypointList.smoothScrollToPosition(waypointListAdapter.itemCount - 1)
        }

        buttonAddJump.setOnClickListener {
            val wp = waypointAddListener.addWaypoint(WaypointTypeJump::class)
            waypointListAdapter.addItem(wp)
            waypointList.smoothScrollToPosition(waypointListAdapter.itemCount - 1)
        }
    }

    private fun bindViews(view: View) {
        waypointList = view.findViewById(R.id.waypoint_list)
        buttonClear = view.findViewById(R.id.button_clear_waypoints)
        buttonAddRth = view.findViewById(R.id.button_add_rth)
        buttonAddJump = view.findViewById(R.id.button_add_jump)
    }

    companion object {
        const val TAG = "WaypointListFragment"
    }
}