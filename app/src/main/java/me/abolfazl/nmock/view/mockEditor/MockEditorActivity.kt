package me.abolfazl.nmock.view.mockEditor

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.carto.core.ScreenBounds
import com.carto.core.ScreenPos
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.abolfazl.nmock.R
import me.abolfazl.nmock.databinding.ActivityMockEditorBinding
import me.abolfazl.nmock.utils.Constant
import me.abolfazl.nmock.utils.managers.LineManager
import me.abolfazl.nmock.utils.managers.MarkerManager
import me.abolfazl.nmock.utils.managers.PermissionManager
import me.abolfazl.nmock.utils.response.SUCCESS_TYPE_MOCK_INSERTION
import me.abolfazl.nmock.view.dialog.NMockDialog
import me.abolfazl.nmock.view.save.SaveMockBottomSheetDialogFragment
import org.neshan.common.model.LatLng
import org.neshan.common.model.LatLngBounds
import org.neshan.mapsdk.model.Marker
import org.neshan.mapsdk.model.Polyline
import kotlin.math.max
import kotlin.math.min

@AndroidEntryPoint
class MockEditorActivity : AppCompatActivity() {

    companion object {
        const val KEY_MOCK_INFORMATION = "MOCK_INFORMATION"
    }

    private lateinit var binding: ActivityMockEditorBinding
    private val viewModel: MockEditorViewModel by viewModels()

    private val markerLayer = ArrayList<Marker>()
    private val polylineLayer = ArrayList<Polyline>()

    // Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var locationRequest: LocationRequest? = null

    private var mockSaverDialog: SaveMockBottomSheetDialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMockEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewFromArguments()

        initListeners()

        initObservers()

        initLiveLocation()
    }

    private fun initViewFromArguments() {
        val mockId = intent.getLongExtra(KEY_MOCK_INFORMATION, -1)
        if (mockId == -1L) return
        showLoadingProgressbar(true)
        viewModel.getMockFromId(mockId)
    }

    private fun initListeners() {
        binding.mapview.setOnMapLongClickListener(this::onMapLongClicked)

        binding.currentLocationFloatingActionButton.setOnClickListener(this::onCurrentLocationClicked)

        binding.backImageView.setOnClickListener { this.finish() }

        binding.saveExtendedFab.setOnClickListener(this::onSaveClicked)

        binding.undoExtendedFab.setOnClickListener(this::onUndoClicked)

        binding.deleteMockImageView.setOnClickListener(this::onDeleteClicked)
    }

    private fun initObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mockEditorState.collect { state ->
                    showLoadingProgressbar(false)

                    state.originAddress?.let { originAddress ->
                        binding.originTextView.text = locationInformationFormat(originAddress, true)
                        binding.titleTextView.text =
                            resources.getText(R.string.chooseDestinationLocation)
                        binding.destinationTextView.visibility = View.VISIBLE
                        binding.destinationTextView.text =
                            getString(R.string.withoutDestinationInformation)
                        binding.undoExtendedFab.show()
                        binding.undoExtendedFab.postDelayed({
                            binding.undoExtendedFab.shrink()
                        }, 3000)
                    }

                    state.destinationAddress?.let { destinationAddress ->
                        binding.destinationTextView.visibility = View.VISIBLE
                        binding.destinationTextView.text =
                            locationInformationFormat(destinationAddress, false)
                    }

                    state.lineVector?.let { vectors ->
                        drawLine(vectors)
                        binding.titleTextView.text = getString(R.string.youCanSaveNow)
                        moveCameraToTripLine(
                            origin = getMarkerFromLayer(
                                markerLayer,
                                MarkerManager.ELEMENT_ID_ORIGIN_MARKER
                            )?.latLng!!,
                            destination = getMarkerFromLayer(
                                markerLayer,
                                MarkerManager.ELEMENT_ID_DESTINATION_MARKER
                            )?.latLng!!
                        )
                        binding.saveExtendedFab.show()
                        binding.saveExtendedFab.postDelayed({
                            binding.saveExtendedFab.shrink()
                        }, 5000)
                    }

                    /*
                        this section used for situation when we came into this fragment
                        with mockId and we want to load all of information about that mock.
                        we find the Mock with id and after, we show all the information
                        that we want on Map.
                     */
                    state.mockInformation?.let { mockInformation ->
                        binding.titleTextView.text = getString(R.string.youCanEditYourTrip)
                        binding.destinationTextView.visibility = View.VISIBLE
                        binding.saveExtendedFab.visibility = View.VISIBLE
                        binding.undoExtendedFab.visibility = View.VISIBLE
                        binding.deleteMockImageView.visibility = View.VISIBLE
                        binding.originTextView.text =
                            locationInformationFormat(mockInformation.originAddress, true)
                        binding.destinationTextView.text =
                            locationInformationFormat(mockInformation.destinationAddress, false)
                        val originMarker = MarkerManager.createMarker(
                            location = mockInformation.originLocation,
                            drawableRes = R.drawable.marker_origin,
                            elementId = MarkerManager.ELEMENT_ID_ORIGIN_MARKER,
                            context = this@MockEditorActivity
                        )
                        val destinationMarker = MarkerManager.createMarker(
                            location = mockInformation.destinationLocation,
                            drawableRes = R.drawable.marker_destination,
                            elementId = MarkerManager.ELEMENT_ID_DESTINATION_MARKER,
                            context = this@MockEditorActivity
                        )
                        if (originMarker != null && destinationMarker != null) {
                            markerLayer.add(originMarker)
                            markerLayer.add(destinationMarker)
                            binding.mapview.addMarker(originMarker)
                            binding.mapview.addMarker(destinationMarker)
                        }
                        drawLine(mockInformation.lineVector!!)
                        moveCameraToTripLine(
                            origin = mockInformation.originLocation,
                            destination = mockInformation.destinationLocation
                        )
                    }
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.oneTimeEmitter.collect { message ->
                showLoadingProgressbar(false)
                Snackbar.make(
                    findViewById(R.id.mockEditorRootView),
                    "Message: $message",
                    Snackbar.LENGTH_SHORT
                ).show()
                if (message == SUCCESS_TYPE_MOCK_INSERTION) {
                    mockSaverDialog?.dismiss()
                    resetUiStateToDefault()
                    viewModel.clearTripInformation(true)

                    val dialog = NMockDialog.newInstance(
                        title = getString(R.string.playingMockDialogTitle),
                        actionButtonText = getString(R.string.yes),
                        secondaryButtonText = getString(R.string.no)
                    )
                    dialog.isCancelable = true
                    dialog.setDialogListener(
                        onActionButtonClicked = {
                            // todo: start activity to mock player...
                        },
                        onSecondaryButtonClicked = {
                            dialog.dismiss()
                        }
                    )
                    dialog.show(supportFragmentManager.beginTransaction(), null)
                }
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
        if (markerLayer.size == 3) {
            // we have origin and destination
            Snackbar.make(
                findViewById(R.id.mockEditorRootView),
                getString(R.string.originDestinationProblem),
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        val originIsNull = getMarkerFromLayer(
            layer = markerLayer,
            id = MarkerManager.ELEMENT_ID_ORIGIN_MARKER
        ) == null

        val marker = MarkerManager.createMarker(
            location = latLng,
            drawableRes = if (originIsNull) R.drawable.marker_origin else R.drawable.marker_destination,
            elementId = if (originIsNull) MarkerManager.ELEMENT_ID_ORIGIN_MARKER
            else MarkerManager.ELEMENT_ID_DESTINATION_MARKER,
            context = this
        )
        marker?.let {
            markerLayer.add(marker)
            binding.mapview.addMarker(it)
        }

        showLoadingProgressbar(true)
        viewModel.getLocationInformation(latLng, originIsNull)

        if (!originIsNull) {
            showLoadingProgressbar(true)
            viewModel.getRouteInformation(
                originLocation = getMarkerFromLayer(
                    markerLayer,
                    MarkerManager.ELEMENT_ID_ORIGIN_MARKER
                )!!.latLng,
                destinationLocation = latLng
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun onCurrentLocationClicked(view: View) {
        if (PermissionManager.permissionsIsGranted(this)) {
            if (PermissionManager.locationIsEnabled(this)) {
                fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                    if (location == null) {
                        initLiveLocation()
                        return@addOnSuccessListener
                    }
                    val oldMarker = getMarkerFromLayer(
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
                            focusOnUserLocation(latLngLocation)
                        }
                    } else {
                        oldMarker.latLng = latLngLocation
                        focusOnUserLocation(latLngLocation)
                    }
                }
            } else {
                Snackbar.make(
                    findViewById(R.id.mockEditorRootView),
                    getString(R.string.pleaseTurnOnLocation),
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(getString(R.string.iAccept)) {
                    startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }.show()
            }
        } else {
            val permission = Manifest.permission.ACCESS_FINE_LOCATION
            val shouldShowRelational = ActivityCompat.shouldShowRequestPermissionRationale(
                this, permission
            )
            if (shouldShowRelational) {
                Snackbar.make(
                    findViewById(R.id.mockEditorRootView),
                    getString(R.string.locationPermissionRational),
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(getString(R.string.iAccept)) {
                    ActivityCompat.requestPermissions(
                        this,
                        PermissionManager.getPermissionList().toTypedArray(),
                        Constant.LOCATION_REQUEST
                    )
                }.show()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    PermissionManager.getPermissionList().toTypedArray(),
                    Constant.LOCATION_REQUEST
                )
            }
        }
    }

    private fun onSaveClicked(view: View) {
        if (mockSaverDialog != null && !mockSaverDialog?.isDetached!!) {
            mockSaverDialog?.dismiss()
        }
        mockSaverDialog = SaveMockBottomSheetDialogFragment.newInstance(
            name = viewModel.mockEditorState.value.mockInformation?.mockName,
            description = viewModel.mockEditorState.value.mockInformation?.mockDescription,
            speed = viewModel.mockEditorState.value.mockInformation?.speed?.toString()
        )
        mockSaverDialog?.let {
            it.onSaveClickListener { mockName, mockDescription, speed ->
                viewModel.saveMockInformation(
                    mockName = mockName,
                    mockDescription = mockDescription ?: "No Description",
                    originLocation = getMarkerFromLayer(
                        markerLayer,
                        MarkerManager.ELEMENT_ID_ORIGIN_MARKER
                    )!!.latLng,
                    destinationLocation = getMarkerFromLayer(
                        markerLayer,
                        MarkerManager.ELEMENT_ID_DESTINATION_MARKER
                    )!!.latLng,
                    speed = speed
                )
            }
            supportFragmentManager.beginTransaction().add(it, it.javaClass.name).commit()
        }
    }

    private fun onUndoClicked(view: View) {
        val destinationMarker =
            getMarkerFromLayer(markerLayer, MarkerManager.ELEMENT_ID_DESTINATION_MARKER)
        if (destinationMarker != null) {
            binding.mapview.removeMarker(destinationMarker)
            markerLayer.remove(destinationMarker)
            polylineLayer.forEach { polyline ->
                binding.mapview.removePolyline(polyline)
            }
            polylineLayer.clear()
            binding.saveExtendedFab.hide()
            viewModel.clearTripInformation(false)
            binding.titleTextView.text = getString(R.string.chooseDestinationLocation)
            binding.destinationTextView.visibility = View.GONE
        } else {
            val originMarker =
                getMarkerFromLayer(markerLayer, MarkerManager.ELEMENT_ID_ORIGIN_MARKER)
            originMarker?.let { marker ->
                binding.mapview.removeMarker(marker)
                markerLayer.remove(marker)
                binding.undoExtendedFab.hide()
            }
            binding.destinationTextView.visibility = View.GONE
            binding.titleTextView.text = getString(R.string.chooseOriginLocation)
            binding.originTextView.text = getString(R.string.withoutOriginInformation)
        }
    }

    private fun onDeleteClicked(view: View) {
        val dialog = NMockDialog.newInstance(
            title = getString(R.string.deleteDialogTitle),
            actionButtonText = getString(R.string.yes),
            secondaryButtonText = getString(R.string.cancel)
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

    private fun getMarkerFromLayer(
        layer: ArrayList<Marker>,
        id: String
    ): Marker? {
        layer.forEach { marker ->
            val hasId = marker.hasMetadata(MarkerManager.ID_ELEMENT_META_DATA)
            if (hasId && marker.getMetadata(MarkerManager.ID_ELEMENT_META_DATA) == id)
                return marker
        }
        return null
    }

    private fun locationInformationFormat(
        address: String,
        isOrigin: Boolean
    ): String {
        val suffix = if (isOrigin) "From:" else "To:"
        return "$suffix $address"
    }

    private fun showLoadingProgressbar(
        visibility: Boolean
    ) {
        runOnUiThread {
            binding.titleTextView.visibility = if (!visibility) View.VISIBLE else View.GONE
            binding.loadingProgressbar.visibility = if (visibility) View.VISIBLE else View.GONE
        }
    }

    private fun focusOnUserLocation(
        location: LatLng,
        zoom: Float = 15F
    ) {
        binding.mapview.moveCamera(location, 0F)
        binding.mapview.setZoom(zoom, 0.15F)
    }

    private fun resetUiStateToDefault() {
        binding.titleTextView.text = getString(R.string.chooseOriginLocation)
        binding.originTextView.text = getString(R.string.withoutOriginInformation)
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

    private fun moveCameraToTripLine(
        origin: LatLng,
        destination: LatLng,
    ) {
        val minX = min(origin.latitude, destination.latitude)
        val maxX = max(origin.latitude, destination.latitude)
        val minY = min(origin.longitude, destination.longitude)
        val maxY = max(origin.longitude, destination.longitude)

        val latLngBounds = LatLngBounds(LatLng(minX, minY), LatLng(maxX, maxY))
        val screenBounds = ScreenBounds(
            ScreenPos(binding.root.x, binding.root.y),
            ScreenPos(binding.mapview.width.toFloat(), binding.mapview.height.toFloat())
        )

        binding.mapview.moveToCameraBounds(latLngBounds, screenBounds, true, 0.5f)
    }

    private fun drawLine(
        vector: ArrayList<List<LatLng>>
    ) {
        val lineStyle = LineManager.createLineStyle()
        vector.forEach { lineVector ->
            val polyLine = LineManager.createLineFromVectors(lineStyle, lineVector)
            binding.mapview.addPolyline(polyLine)
            polylineLayer.add(polyLine)
        }
    }

    override fun onResume() {
        super.onResume()
        requestForLocationUpdate()
    }

    override fun onPause() {
        super.onPause()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}