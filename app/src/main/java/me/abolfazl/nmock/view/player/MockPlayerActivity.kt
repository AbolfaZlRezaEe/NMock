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
import io.sentry.Sentry
import io.sentry.SentryLevel
import kotlinx.coroutines.launch
import me.abolfazl.nmock.R
import me.abolfazl.nmock.databinding.ActivityMockPlayerBinding
import me.abolfazl.nmock.repository.models.MockDataClass
import me.abolfazl.nmock.utils.*
import me.abolfazl.nmock.utils.logger.NMockLogger
import me.abolfazl.nmock.utils.managers.*
import me.abolfazl.nmock.utils.response.OneTimeEmitter
import me.abolfazl.nmock.view.detail.MockDetailBottomSheetDialogFragment
import me.abolfazl.nmock.view.dialog.NMockDialog
import me.abolfazl.nmock.view.home.HomeActivity
import me.abolfazl.nmock.view.speedDialog.MockSpeedBottomSheetDialogFragment
import org.neshan.common.model.LatLng
import org.neshan.mapsdk.model.Marker
import org.neshan.mapsdk.model.Polyline
import javax.inject.Inject

@AndroidEntryPoint
class MockPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMockPlayerBinding
    private val viewModel: MockPlayerViewModel by viewModels()

    @Inject
    lateinit var logger: NMockLogger

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

        // error messages
        const val MOCK_INFORMATION_IS_WRONG_MESSAGE = R.string.mockProblemException
        const val UNKNOWN_ERROR_MESSAGE = R.string.unknownException
        const val MOCK_IS_DONE_MESSAGE = R.string.tripCompleted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMockPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        logger.disableLogHeaderForThisClass()
        logger.setClassInformationForEveryLog(javaClass.simpleName)

        if (!isServiceStillRunning(MockPlayerService::class.java)) {
            logger.writeLog(value = "Player service is off! We are going to turn it on.")
            startService(Intent(this, MockPlayerService::class.java))
        }

        Intent(this@MockPlayerActivity, MockPlayerService::class.java).also { intent ->
            logger.writeLog(value = "We are going to bind player service!")
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        initViewsFromBundle()

        initObservers()

        initListeners()
    }

    private fun handlingIntent() {
        val serviceMustReset = intent.getBooleanExtra(RESET_COMMAND, false)
        if (!serviceMustReset) return
        logger.writeLog(value = "We received RESET_COMMAND")
        mockPlayerService?.removeMockProvider()
        mockPlayerService?.resetResources()
    }

    private fun initViewsFromBundle() {
        var mockId = intent.getLongExtra(KEY_MOCK_ID_PLAYER, -1)
        if (mockId == -1L) {
            logger.writeLog(value = "We couldn't find mock id for loading information of that!")
            mockId = SharedManager.getLong(
                sharedPreferences = sharedPreferences,
                key = SHARED_MOCK_ID,
                defaultValue = -1L
            )
            fromNotificationOpened = true
            if (mockId == -1L) {
                logger.writeLog(value = "We don't have mock id even in Shared! :/")
                showSnackBar(
                    message = resources.getString(R.string.mockInformationProblem),
                    rootView = binding.root,
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
            logger.writeLog(value = "Player service connected to Activity!")
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
            logger.writeLog(value = "Player service disconnected from Activity!")
            serviceIsRunning = false
        }
    }

    private fun onLocationChanged(
        mockLocation: Location?,
        oneTimeEmitter: OneTimeEmitter?
    ) {
        mockLocation?.let { processLocation(it) }

        oneTimeEmitter?.let { processAction(it) }
    }

    private fun processLocation(location: Location) {
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

    override fun onStop() {
        super.onStop()
        if (!serviceIsRunning) return
        if (mockPlayerService?.mockIsRunning()!!) {
            logger.writeLog(value = "Service is running and we just unbind from it.")
            unbindService(serviceConnection)
        } else {
            logger.writeLog(value = "Service is idle. we are going to stop it!")
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
        logger.writeLog(value = "Mock information received!")
        showProgressbar(false)
        binding.titleTextView.text = mockInformation.name
        binding.originTextView.text =
            mockInformation.originAddress?.changeStringTo(resources.getString(R.string.from))
                ?: resources.getString(R.string.unknownAddress)
        binding.destinationTextView.text =
            mockInformation.destinationAddress?.changeStringTo(resources.getString(R.string.to))
                ?: resources.getString(R.string.unknownAddress)
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

    private fun processAction(response: OneTimeEmitter) {
        when (response.actionId) {
            MockPlayerViewModel.ACTION_GET_MOCK_INFORMATION -> {
                showSnackBar(
                    message = resources.getString(response.message),
                    rootView = binding.root,
                    duration = Snackbar.LENGTH_LONG
                )
                Handler(Looper.getMainLooper()).postDelayed({ this.finish() }, 3000)
            }
            MockPlayerViewModel.ACTION_UPDATE_MOCK_INFORMATION -> {
                showSnackBar(
                    message = resources.getString(R.string.updateSpeedWasFailed),
                    rootView = binding.root,
                    duration = Snackbar.LENGTH_LONG
                )
            }
            MockPlayerService.ACTION_MOCK_IS_DONE -> {
                showSnackBar(
                    message = resources.getString(response.message),
                    rootView = binding.root,
                    Snackbar.LENGTH_SHORT
                )
                binding.playPauseFloatingActionButton.setImageDrawable(getDrawable(R.drawable.ic_play_24))
                val currentLocationMarker = MarkerManager.getMarkerFromLayer(
                    layer = markerLayer,
                    id = MarkerManager.ELEMENT_ID_CURRENT_LOCATION_MARKER
                )
                currentLocationMarker?.let {
                    markerLayer.remove(it)
                    binding.mapview.removeMarker(it)
                }
            }
            MockPlayerService.ACTION_DEVELOPER_OPTION_PROBLEM -> {
                val dialog = NMockDialog.newInstance(
                    title = resources.getString(R.string.allowApplicationToMock),
                    actionButtonText = resources.getString(R.string.openIt),
                    secondaryButtonText = resources.getString(R.string.closeMockPlayer)
                )
                dialog.isCancelable = false
                dialog.setDialogListener(onActionButtonClicked = {
                    startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
                    dialog.dismiss()
                }, onSecondaryButtonClicked = {
                    dialog.dismiss()
                    this.finish()
                })
                dialog.show(supportFragmentManager, null)
                try {
                    if (mockPlayerService?.mockIsRunning()!!) {
                        logger.writeLog(value = "We are going to stop service because we don't have developer option permission!")
                        mockPlayerService?.pauseOrPlayMock()
                    }
                } catch (exception: Exception) {
                    logger.writeLog(value = "exception thrown while stopping service. exception-> ${exception.message}")
                    Sentry.captureMessage(
                        "we have problem to stopping service in Developer option action",
                        SentryLevel.INFO
                    )
                }
                binding.playPauseFloatingActionButton.setImageDrawable(getDrawable(R.drawable.ic_play_24))
            }
            else -> {
                showSnackBar(
                    message = resources.getString(response.message),
                    rootView = binding.root,
                    Snackbar.LENGTH_SHORT
                )
            }
        }
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
            title = viewModel.mockPlayerState.value.mockInformation?.name!!,
            description = viewModel.mockPlayerState.value.mockInformation?.description!!,
            provider = viewModel.mockPlayerState.value.mockInformation?.provider!!,
            type = viewModel.mockPlayerState.value.mockInformation?.type!!,
            createdAt = viewModel.mockPlayerState.value.mockInformation?.createdAt!!,
            updatedAt = viewModel.mockPlayerState.value.mockInformation?.updatedAt!!
        )
        detailDialog.isCancelable = true
        detailDialog.show(supportFragmentManager.beginTransaction(), null)
    }

    private fun onPlayPauseClicked() {
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

    private fun onStopClicked() {
        if (!MockPlayerService.SERVICE_IS_RUNNING) return
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
            rootView = binding.root,
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
        logger.writeLog(value = "User clicked on Share button!")
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
        logger.writeLog(value = "User clicked on Navigation button!")
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
                logger.writeLog(value = "User stop the mock!")
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