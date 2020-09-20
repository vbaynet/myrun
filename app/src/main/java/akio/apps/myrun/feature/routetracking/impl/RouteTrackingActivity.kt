package akio.apps.myrun.feature.routetracking.impl

import akio.apps._base.di.BaseInjectionActivity
import akio.apps._base.lifecycle.observe
import akio.apps._base.lifecycle.observeEvent
import akio.apps._base.ui.dp2px
import akio.apps.myrun.R
import akio.apps.myrun.data.routetracking.model.RouteTrackingStatus
import akio.apps.myrun.data.routetracking.model.TrackingLocationEntity
import akio.apps.myrun.data.workout.model.ActivityType
import akio.apps.myrun.databinding.ActivityRouteTrackingBinding
import akio.apps.myrun.feature._base.permissions.AppPermissions.locationPermissions
import akio.apps.myrun.feature._base.permissions.CheckRequiredPermissionsDelegate
import akio.apps.myrun.feature._base.utils.ActivityDialogDelegate
import akio.apps.myrun.feature._base.utils.CheckLocationServiceDelegate
import akio.apps.myrun.feature._base.utils.MapPresentations
import akio.apps.myrun.feature._base.utils.toGmsLatLng
import akio.apps.myrun.feature.myworkout.impl.MyWorkoutActivity
import akio.apps.myrun.feature.routetracking.RouteTrackingViewModel
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.launch

class RouteTrackingActivity : BaseInjectionActivity() {

    private val dialogDelegate by lazy { ActivityDialogDelegate(this) }

    private val viewBinding by lazy { ActivityRouteTrackingBinding.inflate(layoutInflater) }

    private val viewModel: RouteTrackingViewModel by lazy { getViewModel() }

    private lateinit var mapView: GoogleMap

    private val checkLocationServiceDelegate by lazy {
        CheckLocationServiceDelegate(
            this,
            listOf(RouteTrackingService.createLocationTrackingRequest()),
            RC_LOCATION_SERVICE,
            onLocationServiceAvailable
        )
    }

    private val checkLocationPermissionsDelegate by lazy { CheckRequiredPermissionsDelegate(this, RC_LOCATION_PERMISSIONS, locationPermissions, onLocationPermissionsGranted) }

    private var routePolyline: Polyline? = null
    private var drawnLocationCount: Int = 0
    private val trackingRouteLatLngBounds = LatLngBounds.builder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()

        // onCreate: check location permissions -> check location service availability -> allow user to use this screen
        checkLocationPermissionsDelegate.requestPermissions()
    }

    override fun onStart() {
        super.onStart()

        viewModel.resumeDataUpdates()
    }

    override fun onStop() {
        super.onStop()

        viewModel.cancelDataUpdates()
    }

    private fun initObservers() {
        observe(viewModel.isInProgress, dialogDelegate::toggleProgressDialog)
        observe(viewModel.trackingLocationBatch, ::onTrackingLocationUpdate)
        observe(viewModel.trackingStats, viewBinding.trackingStatsView::update)
        observe(viewModel.trackingStatus, ::updateViewForTrackingStatus)
        observeEvent(viewModel.mapInitialLocation) { initLocation ->
            mapView.moveCamera(CameraUpdateFactory.newLatLngZoom(initLocation.toGmsLatLng(), MapPresentations.MAP_DEFAULT_ZOOM_LEVEL))
        }
        observeEvent(viewModel.error, dialogDelegate::showExceptionAlert)
        observeEvent(viewModel.saveWorkoutSuccess) { onSaveWorkoutSuccess() }
    }

    private fun onSaveWorkoutSuccess() {
        startService(RouteTrackingService.stopIntent(this))

        finish()
        startActivity(MyWorkoutActivity.launchIntent(this))
    }

    private fun updateViewForTrackingStatus(@RouteTrackingStatus trackingStatus: Int) {
        when (trackingStatus) {
            RouteTrackingStatus.RESUMED -> updateViewsOnTrackingResumed()
            RouteTrackingStatus.PAUSED -> updateViewsOnTrackingPaused()
            RouteTrackingStatus.STOPPED -> updateViewsOnTrackingStopped()
        }
    }

    private fun onTrackingLocationUpdate(batch: List<TrackingLocationEntity>) {
        drawTrackingLocationUpdate(batch)
        moveMapCameraOnTrackingLocationUpdate(batch)
    }

    private fun moveMapCameraOnTrackingLocationUpdate(batch: List<TrackingLocationEntity>) {
        batch.forEach {
            trackingRouteLatLngBounds.include(it.toGmsLatLng())
        }
        batch.lastOrNull()?.let {
            mapView.moveCamera(CameraUpdateFactory.newLatLngBounds(trackingRouteLatLngBounds.build(), MapPresentations.MAP_LATLNG_BOUND_PADDING))
        }
    }

    private fun drawTrackingLocationUpdate(batch: List<TrackingLocationEntity>) {
        routePolyline?.let { currentPolyline ->
            val appendedPolypoints = currentPolyline.points
            appendedPolypoints.addAll(batch.map { LatLng(it.latitude, it.longitude) })
            currentPolyline.points = appendedPolypoints
        } ?: run {
            val polyline = PolylineOptions()
                .addAll(batch.map { LatLng(it.latitude, it.longitude) })
                .jointType(JointType.ROUND)
                .startCap(RoundCap())
                .endCap(RoundCap())
                .color(ContextCompat.getColor(this, R.color.route_tracking_polyline))
                .width(3.dp2px)
            routePolyline = mapView.addPolyline(polyline)
        }

        drawnLocationCount += batch.size
    }

    private fun initViews() {
        viewBinding.apply {
            setContentView(root)
            recordButton.root.setOnClickListener { onStartRouteTracking() }
            pauseButton.root.setOnClickListener { onPauseRouteTracking() }
            resumeButton.root.setOnClickListener { onResumeRouteTracking() }
            stopButton.root.setOnClickListener { onStopRouteTracking() }
        }
    }

    private fun updateViewsOnTrackingStopped() {
        viewBinding.apply {
            recordButton.root.visibility = View.VISIBLE
            resumeButton.root.visibility = View.GONE
            stopButton.root.visibility = View.GONE
            pauseButton.root.visibility = View.GONE
        }
    }

    private fun updateViewsOnTrackingPaused() {
        viewBinding.apply {
            recordButton.root.visibility = View.GONE
            resumeButton.root.visibility = View.VISIBLE
            stopButton.root.visibility = View.VISIBLE
            pauseButton.root.visibility = View.GONE
        }
    }

    private fun updateViewsOnTrackingResumed() {
        viewBinding.apply {
            recordButton.root.visibility = View.GONE
            resumeButton.root.visibility = View.GONE
            stopButton.root.visibility = View.GONE
            pauseButton.root.visibility = View.VISIBLE
        }
    }

    private fun onStartRouteTracking() {
        startRouteTrackingService()
        viewModel.requestDataUpdates()
    }

    private fun onPauseRouteTracking() {
        startService(RouteTrackingService.pauseIntent(this))
        viewModel.cancelDataUpdates()
    }

    private fun onResumeRouteTracking() {
        startService(RouteTrackingService.resumeIntent(this))
        viewModel.requestDataUpdates()
    }

    private fun onStopRouteTracking() {
        // we are currently paused, do saving:
        AlertDialog.Builder(this)
            .setTitle(R.string.route_tracking_stop_confirmation_title)
            .setPositiveButton(R.string.action_just_do_it) { _, _ ->
                mapView.snapshot { mapSnapshot ->
                    viewModel.saveWorkout(ActivityType.Running, mapSnapshot)
                }
            }
            .setNegativeButton(R.string.action_cancel) {_,_->}
            .show()
    }

    private fun startRouteTrackingService() {
        val serviceIntent = RouteTrackingService.startIntent(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        checkLocationPermissionsDelegate.verifyPermissionsResult()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        checkLocationServiceDelegate.verifyLocationServiceResolutionResult(requestCode, resultCode)
    }

    private fun onLocationRequirementsReady() {
        (supportFragmentManager.findFragmentById(R.id.tracking_map_view) as SupportMapFragment).getMapAsync { map ->
            initMapView(map)
            initObservers()
            viewModel.requestInitialData()
        }
    }

    @SuppressLint("MissingPermission")
    private fun initMapView(map: GoogleMap) {
        this.mapView = map
        map.isMyLocationEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true
    }

    private val onLocationPermissionsGranted = {
        lifecycleScope.launch {
            checkLocationServiceDelegate.checkLocationServiceAvailability(this@RouteTrackingActivity)
        }
    }

    private val onLocationServiceAvailable = { onLocationRequirementsReady() }

    companion object {
        const val RC_LOCATION_SERVICE = 1
        const val RC_LOCATION_PERMISSIONS = 2

        fun launchIntent(context: Context): Intent {
            val intent = Intent(context, RouteTrackingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}