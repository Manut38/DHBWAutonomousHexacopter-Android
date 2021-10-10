package net.gyroinc.dhbwhexacopter.map

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.*
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import net.gyroinc.dhbwhexacopter.R
import net.gyroinc.dhbwhexacopter.models.*

class MissionPlannerMap(
    private val activity: FragmentActivity,
    resourceId: Int,
    private val waypoints: List<Waypoint>,
    private val onInfoWindowClickListener: OnInfoWindowClickListener
) : OnMapReadyCallback, OnMarkerDragListener, OnMyLocationClickListener,
    OnMapLongClickListener, LocationListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private var currentLocation: Location? = null
    private var gpsFix: Boolean = false
    private var dronePositionMarker: Marker? = null
    private lateinit var routePolyline: Polyline
    private lateinit var jumpPolylines: ArrayList<Polyline>
    private var permissionDenied = false
    var mapLongClickListener: OnMapLongClickListener? = null
    var mapReadyCallback: OnMapReadyCallback? = null

    init {
        val mapFragment = activity.supportFragmentManager
            .findFragmentById(resourceId) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupMap() {
        googleMap.isBuildingsEnabled = false
        googleMap.isIndoorEnabled = false
        googleMap.mapType = MAP_TYPE_SATELLITE
        googleMap.uiSettings.isIndoorLevelPickerEnabled = false
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true
        googleMap.uiSettings.isZoomControlsEnabled = false
        googleMap.uiSettings.isTiltGesturesEnabled = false
        googleMap.uiSettings.isMapToolbarEnabled = false
        googleMap.setOnMapLongClickListener(this)
        googleMap.setOnMarkerDragListener(this)
        googleMap.setOnInfoWindowClickListener(onInfoWindowClickListener)
        googleMap.setOnMyLocationClickListener(this)
        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    49.02657630563075,
                    8.385215954229373
                ), 15f
            )
        )
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        this.googleMap = googleMap ?: return
        setupMap()
        setupDronePositionMarker()
        enableMyLocation()
        mapReadyCallback?.onMapReady(googleMap)
    }

    private fun setupDronePositionMarker() {
        dronePositionMarker = this.googleMap.addMarker(
            MarkerOptions().visible(false).position(LatLng(0.0, 0.0)).zIndex(100f)
                .anchor(0.5f, 0.5f)
        )
        dronePositionMarker?.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_drone_marker))
    }

    fun updatePolylines() {
        drawRoutePolyline(waypoints)
        drawJumpPolylines(waypoints)
    }

    private fun drawJumpPolylines(waypoints: List<Waypoint>) {
        if (!this::jumpPolylines.isInitialized) jumpPolylines = ArrayList()
        var visibleJumpCount = 0
        waypoints.forEachIndexed { i, waypoint ->
            if (waypoint is WaypointTypeJump &&
                i >= 2 &&
                waypoints.size >= waypoint.jumpTarget &&
                waypoints[i - 1].isJumpable() &&
                waypoints[waypoint.jumpTarget - 1].isJumpable()
            ) {
                val startPos = waypoints[i - 1].marker.position
                val endPos = waypoints[waypoint.jumpTarget - 1].marker.position

                if (jumpPolylines.size <= visibleJumpCount) {
                    val line = googleMap.addPolyline(
                        PolylineOptions()
                            .color(activity.getColor(R.color.jump))
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

    private fun drawRoutePolyline(waypoints: List<Waypoint>) {
        if (!this::routePolyline.isInitialized) routePolyline = googleMap.addPolyline(
            PolylineOptions()
                .color(activity.getColor(R.color.route))
        )
        routePolyline.points = waypoints.mapNotNull { waypoint ->
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

    fun focusOnWaypoint(waypoint: Waypoint) {
        waypoint.marker.showInfoWindow()
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(waypoint.marker.position))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }
        if (ActivityCompat.checkSelfPermission(
                activity,
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

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private fun enableMyLocation() {
        if (!::googleMap.isInitialized) return
        // [START maps_check_location_permission]
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            locationManager =
                activity.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 0f, this)
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            ActivityCompat.requestPermissions(
                activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
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
            googleMap.moveCamera(cameraUpdate)
        }
        currentLocation = location
    }

    fun addMarker(options: MarkerOptions): Marker? {
        return googleMap.addMarker(options)
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

    override fun onMyLocationClick(location: Location) {
    }

    override fun onMapLongClick(latLng: LatLng) {
        mapLongClickListener?.onMapLongClick(latLng)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}

    fun getCurrentLocation(): Location? {
        return currentLocation
    }

    companion object {
        const val TAG = "MissionPlannerMap"

        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
