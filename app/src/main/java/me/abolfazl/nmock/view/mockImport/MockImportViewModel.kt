package me.abolfazl.nmock.view.mockImport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.SentryLevel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.abolfazl.nmock.repository.locationInfo.LocationInfoRepository
import me.abolfazl.nmock.repository.locationInfo.LocationInfoRepositoryImpl
import me.abolfazl.nmock.repository.mock.MockRepository
import me.abolfazl.nmock.repository.mock.MockRepositoryImpl
import me.abolfazl.nmock.repository.routingInfo.RoutingInfoRepository
import me.abolfazl.nmock.repository.routingInfo.RoutingInfoRepositoryImpl
import me.abolfazl.nmock.utils.locationFormat
import me.abolfazl.nmock.utils.logger.NMockLogger
import me.abolfazl.nmock.utils.response.OneTimeEmitter
import me.abolfazl.nmock.utils.response.SingleEvent
import me.abolfazl.nmock.utils.response.ifNotSuccessful
import me.abolfazl.nmock.utils.response.ifSuccessful
import me.abolfazl.nmock.view.editor.MockEditorActivity
import me.abolfazl.nmock.view.mockImport.fileModels.MockFileModel
import org.neshan.common.model.LatLng
import javax.inject.Inject

@HiltViewModel
class MockImportViewModel @Inject constructor(
    private val mockRepository: MockRepository,
    private val locationInfoRepository: LocationInfoRepository,
    private val routingInfoRepository: RoutingInfoRepository,
    private val logger: NMockLogger
) : ViewModel() {

    companion object {
        const val ACTION_UNKNOWN = "UNKNOWN_EXCEPTION"
        const val ACTION_LOCATION_INFORMATION = "ACTION_LOCATION_INFORMATION"
        const val ACTION_ROUTE_INFORMATION = "ROUTE_INFORMATION_REQUEST"
        const val ACTION_SAVE_MOCK_INFORMATION = "SAVE_MOCK_INFORMATION"
        const val ACTION_GET_MOCK_INFORMATION = "GET_MOCK_INFORMATION"
        const val ACTION_MOCK_SAVED = "MOCK_SAVED!"
    }

    // for handling states
    private val _mockImportState = MutableStateFlow(MockImportState())
    val mockImportState = _mockImportState.asStateFlow()

    // for actions..
    private val _oneTimeEmitter = MutableSharedFlow<OneTimeEmitter>()
    val oneTimeEmitter = _oneTimeEmitter.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logger.writeLog(value = "Exception thrown in MockImportViewModel: ${throwable.message}")
        logger.sendLogsFile(
            fromExceptionHandler = true,
            message = "Exception thrown in MockImportViewModel: ${throwable.message}",
            sentryEventLevel = SentryLevel.ERROR
        )
        viewModelScope.launch {
            _oneTimeEmitter.emit(
                OneTimeEmitter(
                    actionId = ACTION_UNKNOWN,
                    message = actionMapper(0)
                )
            )
        }
    }

    fun loadMockInformationFromFileImport(
        mockFileDataclass: MockFileModel
    ) = viewModelScope.launch {
        val originLocation = LatLng(
            mockFileDataclass.mockFilePositionList[0].latitude,
            mockFileDataclass.mockFilePositionList[0].longitude
        )
        val destinationLocation = LatLng(
            mockFileDataclass.mockFilePositionList[mockFileDataclass.mockFilePositionList.size - 1].latitude,
            mockFileDataclass.mockFilePositionList[mockFileDataclass.mockFilePositionList.size - 1].longitude
        )
        withContext(Dispatchers.Default) {
            getLocationInformation(
                location = originLocation,
                isOrigin = true
            )
            getLocationInformation(
                location = destinationLocation,
                isOrigin = false
            )
        }
        withContext(Dispatchers.Default) { getRouteInformation() }
    }

    private fun getLocationInformation(
        location: LatLng,
        isOrigin: Boolean
    ) = viewModelScope.launch(exceptionHandler) {
        logger.writeLog(
            value = "getLocationInformation function called!" +
                    " isOrigin: $isOrigin," +
                    " latitude: ${location.latitude}," +
                    " longitude: ${location.longitude}"
        )
        locationInfoRepository.getLocationInformation(
            location.latitude,
            location.longitude
        ).collect { response ->
            response.ifSuccessful { result ->
                logger.writeLog(
                    value = "locationInformationReceived!" +
                            "isOrigin: $isOrigin," +
                            "address: ${result.fullAddress}"
                )
                if (isOrigin) {
                    _mockImportState.value = _mockImportState.value.copy(
                        originAddress = SingleEvent(result.fullAddress),
                        originLocation = SingleEvent(location)
                    )
                } else {
                    _mockImportState.value = _mockImportState.value.copy(
                        destinationAddress = SingleEvent(result.fullAddress),
                        destinationLocation = SingleEvent(location)
                    )
                }
            }
            response.ifNotSuccessful { exceptionType ->
                _oneTimeEmitter.emit(
                    OneTimeEmitter(
                        actionId = ACTION_LOCATION_INFORMATION,
                        message = actionMapper(exceptionType)
                    )
                )
            }
        }
    }

    private fun getRouteInformation() = viewModelScope.launch(exceptionHandler) {
        val originLocation = _mockImportState.value.originLocation?.getRawValue()
        val destinationLocation = _mockImportState.value.destinationLocation?.getRawValue()
        if (originLocation == null || destinationLocation == null) {
            logger.writeLog(
                value = "getRouteInformation was failed. origin or destination location was null!"
            )
            _oneTimeEmitter.emit(
                OneTimeEmitter(
                    actionId = ACTION_ROUTE_INFORMATION,
                    message = actionMapper(RoutingInfoRepositoryImpl.UNKNOWN_EXCEPTION)
                )
            )
            return@launch
        }
        routingInfoRepository.getRoutingInformation(
            origin = originLocation.locationFormat(),
            destination = destinationLocation.locationFormat()
        ).collect { response ->
            response.ifSuccessful { result ->
                logger.writeLog(
                    value = "getRouteInformation was successful!"
                )
                _mockImportState.value = _mockImportState.value.copy(
                    lineVector = SingleEvent(result.routeModels[0].getRouteLineVector())
                )
            }
            response.ifNotSuccessful { exceptionType ->
                _oneTimeEmitter.emit(
                    OneTimeEmitter(
                        actionId = ACTION_ROUTE_INFORMATION,
                        message = actionMapper(exceptionType)
                    )
                )
            }
        }
    }

    private fun actionMapper(errorType: Int): Int {
        return 0
    }
}