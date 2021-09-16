package net.gyroinc.dhbwhexacopter.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import net.gyroinc.dhbwhexacopter.R
import net.gyroinc.dhbwhexacopter.activities.MissionPlannerActivity
import net.gyroinc.dhbwhexacopter.models.MainViewModel
import net.gyroinc.dhbwhexacopter.models.Waypoint
import net.gyroinc.dhbwhexacopter.utils.InputFilterMinMax

class WaypointPropertiesFragment : BottomSheetDialogFragment(), View.OnClickListener,
    AdapterView.OnItemSelectedListener {

    private val viewModel: MainViewModel by activityViewModels()
    private var waypointIndex: Int = 0
    private lateinit var typeSpinner: Spinner
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
            requireContext(),
            R.array.waypoint_types,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            typeSpinner.adapter = adapter
        }
        typeSpinner.onItemSelectedListener = this

        restoreWaypointType()

        return view
    }

    private fun restoreWaypointType() {
        when (viewModel.waypoints[waypointIndex].getType()) {
            Waypoint.TYPE_WAYPOINT -> {
                typeSpinner.setSelection(getSpinnerIndex(typeSpinner, "WAYPOINT"))
            }
            Waypoint.TYPE_POSHOLD_UNLIM -> {
                typeSpinner.setSelection(getSpinnerIndex(typeSpinner, "POSHOLD_UNLIM"))
            }
            Waypoint.TYPE_POSHOLD_TIME -> {
                typeSpinner.setSelection(getSpinnerIndex(typeSpinner, "POSHOLD_TIME"))
            }
            Waypoint.TYPE_RTH -> {
                typeSpinner.setSelection(getSpinnerIndex(typeSpinner, "RTH"))
            }
            Waypoint.TYPE_SET_POI -> {
                typeSpinner.setSelection(getSpinnerIndex(typeSpinner, "SET_POI"))
            }
            Waypoint.TYPE_JUMP -> {
                typeSpinner.setSelection(getSpinnerIndex(typeSpinner, "JUMP"))
            }
            Waypoint.TYPE_SET_HEAD -> {
                typeSpinner.setSelection(getSpinnerIndex(typeSpinner, "SET_HEAD"))
            }
            Waypoint.TYPE_LAND -> {
                typeSpinner.setSelection(getSpinnerIndex(typeSpinner, "LAND"))
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // TODO: Use the ViewModel
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

    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        propertiesView.removeAllViews()
        when (parent.getItemAtPosition(pos).toString()) {
            "WAYPOINT" -> {
                viewModel.waypoints[waypointIndex].setType(Waypoint.TYPE_WAYPOINT)
                layoutInflater.inflate(R.layout.waypoint_type_wp, propertiesView)
            }
            "POSHOLD_UNLIM" -> {
                viewModel.waypoints[waypointIndex].setType(Waypoint.TYPE_POSHOLD_UNLIM)
                layoutInflater.inflate(R.layout.waypoint_type_poshold_unlim, propertiesView)
            }
            "POSHOLD_TIME" -> {
                viewModel.waypoints[waypointIndex].setType(Waypoint.TYPE_POSHOLD_TIME)
                layoutInflater.inflate(R.layout.waypoint_type_poshold_time, propertiesView)
            }
            "RTH" -> {
                viewModel.waypoints[waypointIndex].setType(Waypoint.TYPE_RTH)
                layoutInflater.inflate(R.layout.waypoint_type_rth, propertiesView)
            }
            "SET_POI" -> {
                viewModel.waypoints[waypointIndex].setType(Waypoint.TYPE_SET_POI)
            }
            "JUMP" -> {
                viewModel.waypoints[waypointIndex].setType(Waypoint.TYPE_JUMP)
                layoutInflater.inflate(R.layout.waypoint_type_jump, propertiesView)
            }
            "SET_HEAD" -> {
                viewModel.waypoints[waypointIndex].setType(Waypoint.TYPE_SET_HEAD)
                layoutInflater.inflate(R.layout.waypoint_type_set_head, propertiesView)
            }
            "LAND" -> {
                viewModel.waypoints[waypointIndex].setType(Waypoint.TYPE_LAND)
                layoutInflater.inflate(R.layout.waypoint_type_land, propertiesView)
            }
        }

        (activity as MissionPlannerActivity).updatePolylines()

        val altitude: EditText? = propertiesView.findViewById(R.id.waypoint_altitude)
        altitude?.filters = arrayOf<InputFilter>(InputFilterMinMax(2f, 30f))
        altitude?.setText(viewModel.waypoints[waypointIndex].altitude.toString())
        altitude?.doOnTextChanged { text, _, _, _ ->
            if (text != null) {
                if (text.isNotEmpty())
                    viewModel.waypoints[waypointIndex].altitude = text.toString().toFloat()
            }
        }

        val speed: EditText? = propertiesView.findViewById(R.id.waypoint_speed)
        speed?.filters = arrayOf<InputFilter>(InputFilterMinMax(0.01f, 2f))
        speed?.setText(viewModel.waypoints[waypointIndex].speed.toString())
        speed?.doOnTextChanged { text, _, _, _ ->
            if (text != null) {
                if (text.isNotEmpty())
                    viewModel.waypoints[waypointIndex].speed = text.toString().toFloat()
            }
        }

        val waitTime: EditText? = propertiesView.findViewById(R.id.waypoint_wait_time)
        waitTime?.setText(viewModel.waypoints[waypointIndex].waitTime.toString())
        waitTime?.doOnTextChanged { text, _, _, _ ->
            if (text != null) {
                if (text.isNotEmpty())
                    viewModel.waypoints[waypointIndex].waitTime = text.toString().toInt()
            }
        }

        val jumpTarget: EditText? = propertiesView.findViewById(R.id.waypoint_jump_target)
        jumpTarget?.filters = arrayOf<InputFilter>(InputFilterMinMax(1f, 99f))
        jumpTarget?.setText(viewModel.waypoints[waypointIndex].jumpTarget.toString())
        jumpTarget?.doOnTextChanged { text, _, _, _ ->
            if (text != null) {
                if (text.isNotEmpty()) {
                    viewModel.waypoints[waypointIndex].jumpTarget = text.toString().toInt()
                    (activity as MissionPlannerActivity).updatePolylines()
                }
            }
        }

        val jumpRepeat: EditText? = propertiesView.findViewById(R.id.waypoint_jump_repeat)
        jumpRepeat?.setText(viewModel.waypoints[waypointIndex].jumpRepeat.toString())
        jumpTarget?.filters = arrayOf<InputFilter>(InputFilterMinMax(-1f, 999f))
        jumpRepeat?.doOnTextChanged { text, _, _, _ ->
            if (text != null) {
                if (text.isNotEmpty())
                    viewModel.waypoints[waypointIndex].jumpRepeat = text.toString().toInt()
            }
        }

        val elevationAdjustment: EditText? =
            propertiesView.findViewById(R.id.waypoint_elevation_adjustment)
        elevationAdjustment?.setText(viewModel.waypoints[waypointIndex].elevationAdjustment.toString())
        elevationAdjustment?.doOnTextChanged { text, _, _, _ ->
            if (text != null) {
                if (text.isNotEmpty())
                    viewModel.waypoints[waypointIndex].elevationAdjustment = text.toString().toInt()
            }
        }

        val heading: EditText? = propertiesView.findViewById(R.id.waypoint_heading)
        heading?.setText(viewModel.waypoints[waypointIndex].heading.toString())
        heading?.doOnTextChanged { text, _, _, _ ->
            if (text != null) {
                if (text.isNotEmpty())
                    viewModel.waypoints[waypointIndex].heading = text.toString().toInt()
            }
        }

        val landSwitch: SwitchCompat? =
            propertiesView.findViewById(R.id.waypoint_rth_land_switch)
        landSwitch?.isChecked = viewModel.waypoints[waypointIndex].rthLand
        landSwitch?.setOnCheckedChangeListener { _, isChecked ->
            viewModel.waypoints[waypointIndex].rthLand = isChecked
        }

        val headingReset: SwitchCompat? =
            propertiesView.findViewById(R.id.waypoint_heading_reset_switch)
        headingReset?.isChecked = viewModel.waypoints[waypointIndex].headingReset
        headingReset?.setOnCheckedChangeListener { _, isChecked ->
            viewModel.waypoints[waypointIndex].headingReset = isChecked
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    private fun getSpinnerIndex(spinner: Spinner, string: String): Int {
        var index = 0
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i) == string) {
                index = i
            }
        }
        return index
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.onDismiss(dialog)
    }

    companion object {
        const val TAG = "WaypointDialogFragment"
    }
}

