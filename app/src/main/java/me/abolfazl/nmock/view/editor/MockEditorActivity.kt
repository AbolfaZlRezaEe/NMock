package me.abolfazl.nmock.view.editor

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.carto.core.ScreenPos
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.abolfazl.nmock.R
import me.abolfazl.nmock.databinding.ActivityMockEditorBinding
import me.abolfazl.nmock.utils.Constant
import me.abolfazl.nmock.utils.changeStringTo
import me.abolfazl.nmock.utils.managers.*
import me.abolfazl.nmock.utils.response.OneTimeEmitter
import me.abolfazl.nmock.utils.showSnackBar
import me.abolfazl.nmock.utils.toPixel
import me.abolfazl.nmock.view.dialog.NMockDialog
import me.abolfazl.nmock.view.home.HomeActivity
import me.abolfazl.nmock.view.player.MockPlayerActivity
import me.abolfazl.nmock.view.player.MockPlayerService
import me.abolfazl.nmock.view.saverDialog.SaveMockBottomSheetDialogFragment
import org.neshan.common.model.LatLng
import org.neshan.mapsdk.model.Marker
import org.neshan.mapsdk.model.Polyline

@AndroidEntryPoint
class MockEditorActivity : AppCompatActivity() {

    companion object {
        const val KEY_MOCK_INFORMATION = "MOCK_INFORMATION"

        // error messages
        const val UNKNOWN_ERROR_MESSAGE = R.string.unknownException
        const val LOCATION_INFORMATION_EXCEPTION_MESSAGE = R.string.locationInformationException
        const val ROUTE_INFORMATION_EXCEPTION_MESSAGE = R.string.routeInformationException
        const val MOCK_INFORMATION_IS_WRONG_MESSAGE = R.string.mockProblemException
    }

    private lateinit var binding: ActivityMockEditorBinding
    private val viewModel: MockEditorViewModel by viewModels()

    // Layers
    private val markerLayer = ArrayList<Marker>()
    private val polylineLayer = ArrayList<Polyline>()

    // Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var locationRequest: LocationRequest? = null

    private var mockSaverDialog: SaveMockBottomSheetDialogFragment? = null
    private var fromImplicitIntent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMockEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorSecondary)

        handlingIntent()

        initListeners()

        initObservers()

        initLiveLocation()
    }

    private fun handlingIntent() {
        if (intent == null) return
        showLoadingProgressbar(true)
        // Reading intent data from Extras:
        val mockId = intent.getLongExtra(KEY_MOCK_INFORMATION, -1)
        if (mockId != -1L) {
            viewModel.getMockInformationFromId(mockId)
            binding.deleteMockImageView.visibility = View.VISIBLE
            return
        } else if (intent.data != null) {
            // Reading intent data from URI object:
            var speed: String? = null
            var originLocation: String? = null
            var destinationLocation: String? = null
            intent.data?.let { uri ->
                uri.authority?.let { string ->
                    if (string.contains(UriManager.SHARED_URI_AUTHORITY)) {
                        speed = uri.getQueryParameter(UriManager.SHARED_URI_SPEED_KEY)
                        originLocation = uri.getQueryParameter(UriManager.SHARED_URI_ORIGIN_KEY)
                        destinationLocation =
                            uri.getQueryParameter(UriManager.SHARED_URI_DESTINATION_KEY)
                    }
                }
            }
            if (speed == null) {
                speed = Constant.DEFAULT_SPEED.toString()
            }
            if (originLocation == null || destinationLocation == null) {
                showSnackBar(
                    message = resources.getString(R.string.linkProblemTitle),
                    rootView = binding.root,
                    duration = Snackbar.LENGTH_LONG
                )
                Handler(Looper.getMainLooper()).postDelayed({
                    this.finish()
                }, 3000)
            }
            fromImplicitIntent = true
            viewModel.loadMockWithOriginAndDestinationLocation(
                originLocation = originLocation!!,
                destinationLocation = destinationLocation!!,
                speed = speed!!
            )
        }
    }

    private fun initListeners() {
        binding.mapview.setOnMapLongClickListener(this::onMapLongClicked)

        binding.currentLocationFloatingActionButton.setOnClickListener { onCurrentLocationClicked() }

        binding.backImageView.setOnClickListener { onBackClicked() }

        binding.saveExtendedFab.setOnClickListener { onSaveClicked() }

        binding.undoExtendedFab.setOnClickListener { onUndoClicked() }

        binding.deleteMockImageView.setOnClickListener { onDeleteClicked() }
    }

    private fun initObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mockEditorState.collect { state ->
                    showLoadingProgressbar(false)

                    state.originAddress?.let {
                        it.ifNotHandled { address -> processOriginAddress(address) }
                    }

                    state.destinationAddress?.let {
                        it.ifNotHandled { address -> processDestinationAddress(address) }
                    }

                    state.lineVector?.let {
                        it.ifNotHandled { lineVector -> processLineVector(lineVector) }
                    }

                    state.originLocation?.let {
                        it.ifNotHandled { location -> processMarker(true, location) }
                    }

                    state.destinationLocation?.let {
                        it.ifNotHandled { location -> processMarker(false, location) }
                    }
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.oneTimeEmitter.collect { processAction(it) }
        }
    }

    private fun processOriginAddress(originAddress: String?) {
        binding.originTextView.text =
            originAddress?.changeStringTo(resources.getString(R.string.from))
                ?: resources.getString(R.string.unknownAddress)
        binding.titleTextView.text =
            resources.getText(R.string.chooseDestinationLocation)
        binding.destinationTextView.visibility = View.VISIBLE
        binding.destinationTextView.text =
            resources.getString(R.string.withoutDestinationInformation)
        binding.undoExtendedFab.show()
        binding.undoExtendedFab.postDelayed({
            binding.undoExtendedFab.shrink()
        }, 3000)
    }

    private fun processDestinationAddress(destinationAddress: String?) {
        binding.destinationTextView.visibility = View.VISIBLE
        binding.destinationTextView.text =
            destinationAddress?.changeStringTo(resources.getString(R.string.to))
                ?: resources.getString(R.string.unknownAddress)

        showLoadingProgressbar(true)
        viewModel.getRouteInformation()
    }

    private fun processLineVector(lineVector: ArrayList<List<LatLng>>) {
        LineManager.drawLineOnMap(
            mapView = binding.mapview,
            polylineLayer = polylineLayer,
            vector = lineVector
        )
        binding.titleTextView.text = resources.getString(R.string.youCanSaveNow)
        CameraManager.moveCameraToTripLine(
            mapView = binding.mapview,
            screenPos = ScreenPos(
                binding.root.x + 32.toPixel(this@MockEditorActivity),
                binding.root.y + 32.toPixel(this@MockEditorActivity)
            ),
            origin = viewModel.mockEditorState.value.originLocation?.getRawValue()!!,
            destination = viewModel.mockEditorState.value.destinationLocation?.getRawValue()!!
        )
        binding.saveExtendedFab.show()
        binding.saveExtendedFab.postDelayed({
            binding.saveExtendedFab.shrink()
        }, 5000)
    }

    private fun processAfterMockSaved() {
        mockSaverDialog?.dismiss()
        resetUiStateToDefault()
        if (MockPlayerService.SERVICE_IS_RUNNING) return
        val dialog = NMockDialog.newInstance(
            title = resources.getString(R.string.playingMockDialogTitle),
            actionButtonText = resources.getString(R.string.yes),
            secondaryButtonText = resources.getString(R.string.no)
        )
        dialog.isCancelable = false
        dialog.setDialogListener(
            onActionButtonClicked = {
                startActivity(
                    Intent(
                        this@MockEditorActivity,
                        MockPlayerActivity::class.java
                    ).also {
                        it.putExtra(
                            MockPlayerActivity.KEY_MOCK_ID_PLAYER,
                            viewModel.mockEditorState.value.id?.getRawValue()
                        )
                    })
                dialog.dismiss()
                this@MockEditorActivity.finish()
                viewModel.clearMockInformation(true)
            },
            onSecondaryButtonClicked = {
                dialog.dismiss()
                viewModel.clearMockInformation(true)
            }
        )
        dialog.show(supportFragmentManager.beginTransaction(), null)
    }

    private fun processMarker(
        isOrigin: Boolean,
        location: LatLng
    ) {
        val markerFromMap = MarkerManager.getMarkerFromLayer(
            markerLayer,
            if (isOrigin) MarkerManager.ELEMENT_ID_ORIGIN_MARKER else MarkerManager.ELEMENT_ID_DESTINATION_MARKER
        )
        if (markerFromMap != null) {
            markerFromMap.latLng = location
        } else {
            val marker = MarkerManager.createMarker(
                location = location,
                drawableRes = if (isOrigin) R.drawable.ic_origin_marker
                else R.drawable.ic_destination_marker,
                elementId = if (isOrigin) MarkerManager.ELEMENT_ID_ORIGIN_MARKER
                else MarkerManager.ELEMENT_ID_DESTINATION_MARKER,
                context = this@MockEditorActivity
            )
            marker?.let {
                markerLayer.add(it)
                binding.mapview.addMarker(it)
            }
        }
    }

    private fun processAction(response: OneTimeEmitter) {
        showLoadingProgressbar(false)

        when (response.actionId) {
            MockEditorViewModel.ACTION_MOCK_SAVED -> {
                processAfterMockSaved()
            }
            MockEditorViewModel.ACTION_ROUTE_INFORMATION -> {
                val dialog = NMockDialog.newInstance(
                    title = resources.getString(R.string.wouldYouLikeToTryAgain),
                    actionButtonText = resources.getString(R.string.yes),
                    secondaryButtonText = resources.getString(R.string.no)
                )
                dialog.isCancelable = false
                dialog.setDialogListener(onActionButtonClicked = {
                    showLoadingProgressbar(true)
                    viewModel.getRouteInformation()
                    dialog.dismiss()
                }, onSecondaryButtonClicked = {
                    dialog.dismiss()
                    resetUiStateToDefault()
                    viewModel.clearMockInformation(true)
                })
                dialog.show(supportFragmentManager, null)
            }
            // todo: we should have a capacity to retrying this action!
            MockEditorViewModel.ACTION_SAVE_MOCK_INFORMATION -> {
                // for now:
                mockSaverDialog?.dismiss()
                showSnackBar(
                    message = resources.getString(response.message),
                    rootView = binding.root,
                    duration = Snackbar.LENGTH_SHORT
                )
                resetUiStateToDefault()
                viewModel.clearMockInformation(true)
            }
            MockEditorViewModel.ACTION_GET_MOCK_INFORMATION -> {
                showSnackBar(
                    message = resources.getString(response.message),
                    rootView = binding.root,
                    duration = Snackbar.LENGTH_LONG
                )
                Handler(Looper.getMainLooper()).postDelayed({ this.finish() }, 3000)
            }
            else -> {
                showSnackBar(
                    message = resources.getString(response.message),
                    rootView = binding.root,
                    duration = Snackbar.LENGTH_LONG
                )
            }
        }
    }

    private fun initLiveLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create()?.apply {
            interval = Constant.LOCATION_INTERVAL
            fastestInterval = Constant.LOCATION_FASTEST_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
            }
        }
        requestForLocationUpdate()
    }

    @SuppressLint("MissingPermission")
    private fun requestForLocationUpdate() {
        if (PermissionManager.permissionsIsGranted(this)) {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun onMapLongClicked(latLng: LatLng) {
        val originMarker = MarkerManager.getMarkerFromLayer(
            layer = markerLayer,
            id = MarkerManager.ELEMENT_ID_ORIGIN_MARKER
        )
        val destinationMarker = MarkerManager.getMarkerFromLayer(
            layer = markerLayer,
            id = MarkerManager.ELEMENT_ID_DESTINATION_MARKER
        )
        if (originMarker != null && destinationMarker != null) {
            // we have origin and destination
            showSnackBar(
                message = resources.getString(R.string.originDestinationProblem),
                rootView = binding.root,
                duration = Snackbar.LENGTH_SHORT
            )
            return
        }

        showLoadingProgressbar(true)
        viewModel.getLocationInformation(latLng, originMarker == null)
    }

    @SuppressLint("MissingPermission")
    private fun onCurrentLocationClicked() {
        if (PermissionManager.permissionsIsGranted(this)) {
            if (PermissionManager.locationIsEnabled(this)) {
                fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                    if (location == null) {
                        initLiveLocation()
                        return@addOnSuccessListener
                    }
                    val oldMarker = MarkerManager.getMarkerFromLayer(
                        markerLayer,
                        MarkerManager.ELEMENT_ID_CURRENT_LOCATION_MARKER
                    )
                    val latLngLocation = LatLng(location.latitude, location.longitude)
                    if (oldMarker == null) {
                        val marker = MarkerManager.createMarker(
                            location = latLngLocation,
                            drawableRes = R.drawable.current_location_marker,
                            context = this,
                            elementId = MarkerManager.ELEMENT_ID_CURRENT_LOCATION_MARKER,
                            markerSize = MarkerManager.CURRENT_LOCATION_MARKER_SIZE
                        )
                        marker?.let {
                            markerLayer.add(marker)
                            binding.mapview.addMarker(marker)
                            CameraManager.focusOnUserLocation(
                                mapView = binding.mapview,
                                location = latLngLocation
                            )
                        }
                    } else {
                        oldMarker.latLng = latLngLocation
                        CameraManager.focusOnUserLocation(
                            mapView = binding.mapview,
                            location = latLngLocation
                        )
                    }
                }
            } else {
                showSnackBar(
                    message = resources.getString(R.string.pleaseTurnOnLocation),
                    rootView = binding.root,
                    duration = Snackbar.LENGTH_INDEFINITE,
                    actionText = resources.getString(R.string.iAccept)
                ) {
                    startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            }
        } else {
            val permission = Manifest.permission.ACCESS_FINE_LOCATION
            val shouldShowRelational = ActivityCompat.shouldShowRequestPermissionRationale(
                this, permission
            )
            if (shouldShowRelational) {
                showSnackBar(
                    message = resources.getString(R.string.locationPermissionRational),
                    rootView = binding.root,
                    duration = Snackbar.LENGTH_INDEFINITE,
                    actionText = resources.getString(R.string.iAccept),
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        PermissionManager.getPermissionList().toTypedArray(),
                        Constant.LOCATION_REQUEST
                    )
                }
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    PermissionManager.getPermissionList().toTypedArray(),
                    Constant.LOCATION_REQUEST
                )
            }
        }
    }

    private fun onBackClicked() {
        if (!viewModel.hasMockData()) {
            this.finish()
            return
        }
        val dialog = NMockDialog.newInstance(
            title = resources.getString(R.string.mockEditorLeaveDialogTitle),
            actionButtonText = resources.getString(R.string.yes),
            secondaryButtonText = resources.getString(R.string.cancel)
        )
        dialog.isCancelable = true
        dialog.setDialogListener(
            onActionButtonClicked = {
                if (fromImplicitIntent) {
                    startActivity(Intent(this, HomeActivity::class.java))
                }
                dialog.dismiss()
                this.finish()
            },
            onSecondaryButtonClicked = {
                dialog.dismiss()
            }
        )
        dialog.show(supportFragmentManager.beginTransaction(), null)
    }

    private fun onSaveClicked() {
        if (mockSaverDialog != null && !mockSaverDialog?.isDetached!!) {
            mockSaverDialog?.dismiss()
        }
        val speed =
            if (viewModel.mockEditorState.value.speed != 0) viewModel.mockEditorState.value.speed
            else Constant.DEFAULT_SPEED
        mockSaverDialog = SaveMockBottomSheetDialogFragment.newInstance(
            name = viewModel.mockEditorState.value.name?.getRawValue(),
            description = viewModel.mockEditorState.value.description?.getRawValue(),
            speed = speed.toString()
        )
        mockSaverDialog?.let {
            it.onSaveClickListener { mockName, mockDescription, speed ->
                viewModel.saveMockInformation(
                    name = mockName,
                    description = mockDescription,
                    speed = speed
                )
            }
            supportFragmentManager.beginTransaction().add(it, it.javaClass.name).commit()
        }
    }

    private fun onUndoClicked() {
        val destinationMarker =
            MarkerManager.getMarkerFromLayer(
                markerLayer,
                MarkerManager.ELEMENT_ID_DESTINATION_MARKER
            )
        if (destinationMarker != null) {
            binding.mapview.removeMarker(destinationMarker)
            markerLayer.remove(destinationMarker)
            polylineLayer.forEach { polyline ->
                binding.mapview.removePolyline(polyline)
            }
            polylineLayer.clear()
            binding.saveExtendedFab.hide()
            viewModel.clearMockInformation(false)
            binding.titleTextView.text = resources.getString(R.string.chooseDestinationLocation)
            binding.destinationTextView.visibility = View.GONE
        } else {
            val originMarker =
                MarkerManager.getMarkerFromLayer(
                    markerLayer,
                    MarkerManager.ELEMENT_ID_ORIGIN_MARKER
                )
            originMarker?.let { marker ->
                binding.mapview.removeMarker(marker)
                markerLayer.remove(marker)
                binding.undoExtendedFab.hide()
            }
            binding.destinationTextView.visibility = View.GONE
            binding.titleTextView.text = resources.getString(R.string.chooseOriginLocation)
            binding.originTextView.text = resources.getString(R.string.withoutOriginInformation)
        }
    }

    private fun onDeleteClicked() {
        val dialog = NMockDialog.newInstance(
            title = resources.getString(R.string.deleteDialogTitle),
            actionButtonText = resources.getString(R.string.yes),
            secondaryButtonText = resources.getString(R.string.cancel)
        )
        dialog.isCancelable = false
        dialog.setDialogListener(
            onActionButtonClicked = {
                viewModel.deleteMock()
                resetUiStateToDefault()
                dialog.dismiss()
                this.finish()
            },
            onSecondaryButtonClicked = {
                dialog.dismiss()
            }
        )
        dialog.show(supportFragmentManager.beginTransaction(), null)
    }

    private fun showLoadingProgressbar(
        visibility: Boolean
    ) {
        runOnUiThread {
            binding.titleTextView.visibility = if (!visibility) View.VISIBLE else View.GONE
            binding.loadingProgressbar.visibility = if (visibility) View.VISIBLE else View.GONE
        }
    }

    private fun resetUiStateToDefault() {
        binding.titleTextView.text = resources.getString(R.string.chooseOriginLocation)
        binding.originTextView.text = resources.getString(R.string.withoutOriginInformation)
        binding.destinationTextView.visibility = View.GONE
        binding.saveExtendedFab.hide()
        binding.undoExtendedFab.hide()
        markerLayer.forEach { marker ->
            binding.mapview.removeMarker(marker)
        }
        markerLayer.clear()
        polylineLayer.forEach { polyline ->
            binding.mapview.removePolyline(polyline)
        }
        polylineLayer.clear()
    }

    override fun onResume() {
        super.onResume()
        requestForLocationUpdate()
    }

    override fun onPause() {
        super.onPause()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onBackPressed() {
        onBackClicked()
    }
}