package net.gyroinc.dhbwhexacopter.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.switchmaterial.SwitchMaterial
import net.gyroinc.dhbwhexacopter.R
import net.gyroinc.dhbwhexacopter.activities.MissionPlannerActivity
import net.gyroinc.dhbwhexacopter.models.MainViewModel
import net.gyroinc.dhbwhexacopter.models.Waypoint
import net.gyroinc.dhbwhexacopter.utils.InputFilterMinMax

class WaypointPropertiesFragment : BottomSheetDialogFragment(), View.OnClickListener,
    AdapterView.OnItemClickListener {

    private val viewModel: MainViewModel by activityViewModels()
    private var waypointIndex: Int = 0
    private lateinit var typeSpinner: AutoCompleteTextView
    private lateinit var propertiesView: LinearLayout
    var onDismissListener: DialogInterface.OnDismissListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.waypoint_properties_fragment, container, false)

        view.findViewById<ImageView>(R.id.close_button).setOnClickListener { dismiss() }

        waypointIndex = requireArguments().getInt("waypointIndex")
        propertiesView = view.findViewById(R.id.waypoint_properties)

        val saveButton: Button = view.findViewById(R.id.waypoint_button_save)
        saveButton.setOnClickListener(this)

        val deleteButton: Button = view.findViewById(R.id.waypoint_button_delete)
        deleteButton.setOnClickListener(this)

        val waypointTitle: TextView = view.findViewById(R.id.waypoint_properties_title)
        waypointTitle.text = getString(R.string.waypoint_dialog_title, waypointIndex + 1)

        typeSpinner = view.findViewById(R.id.waypoint_type_spinner)
        ArrayAdapter.createFromResource(
            requireContext(), R.array.waypoint_types, R.layout.list_item
        ).also { adapter ->
            typeSpinner.setAdapter(adapter)
            typeSpinner.onItemClickListener = this
        }

        restoreWaypointType()

        return view
    }

    private fun restoreWaypointType() {
        val type = viewModel.waypoints[waypointIndex].getType()
        typeSpinner.setText(typeSpinner.adapter.getItem(type - 1).toString(), false)
        setWaypointType(type)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.waypoint_button_save -> {
                dismiss()
            }
            R.id.waypoint_button_delete -> {
                (activity as MissionPlannerActivity).onWaypointRemoved(waypointIndex)
                dismiss()
            }
        }
    }

    override fun onItemClick(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        setWaypointType(pos + 1)
    }

    private fun setWaypointType(type: Int) {
        propertiesView.removeAllViews()
        viewModel.waypoints[waypointIndex].setType(type)
        when (type) {
            Waypoint.TYPE_WAYPOINT -> {
                layoutInflater.inflate(R.layout.waypoint_type_wp, propertiesView)
            }
            Waypoint.TYPE_POSHOLD_UNLIM -> {
                layoutInflater.inflate(R.layout.waypoint_type_poshold_unlim, propertiesView)
            }
            Waypoint.TYPE_POSHOLD_TIME -> {
                layoutInflater.inflate(R.layout.waypoint_type_poshold_time, propertiesView)
            }
            Waypoint.TYPE_RTH -> {
                layoutInflater.inflate(R.layout.waypoint_type_rth, propertiesView)
            }
            Waypoint.TYPE_SET_POI -> {

            }
            Waypoint.TYPE_JUMP -> {
                layoutInflater.inflate(R.layout.waypoint_type_jump, propertiesView)
            }
            Waypoint.TYPE_SET_HEAD -> {
                layoutInflater.inflate(R.layout.waypoint_type_set_head, propertiesView)
            }
            Waypoint.TYPE_LAND -> {
                layoutInflater.inflate(R.layout.waypoint_type_land, propertiesView)
            }
        }

        (activity as MissionPlannerActivity).updatePolylines()

        val altitude: EditText? = propertiesView.findViewById(R.id.waypoint_altitude)
        altitude?.filters = arrayOf(InputFilterMinMax(1f, 40f))
        altitude?.setText(viewModel.waypoints[waypointIndex].altitude.toString())
        altitude?.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty())
                viewModel.waypoints[waypointIndex].altitude = text.toString().toFloat()
        }

        val speed: EditText? = propertiesView.findViewById(R.id.waypoint_speed)
        speed?.filters = arrayOf(InputFilterMinMax(0f, 2f))
        speed?.setText(viewModel.waypoints[waypointIndex].speed.toString())
        speed?.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty())
                viewModel.waypoints[waypointIndex].speed = text.toString().toFloat()
        }

        val waitTime: EditText? = propertiesView.findViewById(R.id.waypoint_wait_time)
        waitTime?.setText(viewModel.waypoints[waypointIndex].waitTime.toString())
        waitTime?.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty())
                viewModel.waypoints[waypointIndex].waitTime = text.toString().toInt()
        }

        val jumpTarget: EditText? = propertiesView.findViewById(R.id.waypoint_jump_target)
        jumpTarget?.filters = arrayOf<InputFilter>(InputFilterMinMax(1f, 999f))
        jumpTarget?.setText(viewModel.waypoints[waypointIndex].jumpTarget.toString())
        jumpTarget?.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty()) {
                viewModel.waypoints[waypointIndex].jumpTarget = text.toString().toInt()
                (activity as MissionPlannerActivity).updatePolylines()
            }
        }

        val jumpRepeat: EditText? = propertiesView.findViewById(R.id.waypoint_jump_repeat)
        jumpRepeat?.setText(viewModel.waypoints[waypointIndex].jumpRepeat.toString())
        jumpRepeat?.filters = arrayOf<InputFilter>(InputFilterMinMax(-1f, 999f))
        jumpRepeat?.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty()) {
                try {
                    viewModel.waypoints[waypointIndex].jumpRepeat = text.toString().toInt()
                } catch (nfe: NumberFormatException) {
                    viewModel.waypoints[waypointIndex].jumpRepeat = 0
                }
            }
        }

        val elevationAdjustment: EditText? =
            propertiesView.findViewById(R.id.waypoint_elevation_adjustment)
        elevationAdjustment?.setText(viewModel.waypoints[waypointIndex].elevationAdjustment.toString())
        elevationAdjustment?.filters = arrayOf(InputFilterMinMax(-999f, 999f))
        elevationAdjustment?.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty()) {
                try {
                    viewModel.waypoints[waypointIndex].elevationAdjustment = text.toString().toInt()
                } catch (nfe: NumberFormatException) {
                    viewModel.waypoints[waypointIndex].elevationAdjustment = 0
                }
            }
        }

        val heading: EditText? = propertiesView.findViewById(R.id.waypoint_heading)
        heading?.setText(viewModel.waypoints[waypointIndex].heading.toString())
        heading?.filters = arrayOf(InputFilterMinMax(-360f, 360f))
        heading?.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty()) {
                try {
                    viewModel.waypoints[waypointIndex].heading = text.toString().toInt()
                } catch (nfe: NumberFormatException) {
                    viewModel.waypoints[waypointIndex].heading = 0
                }
            }
        }

        val landSwitch: SwitchMaterial? =
            propertiesView.findViewById(R.id.waypoint_rth_land_switch)
        landSwitch?.isChecked = viewModel.waypoints[waypointIndex].rthLand
        landSwitch?.setOnCheckedChangeListener { _, isChecked ->
            viewModel.waypoints[waypointIndex].rthLand = isChecked
        }

        val headingReset: SwitchMaterial? =
            propertiesView.findViewById(R.id.waypoint_heading_reset_switch)
        headingReset?.isChecked = viewModel.waypoints[waypointIndex].headingReset
        headingReset?.setOnCheckedChangeListener { _, isChecked ->
            viewModel.waypoints[waypointIndex].headingReset = isChecked
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.onDismiss(dialog)
    }

    companion object {
        const val TAG = "WaypointDialogFragment"
    }
}

