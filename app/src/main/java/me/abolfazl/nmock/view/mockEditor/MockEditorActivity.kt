package me.abolfazl.nmock.view.mockEditor

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import me.abolfazl.nmock.R
import me.abolfazl.nmock.databinding.ActivityMockEditorBinding
import me.abolfazl.nmock.utils.Constant
import me.abolfazl.nmock.utils.managers.LineManager
import me.abolfazl.nmock.utils.managers.MarkerManager
import me.abolfazl.nmock.utils.managers.PermissionManager
import me.abolfazl.nmock.utils.response.SUCCESS_TYPE_MOCK_INSERTION
import me.abolfazl.nmock.view.save.SaveMockBottomSheetDialogFragment
import me.abolfazl.nmock.view.save.SaveMockCallback
import org.neshan.common.model.LatLng
import org.neshan.mapsdk.model.Marker
import org.neshan.mapsdk.model.Polyline
import timber.log.Timber

@AndroidEntryPoint
class MockEditorActivity : AppCompatActivity() {

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

        initListeners()

        initObservers()

        initLiveLocation()
    }

    private fun initListeners() {
        binding.mapview.setOnMapLongClickListener(this::onMapLongClicked)

        binding.currentLocationFloatingActionButton.setOnClickListener(this::onCurrentLocationClicked)

        binding.closeFloatingActionButton.setOnClickListener { this.finish() }

        binding.saveExtendedFab.setOnClickListener(this::onSaveClicked)
    }

    private fun initObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mockEditorState.collect { state ->

                    state.originAddress?.let { originAddress ->
                        showLoadingProgressbar(false)
                        binding.originTextView.text = locationInformationFormat(originAddress, true)
                        binding.titleTextView.text =
                            resources.getText(R.string.chooseDestinationLocation)
                    }

                    state.destinationAddress?.let { destinationAddress ->
                        showLoadingProgressbar(false)
                        binding.destinationTextView.visibility = View.VISIBLE
                        binding.destinationTextView.text =
                            locationInformationFormat(destinationAddress, false)
                    }

                    state.lineVector?.let { vectors ->
                        showLoadingProgressbar(false)
                        val lineStyle = LineManager.createLineStyle()
                        vectors.forEach { lineVector ->
                            val polyLine = LineManager.createLineFromVectors(lineStyle, lineVector)
                            binding.mapview.addPolyline(polyLine)
                            polylineLayer.add(polyLine)
                        }
                        binding.titleTextView.text = getString(R.string.youCanSaveNow)
                        binding.saveExtendedFab.show()
                        binding.saveExtendedFab.postDelayed({
                            binding.saveExtendedFab.shrink()
                        }, 5000)
                    }
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.oneTimeEmitter.collect { message ->
                showLoadingProgressbar(false)
                Snackbar.make(
                    findViewById(R.id.mockEditorRootView),
                    message,
                    Snackbar.LENGTH_SHORT
                ).show()
                if (message == SUCCESS_TYPE_MOCK_INSERTION) {
                    mockSaverDialog?.dismiss()
                    // todo: showing dialog for going to play activity...
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
                        oldMarker.latLng = LatLng(location.latitude, location.longitude)
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

    private fun onSaveClicked(
        view: View
    ) {
        mockSaverDialog?.dismiss()
        mockSaverDialog = SaveMockBottomSheetDialogFragment.newInstance()
        mockSaverDialog?.let {
            it.setMockCallback(object : SaveMockCallback {
                override fun onClose() {
                    it.dismiss()
                    mockSaverDialog = null
                }

                override fun onSave(
                    mockName: String,
                    mockDescription: String?
                ) {
                    viewModel.saveMockInformation(
                        mockName = mockName,
                        mockDescription = mockDescription ?: "No Description"
                    )
                }
            })
            supportFragmentManager.beginTransaction().add(it, it.javaClass.name).commit()
        }
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

    override fun onResume() {
        super.onResume()
        requestForLocationUpdate()
    }

    override fun onPause() {
        super.onPause()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}