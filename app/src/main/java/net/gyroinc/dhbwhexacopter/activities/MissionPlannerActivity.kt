package net.gyroinc.dhbwhexacopter.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import net.gyroinc.dhbwhexacopter.*
import net.gyroinc.dhbwhexacopter.fragments.LedControlFragment
import net.gyroinc.dhbwhexacopter.fragments.MissionPlannerMap
import net.gyroinc.dhbwhexacopter.fragments.WaypointListFragment
import net.gyroinc.dhbwhexacopter.models.*
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import kotlin.reflect.KClass

class MissionPlannerActivity : AppCompatActivity() {

    lateinit var missionPlannerMap: MissionPlannerMap
    var mqttConnection: MQTTConnection = MQTTConnection(this)
    private lateinit var viewModel: MainViewModel
    private lateinit var fab: FloatingActionButton
    private lateinit var mainLayout: CoordinatorLayout
    private var droneStatus: DroneStatus = DroneStatus(false)
    private lateinit var dronePositionMarker: Marker
    private lateinit var navStatusCard: CardView
    private lateinit var errorStatusCard: CardView
    private lateinit var errorStatusTextView: TextView
    private lateinit var navStatusNavStateTextView: TextView
    private lateinit var navStatusGpsModeTextView: TextView
    private lateinit var navStatusWpNumberTextView: TextView
    private lateinit var gpsStatusSpeedTextView: TextView
    private lateinit var gpsStatusFixTextView: TextView
    private lateinit var gpsStatusNumSatTextView: TextView
    private lateinit var gpsStatusAltitudeTextView: TextView
    private lateinit var bottomAppBar: BottomAppBar
    private lateinit var prefs: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mission_planner)
        initializeSharedPreferences()
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        bindViews()
        setSupportActionBar(bottomAppBar)
        initializeMap()
        adjustMargins()
        setupButtonActions()
        setupMqttCallbacks()
    }

    private fun initializeSharedPreferences() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.edit().putInt("ledControlBrightness", 0).apply()
    }

    private fun adjustMargins() {
        val params = errorStatusCard.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin =
            getStatusBarHeight() + resources.getDimensionPixelSize(R.dimen.error_status_card_margin_top)
        errorStatusCard.layoutParams = params
    }

    private fun setupButtonActions() {
        fab.setOnClickListener {
            if (!mqttConnection.isConnected()) {
                connectMQTT()
            } else {
                if (droneStatus.online) {
                    showStatusSnackBar("Sending Waypoints to Drone...", Snackbar.LENGTH_LONG)
                    mqttConnection.publishWaypoints(viewModel.waypoints)
                } else {
                    disconnectMQTT()
                }
            }
        }
    }

    private fun initializeMap() {
        missionPlannerMap = MissionPlannerMap(this, R.id.map, viewModel.waypoints)
        missionPlannerMap.setOnMapLongClickListener { latLng ->
            addWaypoint(WaypointTypeNormal::class, latLng)
        }
        bottomAppBar.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        missionPlannerMap.setOnMapReadyCallback { googleMap ->
            googleMap.setPadding(
                0,
                getStatusBarHeight(),
                0,
                bottomAppBar.measuredHeight
            )
        }
    }

    private fun bindViews() {
        mainLayout = findViewById(R.id.mainLayout)
        fab = findViewById(R.id.fab)
        navStatusNavStateTextView = findViewById(R.id.nav_status_nav_state)
        navStatusGpsModeTextView = findViewById(R.id.nav_status_gps_mode)
        navStatusWpNumberTextView = findViewById(R.id.nav_status_wp_number)
        gpsStatusSpeedTextView = findViewById(R.id.gps_status_speed)
        gpsStatusNumSatTextView = findViewById(R.id.nav_status_satellites)
        gpsStatusFixTextView = findViewById(R.id.nav_status_gps_fix)
        gpsStatusAltitudeTextView = findViewById(R.id.nav_status_altitude)
        navStatusCard = findViewById(R.id.nav_status_card)
        errorStatusCard = findViewById(R.id.error_status_card)
        bottomAppBar = findViewById(R.id.bottom_app_bar)
        errorStatusTextView = findViewById(R.id.error_status_text)
    }

    private fun disconnectMQTT() {
        setConnectionState(DISCONNECTED)
        val token = mqttConnection.disconnect()
        token.actionCallback = object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                mqttConnection.unregisterResources()
                showStatusSnackBar("Disconnected!", Snackbar.LENGTH_SHORT)
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                mqttConnection.unregisterResources()
            }
        }
        droneStatus = DroneStatus(false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_disconnected, menu)
        setConnectionState(DISCONNECTED)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            WaypointListFragment().also {
                it.show(
                    supportFragmentManager,
                    WaypointListFragment.TAG
                )
            }
            true
        }
        R.id.action_settings -> {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            true
        }
        R.id.action_disconnect -> {
            disconnectMQTT()
            true
        }
        R.id.action_led -> {
            LedControlFragment().also { it.show(supportFragmentManager, LedControlFragment.TAG) }
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun connectMQTT() {
        showStatusSnackBar("Connecting...", Snackbar.LENGTH_SHORT)
        val token = mqttConnection.connect()
        token.actionCallback =
            object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    setConnectionState(MQTT_CONNECTED)
                    showStatusSnackBar("MQTT Connected!", Snackbar.LENGTH_SHORT)
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    showStatusSnackBar(
                        "Failed to connect to MQTT server",
                        Snackbar.LENGTH_SHORT
                    )
                    Log.e(MQTTConnection.TAG, "Failed to connect")
                    Log.e(MQTTConnection.TAG, exception.localizedMessage!!)
                    mqttConnection.unregisterResources()
                }
            }
    }

    private fun setupMqttCallbacks() {
        mqttConnection.setMQTTStatusCallback(object : IMqttStatusCallback {
            override fun onConnectionLost() {
                setConnectionState(DISCONNECTED)
            }
        })

        mqttConnection.setDroneStatusCallback(object : IDroneStatusCallback {
            override fun onUpdateReceived(status: DroneStatus) {
                if (status.online) {
                    setConnectionState(DRONE_ONLINE)
                    showStatusSnackBar("Drone connected!", Snackbar.LENGTH_SHORT)
                } else {
                    setConnectionState(MQTT_CONNECTED)
                    if (droneStatus.online)
                        showStatusSnackBar(
                            "Drone disconnected!",
                            Snackbar.LENGTH_SHORT
                        )
                }
                droneStatus = status
            }
        })

        mqttConnection.setNavStatusCallback(object : INavStatusCallback {
            override fun onUpdateReceived(status: DroneNavStatus) {
                val gpsModeArray = resources.getStringArray(R.array.nav_status_gps_mode)
                navStatusGpsModeTextView.text =
                    if (gpsModeArray.size > status.gps_mode) gpsModeArray[status.gps_mode] else "Invalid"
                val navStateArray =
                    resources.getStringArray(R.array.nav_status_nav_state)
                navStatusNavStateTextView.text =
                    if (navStateArray.size > status.nav_state) navStateArray[status.nav_state] else "Invalid"
                navStatusWpNumberTextView.text = status.wp_number.toString()
                val errorStateArray =
                    resources.getStringArray(R.array.nav_status_error_description)
                errorStatusTextView.text =
                    if (errorStateArray.size > status.nav_error) errorStateArray[status.nav_error] else "Invalid"
            }
        })

        mqttConnection.setGPSCallback(object : IGpsCallback {
            override fun onUpdateReceived(status: DroneGpsStatus) {
                if (::dronePositionMarker.isInitialized) {
                    dronePositionMarker.position = status.getLatLng()
                    if (droneStatus.online) dronePositionMarker.isVisible = true
                }
                val fixArray = resources.getStringArray(R.array.nav_status_gps_fix)
                gpsStatusFixTextView.text =
                    if (fixArray.size > status.fix) fixArray[status.fix] else "Invalid"
                gpsStatusSpeedTextView.text = getString(
                    R.string.gps_status_speed_value,
                    (status.speed.toDouble() / 100)
                )
                gpsStatusNumSatTextView.text = status.numsat.toString()
                gpsStatusAltitudeTextView.text = getString(
                    R.string.gps_status_altitude_value, status.alt
                )
            }
        })
    }

    private fun setConnectionState(state: Int) {
        when (state) {
            DISCONNECTED -> {
                bottomAppBar.setFabAlignmentModeAndReplaceMenu(
                    BottomAppBar.FAB_ALIGNMENT_MODE_CENTER,
                    R.menu.menu_disconnected
                )
                fab.backgroundTintList = getColorStateList(R.color.fab_disconnected)
                fab.setImageResource(R.drawable.ic_outline_power_settings_new_24)
                if (this::dronePositionMarker.isInitialized) dronePositionMarker.isVisible = false
                navStatusCard.isVisible = false
                errorStatusCard.isVisible = false
            }
            MQTT_CONNECTED -> {
                bottomAppBar.setFabAlignmentModeAndReplaceMenu(
                    BottomAppBar.FAB_ALIGNMENT_MODE_CENTER,
                    R.menu.menu_disconnected
                )
                fab.backgroundTintList = getColorStateList(R.color.fab_mqtt_connected)
                fab.setImageResource(R.drawable.ic_outline_power_settings_new_24)
                if (this::dronePositionMarker.isInitialized) dronePositionMarker.isVisible = false
                navStatusCard.isVisible = false
                errorStatusCard.isVisible = false
            }
            DRONE_ONLINE -> {
                bottomAppBar.setFabAlignmentModeAndReplaceMenu(
                    BottomAppBar.FAB_ALIGNMENT_MODE_END,
                    R.menu.menu_drone_online
                )
                fab.backgroundTintList = getColorStateList(R.color.fab_drone_online)
                fab.setImageResource(R.drawable.ic_baseline_send_24)
                navStatusCard.isVisible = true
                errorStatusCard.isVisible = true
            }
        }
    }

    private fun <T : Waypoint> addWaypoint(type: KClass<T>, position: LatLng): Waypoint {
        val waypointMarker = missionPlannerMap.addMarker(
            MarkerOptions()
                .position(position)
                .draggable(true)
        )
        waypointMarker?.showInfoWindow()
        val waypoint: T = Waypoint.getInstanceOf(
            type, viewModel.waypoints.size + 1,
            position,
            waypointMarker!!
        )
        viewModel.waypoints.add(waypoint)
        missionPlannerMap.updatePolylines()
        return waypoint
    }

    fun <T : Waypoint> addWaypoint(type: KClass<T>): Waypoint {
        missionPlannerMap.getCurrentLocation()?.let { location ->
            return addWaypoint(type, LatLng(location.latitude, location.longitude))
        }
        return addWaypoint(type, LatLng(0.0, 0.0))
    }

    fun onWaypointRemoved(waypointIndex: Int) {
        viewModel.waypoints[waypointIndex].marker.remove()
        viewModel.waypoints.removeAt(waypointIndex)
        for (i in waypointIndex until viewModel.waypoints.size) {
            viewModel.waypoints[i].setInfoWindowWPNumber(i + 1)
        }
        missionPlannerMap.updatePolylines()
    }

    fun onWaypointsCleared() {
        viewModel.waypoints.forEach {
            it.marker.remove()
        }
        viewModel.waypoints.clear()
        missionPlannerMap.updatePolylines()
    }

    fun onWaypointIndexChanged(prevIndex: Int, newIndex: Int) {
        val waypoint = viewModel.waypoints[prevIndex]
        viewModel.waypoints.removeAt(prevIndex)
        viewModel.waypoints.add(newIndex, waypoint)
        for (i in 0 until viewModel.waypoints.size) {
            viewModel.waypoints[i].setInfoWindowWPNumber(i + 1)
        }
        missionPlannerMap.updatePolylines()
    }

    fun focusOnWaypoint(waypoint: Waypoint) {
        waypoint.marker.showInfoWindow()
        missionPlannerMap.animateCamera(CameraUpdateFactory.newLatLng(waypoint.marker.position))
    }

    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }

    private fun showStatusSnackBar(message: String, length: Int) {
        val snack: Snackbar = Snackbar.make(mainLayout, message, length)
        snack.show()
    }

    companion object {
        const val TAG = "MissionPlannerActivity"

        private const val DISCONNECTED = 0
        private const val MQTT_CONNECTED = 1
        private const val DRONE_ONLINE = 2
    }
}
