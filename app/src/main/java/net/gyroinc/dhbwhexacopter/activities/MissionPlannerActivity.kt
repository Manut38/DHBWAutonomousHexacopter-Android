package net.gyroinc.dhbwhexacopter.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
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
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.*
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import net.gyroinc.dhbwhexacopter.*
import net.gyroinc.dhbwhexacopter.fragments.LedControlFragment
import net.gyroinc.dhbwhexacopter.fragments.WaypointListFragment
import net.gyroinc.dhbwhexacopter.fragments.WaypointPropertiesFragment
import net.gyroinc.dhbwhexacopter.models.*
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import kotlin.reflect.KClass

class MissionPlannerActivity : AppCompatActivity(), OnMyLocationClickListener,
    OnMyLocationButtonClickListener, OnMapReadyCallback,
    OnRequestPermissionsResultCallback, LocationListener, OnInfoWindowClickListener,
    OnMapLongClickListener, OnMarkerDragListener {

    private var gpsFix: Boolean = false
    private var permissionDenied = false
    private lateinit var currentLocation: Location
    private lateinit var map: GoogleMap
    private lateinit var locationManager: LocationManager
    var mqttConnection: MQTTConnection = MQTTConnection(this)
    private lateinit var routePolyline: Polyline
    private lateinit var jumpPolylines: ArrayList<Polyline>
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

        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        //Bind View Model
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        //Bind Views
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

        //AppBar
        setSupportActionBar(bottomAppBar)

        //Initialize Map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Button Actions
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

        //Reset LED  Brightness SeekBar
        prefs.edit().putInt("ledControlBrightness", 0).apply()

        //Adjust Error Status Margin
        val params = errorStatusCard.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin =
            getStatusBarHeight() + resources.getDimensionPixelSize(R.dimen.error_status_card_margin_top)
        errorStatusCard.layoutParams = params
    }

    private fun disconnectMQTT() {
        setConnectionState(DISCONNECTED)
        val token = mqttConnection.disconnect()
        token.actionCallback = object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                mqttConnection.mqttClient.unregisterResources()
                showStatusSnackBar("Disconnected!", Snackbar.LENGTH_SHORT)
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                mqttConnection.mqttClient.unregisterResources()
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
        mqttConnection = MQTTConnection(this)
        val token = mqttConnection.connect()
        token.actionCallback =
            object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    setConnectionState(MQTT_CONNECTED)
                    showStatusSnackBar("MQTT Connected!", Snackbar.LENGTH_SHORT)

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

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    showStatusSnackBar(
                        "Failed to connect to MQTT server",
                        Snackbar.LENGTH_SHORT
                    )
                    Log.e(MQTTConnection.TAG, "Failed to connect")
                    Log.e(MQTTConnection.TAG, exception.localizedMessage!!)
                    mqttConnection.mqttClient.unregisterResources()
                }
            }
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

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap ?: return
        map.setOnMyLocationClickListener(this)
        bottomAppBar.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val paddingBottom = bottomAppBar.measuredHeight
        Log.d(TAG, paddingBottom.toString())

        map.setPadding(
            0,
            getStatusBarHeight(),
            0,
            paddingBottom
        )

        map.isBuildingsEnabled = false
        map.isIndoorEnabled = false
        map.mapType = MAP_TYPE_SATELLITE
        map.uiSettings.isIndoorLevelPickerEnabled = false
        map.uiSettings.isCompassEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true
        map.uiSettings.isZoomControlsEnabled = false
        map.uiSettings.isTiltGesturesEnabled = false
        map.uiSettings.isMapToolbarEnabled = false
        map.setOnMapLongClickListener(this)
        map.setOnMarkerDragListener(this)
        map.setOnInfoWindowClickListener(this)
        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    49.02657630563075,
                    8.385215954229373
                ), 15f
            )
        )
        enableMyLocation()

        dronePositionMarker = map.addMarker(
            MarkerOptions().visible(false).position(LatLng(0.0, 0.0)).zIndex(100f)
                .anchor(0.5f, 0.5f)
        )!!
        dronePositionMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_drone_marker))
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private fun enableMyLocation() {
        if (!::map.isInitialized) return
        // [START maps_check_location_permission]
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 0f, this)
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
        // [END maps_check_location_permission]
    }

    override fun onLocationChanged(location: Location) {
        if (!gpsFix) {
            gpsFix = true
            val latLng = LatLng(location.latitude, location.longitude)
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18f)
            map.moveCamera(cameraUpdate)
        }

        currentLocation = location
    }

    override fun onMyLocationButtonClick(): Boolean {
        return false
    }

    override fun onMyLocationClick(location: Location) {
    }

    override fun onMapLongClick(latlng: LatLng) {
        addWaypoint(WaypointTypeNormal::class, latlng)
    }

    private fun <T : Waypoint> addWaypoint(type: KClass<T>, position: LatLng): Waypoint {
        val waypointMarker = map.addMarker(
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
        updatePolylines()
        return waypoint
    }

    fun <T : Waypoint> addWaypoint(type: KClass<T>): Waypoint {
        return if (::currentLocation.isInitialized) {
            addWaypoint(type, LatLng(currentLocation.latitude, currentLocation.longitude))
        } else {
            addWaypoint(type, LatLng(0.0, 0.0))
        }
    }

    fun updatePolylines() {
        drawRoutePolyline()
        drawJumpPolylines()
    }

    private fun drawJumpPolylines() {
        if (!this::jumpPolylines.isInitialized) jumpPolylines = ArrayList()
        var visibleJumpCount = 0
        viewModel.waypoints.forEachIndexed { i, waypoint ->
            if (waypoint is WaypointTypeJump &&
                i >= 2 &&
                viewModel.waypoints.size >= waypoint.jumpTarget &&
                viewModel.waypoints[i - 1].isJumpable() &&
                viewModel.waypoints[waypoint.jumpTarget - 1].isJumpable()
            ) {
                val startPos = viewModel.waypoints[i - 1].marker.position
                val endPos = viewModel.waypoints[waypoint.jumpTarget - 1].marker.position

                if (jumpPolylines.size <= visibleJumpCount) {
                    val line = map.addPolyline(
                        PolylineOptions()
                            .color(getColor(R.color.jump))
                            .pattern(listOf(Dash(20f), Gap(10f)))
                            .add(startPos)
                            .add(endPos)
                    )
                    jumpPolylines.add(line)
                } else {
                    jumpPolylines[visibleJumpCount].points = listOf(startPos, endPos)
                }
                visibleJumpCount += 1
            }
        }

        for (i in visibleJumpCount until jumpPolylines.size) {
            jumpPolylines[i].remove()
            jumpPolylines.removeAt(i)
        }
    }

    private fun drawRoutePolyline() {
        if (!this::routePolyline.isInitialized) routePolyline = map.addPolyline(
            PolylineOptions()
                .color(getColor(R.color.route))
        )
        routePolyline.points = viewModel.waypoints.mapNotNull { waypoint ->
            waypoint.marker.position.takeIf {
                when (waypoint) {
                    is WaypointTypeNormal,
                    is WaypointTypePosholdTime,
                    is WaypointTypePosholdUnlim,
                    is WaypointTypeLand -> true
                    else -> false
                }
            }
        }
    }

    override fun onInfoWindowClick(waypointMarker: Marker) {
        val dialog = WaypointPropertiesFragment().also { dialog ->
            dialog.arguments = Bundle().also { bundle ->
                bundle.putInt(
                    "waypointIndex",
                    waypointMarker.tag as Int
                )
            }
        }
        dialog.show(supportFragmentManager, WaypointPropertiesFragment.TAG)
    }

    override fun onMarkerDragStart(p0: Marker?) {
        updatePolylines()
    }

    override fun onMarkerDrag(p0: Marker?) {
        updatePolylines()
    }

    override fun onMarkerDragEnd(marker: Marker) {
        updatePolylines()
    }

    fun onWaypointRemoved(waypointIndex: Int) {
        viewModel.waypoints[waypointIndex].marker.remove()
        viewModel.waypoints.removeAt(waypointIndex)
        for (i in waypointIndex until viewModel.waypoints.size) {
            viewModel.waypoints[i].setInfoWindowWPNumber(i + 1)
        }
        updatePolylines()
    }

    fun onWaypointsCleared() {
        viewModel.waypoints.forEach {
            it.marker.remove()
        }
        viewModel.waypoints.clear()
        updatePolylines()
    }

    fun onWaypointIndexChanged(prevIndex: Int, newIndex: Int) {
        val waypoint = viewModel.waypoints[prevIndex]
        viewModel.waypoints.removeAt(prevIndex)
        viewModel.waypoints.add(newIndex, waypoint)
        for (i in 0 until viewModel.waypoints.size) {
            viewModel.waypoints[i].setInfoWindowWPNumber(i + 1)
        }
        updatePolylines()
    }

    fun focusOnWaypoint(waypoint: Waypoint) {
        waypoint.marker.showInfoWindow()
        map.animateCamera(CameraUpdateFactory.newLatLng(waypoint.marker.position))
    }

    // [START maps_check_location_permission_result]
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation()
        } else {
            // Permission was denied. Display an error message
            // [START_EXCLUDE]
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true
            // [END_EXCLUDE]
        }
    }

    // [END maps_check_location_permission_result]
    override fun onResumeFragments() {
        super.onResumeFragments()
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            //showMissingPermissionError()
            permissionDenied = false
        }
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
        /**
         * Request code for location permission request.
         *
         * @see .onRequestPermissionsResult
         */
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1

        const val TAG = "MissionPlannerActivity"

        private const val DISCONNECTED = 0
        private const val MQTT_CONNECTED = 1
        private const val DRONE_ONLINE = 2
    }
}
