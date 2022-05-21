package me.abolfazl.nmock.view.player

import android.content.*
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.carto.core.ScreenPos
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.abolfazl.nmock.R
import me.abolfazl.nmock.databinding.ActivityMockPlayerBinding
import me.abolfazl.nmock.repository.models.MockDataClass
import me.abolfazl.nmock.utils.*
import me.abolfazl.nmock.utils.managers.*
import me.abolfazl.nmock.utils.response.OneTimeEmitter
import me.abolfazl.nmock.utils.response.exceptions.EXCEPTION_DATABASE_GETTING_ERROR
import me.abolfazl.nmock.utils.response.exceptions.EXCEPTION_INSERTION_ERROR
import me.abolfazl.nmock.view.detail.MockDetailBottomSheetDialogFragment
import me.abolfazl.nmock.view.dialog.NMockDialog
import me.abolfazl.nmock.view.home.HomeActivity
import me.abolfazl.nmock.view.speedDialog.MockSpeedBottomSheetDialogFragment
import org.neshan.common.model.LatLng
import org.neshan.mapsdk.model.Marker
import org.neshan.mapsdk.model.Polyline
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MockPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMockPlayerBinding
    private val viewModel: MockPlayerViewModel by viewModels()

    @Inject
    lateinit var sharedPreferences: SharedPreferences
    private var fromNotificationOpened = false

    //Layers
    private val markerLayer = ArrayList<Marker>()
    private val polylineLayer = ArrayList<Polyline>()

    private var serviceIsRunning = false
    private var mockPlayerService: MockPlayerService? = null

    companion object {
        const val KEY_MOCK_ID_PLAYER = "MOCK_PLAYER_ID"
        const val RESET_COMMAND = "RESET_SERVICE!"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMockPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!isServiceStillRunning(MockPlayerService::class.java)) {
            startService(Intent(this, MockPlayerService::class.java))
        }

        Intent(this@MockPlayerActivity, MockPlayerService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        initViewsFromBundle()

        initObservers()

        initListeners()
    }

    private fun handlingIntent() {
        val serviceMustReset = intent.getBooleanExtra(RESET_COMMAND, false)
        if (!serviceMustReset) return
        mockPlayerService?.removeMockProvider()
        mockPlayerService?.resetResources()
    }

    private fun initViewsFromBundle() {
        var mockId = intent.getLongExtra(KEY_MOCK_ID_PLAYER, -1)
        if (mockId == -1L) {
            mockId = SharedManager.getLong(
                sharedPreferences = sharedPreferences,
                key = SHARED_MOCK_ID,
                defaultValue = -1L
            )
            fromNotificationOpened = true
            if (mockId == -1L) {
                showSnackBar(
                    message = resources.getString(R.string.mockInformationProblem),
                    rootView = findViewById(R.id.mockPlayerRootView),
                    duration = Snackbar.LENGTH_LONG
                )
                Handler(Looper.getMainLooper()).postDelayed({
                    this.finish()
                }, 3000)
                return
            }
        }
        showProgressbar(true)
        viewModel.getMockInformation(mockId)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(component: ComponentName?, binder: IBinder?) {
            serviceIsRunning = true
            val mockPlayerBinder = binder as MockPlayerService.MockPlayerBinder
            mockPlayerService = mockPlayerBinder.getService()

            handlingIntent()

            if (fromNotificationOpened && mockPlayerService?.mockIsRunning()!!) {
                binding.playPauseFloatingActionButton.setImageDrawable(getDrawable(R.drawable.ic_pause_24))
            }

            mockPlayerService?.setLocationChangedListener { mockLocation, oneTimeEmitter ->
                onLocationChanged(mockLocation, oneTimeEmitter)
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            serviceIsRunning = false
        }
    }

    private fun onLocationChanged(
        mockLocation: Location?,
        oneTimeEmitter: OneTimeEmitter<String>?
    ) {
        mockLocation?.let { location ->
            var currentLocationMarker = MarkerManager.getMarkerFromLayer(
                layer = markerLayer,
                id = MarkerManager.ELEMENT_ID_CURRENT_LOCATION_MARKER
            )
            val latLng = LatLng(location.latitude, location.longitude)
            if (currentLocationMarker == null) {
                currentLocationMarker = MarkerManager.createMarker(
                    location = latLng,
                    drawableRes = R.drawable.current_mock_location,
                    context = this@MockPlayerActivity,
                    elementId = MarkerManager.ELEMENT_ID_CURRENT_LOCATION_MARKER,
                )
                currentLocationMarker?.let {
                    markerLayer.add(it)
                    binding.mapview.addMarker(currentLocationMarker)
                }
            } else {
                currentLocationMarker.latLng = latLng
            }
        }
        oneTimeEmitter?.let { exception ->
            exception.exception?.let {
                showSnackBar(
                    message = resources.getString(R.string.unknownException),
                    rootView = findViewById(R.id.mockPlayerRootView),
                    duration = Snackbar.LENGTH_SHORT
                )
                Timber.e(exception.exception)
            }
            exception.message?.let {
                showSnackBar(
                    message = resources.getString(R.string.tripCompleted),
                    rootView = findViewById(R.id.mockPlayerRootView),
                    duration = Snackbar.LENGTH_SHORT
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (!serviceIsRunning) return
        if (mockPlayerService?.mockIsRunning()!!) {
            unbindService(serviceConnection)
        } else {
            mockPlayerService?.stopIdleService()
        }
        serviceIsRunning = false
    }

    private fun initObservers() {
        lifecycleScope.launch {
            viewModel.mockPlayerState.collect { state ->
                state.mockInformation?.let { processMockInformation(it) }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.oneTimeEmitter.collect { processAction(it) }
            }
        }
    }

    private fun processMockInformation(mockInformation: MockDataClass) {
        showProgressbar(false)
        binding.titleTextView.text = mockInformation.mockName
        binding.originTextView.text =
            mockInformation.originAddress.changeStringTo("From:")
        binding.destinationTextView.text =
            mockInformation.destinationAddress.changeStringTo("To:")
        LineManager.drawLineOnMap(
            mapView = binding.mapview,
            polylineLayer = polylineLayer,
            vector = mockInformation.lineVector!!
        )
        val originMarker = MarkerManager.createMarker(
            location = mockInformation.originLocation,
            drawableRes = R.drawable.ic_origin_marker,
            elementId = MarkerManager.ELEMENT_ID_ORIGIN_MARKER,
            context = this@MockPlayerActivity
        )
        val destinationMarker = MarkerManager.createMarker(
            location = mockInformation.destinationLocation,
            drawableRes = R.drawable.ic_destination_marker,
            elementId = MarkerManager.ELEMENT_ID_DESTINATION_MARKER,
            context = this@MockPlayerActivity
        )
        if (originMarker != null && destinationMarker != null) {
            markerLayer.add(originMarker)
            markerLayer.add(destinationMarker)
            binding.mapview.addMarker(originMarker)
            binding.mapview.addMarker(destinationMarker)
        }
        LineManager.drawLineOnMap(
            mapView = binding.mapview,
            polylineLayer = polylineLayer,
            vector = mockInformation.lineVector
        )
        CameraManager.moveCameraToTripLine(
            mapView = binding.mapview,
            screenPos = ScreenPos(
                binding.root.x + 32.toPixel(this@MockPlayerActivity),
                binding.root.y + 32.toPixel(this@MockPlayerActivity)
            ),
            origin = mockInformation.originLocation,
            destination = mockInformation.destinationLocation
        )
        SharedManager.putLong(
            sharedPreferences = sharedPreferences,
            key = SHARED_MOCK_ID,
            value = mockInformation.id!!
        )
    }

    private fun processAction(response: OneTimeEmitter<String>) {
        val message = when (response.exception) {
            EXCEPTION_INSERTION_ERROR -> resources.getString(R.string.databaseInsertionException)
            EXCEPTION_DATABASE_GETTING_ERROR -> resources.getString(R.string.databaseGettingException)
            else -> resources.getString(R.string.unknownException)
        }

        showSnackBar(
            message = message,
            rootView = findViewById(R.id.mockPlayerRootView),
            Snackbar.LENGTH_SHORT
        )
    }

    private fun initListeners() {
        binding.backImageView.setOnClickListener { onBackClicked() }

        binding.detailImageView.setOnClickListener { onDetailClicked() }

        binding.playPauseFloatingActionButton.setOnClickListener { onPlayPauseClicked() }

        binding.stopFloatingActionButton.setOnClickListener { onStopClicked() }

        binding.speedFloatingActionButton.setOnClickListener { onSpeedFabClicked() }

        binding.shareMaterialButton.setOnClickListener { onShareClicked() }

        binding.navigateMaterialButton.setOnClickListener { onNavigationClicked() }
    }

    private fun onBackClicked() {
        if (!mockPlayerService?.mockIsRunning()!!) {
            this.finish()
            return
        }
        showEndDialog()
    }

    private fun onDetailClicked() {
        val detailDialog = MockDetailBottomSheetDialogFragment.newInstance(
            title = viewModel.mockPlayerState.value.mockInformation?.mockName!!,
            description = viewModel.mockPlayerState.value.mockInformation?.mockDescription!!,
            provider = viewModel.mockPlayerState.value.mockInformation?.provider!!,
            type = viewModel.mockPlayerState.value.mockInformation?.mockType!!,
            createdAt = viewModel.mockPlayerState.value.mockInformation?.createdAt!!,
            updatedAt = viewModel.mockPlayerState.value.mockInformation?.updatedAt!!
        )
        detailDialog.isCancelable = true
        detailDialog.show(supportFragmentManager.beginTransaction(), null)
    }

    private fun onPlayPauseClicked() {
        if (handleDeveloperSettingSection()) return
        if (mockPlayerService?.mockIsRunning()!!) {
            mockPlayerService?.pauseOrPlayMock()
            binding.playPauseFloatingActionButton.setImageDrawable(getDrawable(R.drawable.ic_play_24))
        } else {
            binding.playPauseFloatingActionButton.setImageDrawable(getDrawable(R.drawable.ic_pause_24))
            if (mockPlayerService?.shouldReInitialize()!!) {
                mockPlayerService?.setLineVectorForProcessing(
                    viewModel.mockPlayerState.value.mockInformation?.lineVector!![0]
                )
                mockPlayerService?.setMockSpeed(
                    viewModel.mockPlayerState.value.mockInformation?.speed!!
                )
            }
            mockPlayerService?.pauseOrPlayMock()
        }
        mockPlayerService?.initializeMockProvider()
    }

    private fun handleDeveloperSettingSection(): Boolean {
        val mustShowDeveloperOption = SharedManager.getBoolean(
            sharedPreferences = sharedPreferences,
            key = SHARED_MOCK_SETTING,
            defaultValue = true
        )
        if (mustShowDeveloperOption) {
            showSnackBar(
                message = resources.getString(R.string.allowApplicationToMock),
                rootView = findViewById(R.id.mockPlayerRootView),
                duration = Snackbar.LENGTH_LONG,
                actionText = resources.getString(R.string.openIt),
            ) {
                startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
            }
            SharedManager.putBoolean(
                sharedPreferences = sharedPreferences,
                key = SHARED_MOCK_SETTING,
                value = false
            )
            return true
        }
        return false
    }

    private fun onStopClicked() {
        if (!mockPlayerService?.mockIsRunning()!!) return
        mockPlayerService?.pauseOrPlayMock()
        mockPlayerService?.removeMockProvider()
        mockPlayerService?.resetResources()
        val currentLocationMarker = MarkerManager.getMarkerFromLayer(
            layer = markerLayer,
            id = MarkerManager.ELEMENT_ID_CURRENT_LOCATION_MARKER
        )
        currentLocationMarker?.let { marker ->
            markerLayer.remove(marker)
            binding.mapview.removeMarker(marker)
        }
        binding.playPauseFloatingActionButton.setImageDrawable(getDrawable(R.drawable.ic_play_24))
        showSnackBar(
            message = resources.getString(R.string.mockPlayerServiceStoppedCompletely),
            rootView = findViewById(R.id.mockPlayerRootView),
            duration = Snackbar.LENGTH_LONG
        )
    }

    private fun onSpeedFabClicked() {
        val speedDialog = MockSpeedBottomSheetDialogFragment.newInstance(
            viewModel.mockPlayerState.value.mockInformation?.speed!!
        )
        speedDialog.isCancelable = false
        speedDialog.setOnSaveClickListener { newSpeed ->
            mockPlayerService?.setMockSpeed(newSpeed)
            viewModel.changeMockSpeed(newSpeed)
            speedDialog.dismiss()
        }
        speedDialog.show(supportFragmentManager.beginTransaction(), null)
    }

    private fun showProgressbar(show: Boolean) {
        binding.loadingProgressbar.visibility = if (show) View.VISIBLE else View.GONE
        binding.titleTextView.visibility = if (!show) View.VISIBLE else View.GONE
    }

    private fun onShareClicked() {
        val uri = UriManager.createShareUri(
            origin = viewModel.mockPlayerState.value.mockInformation?.originLocation!!,
            destination = viewModel.mockPlayerState.value.mockInformation?.destinationLocation!!,
            speed = viewModel.mockPlayerState.value.mockInformation?.speed!!
        )
        startActivity(
            Intent(Intent.ACTION_SEND, uri).apply {
                putExtra(Intent.EXTRA_TEXT, uri.toString())
                type = "text/plain"
            }
        )
    }

    private fun onNavigationClicked() {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                UriManager.createNavigationUri(viewModel.mockPlayerState.value.mockInformation?.destinationLocation!!)
            )
        )
    }

    private fun showEndDialog() {
        val dialog = NMockDialog.newInstance(
            title = resources.getString(R.string.playerDialogTitle),
            actionButtonText = resources.getString(R.string.stopMockService),
            secondaryButtonText = resources.getString(R.string.justLeave)
        )
        dialog.isCancelable = true
        dialog.setDialogListener(
            onActionButtonClicked = {
                mockPlayerService?.stopIdleService()
                SharedManager.deleteLong(
                    sharedPreferences = sharedPreferences,
                    key = SHARED_MOCK_ID
                )
                dialog.dismiss()
                this.finish()
            },
            onSecondaryButtonClicked = {
                if (fromNotificationOpened) {
                    startActivity(Intent(this, HomeActivity::class.java))
                }
                dialog.dismiss()
                this.finish()
            }
        )
        dialog.show(supportFragmentManager.beginTransaction(), null)
    }

    override fun onBackPressed() {
        if (!mockPlayerService?.mockIsRunning()!!) {
            super.onBackPressed()
            return
        }
        showEndDialog()
    }

}