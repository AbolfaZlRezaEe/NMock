package me.abolfazl.nmock.view.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
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
import me.abolfazl.nmock.utils.Constant
import me.abolfazl.nmock.utils.locationFormat
import me.abolfazl.nmock.utils.response.OneTimeEmitter
import me.abolfazl.nmock.utils.response.ifNotSuccessful
import me.abolfazl.nmock.utils.response.ifSuccessful
import org.neshan.common.model.LatLng
import javax.inject.Inject

@HiltViewModel
class MockEditorViewModel @Inject constructor(
    private val locationInfoRepository: LocationInfoRepository,
    private val routingInfoRepository: RoutingInfoRepository,
    private val mockRepository: MockRepository,
) : ViewModel() {

    companion object {
        const val ACTION_UNKNOWN = "UNKNOWN_EXCEPTION"
        const val ACTION_ORIGIN_LOCATION_INFORMATION = "ACTION_ORIGIN_LOCATION_INFORMATION"
        const val ACTION_DESTINATION_LOCATION_INFORMATION =
            "ACTION_DESTINATION_LOCATION_INFORMATION"
        const val ACTION_ROUTE_INFORMATION = "ROUTE_INFORMATION_REQUEST"
        const val ACTION_SAVE_MOCK_INFORMATION = "SAVE_MOCK_INFORMATION"
        const val ACTION_GET_MOCK_INFORMATION = "GET_MOCK_INFORMATION"
    }

    // for handling states
    private val _mockEditorState = MutableStateFlow(MockEditorState())
    val mockEditorState = _mockEditorState.asStateFlow()

    // for errors..
    private val _oneTimeEmitter = MutableSharedFlow<OneTimeEmitter>()
    val oneTimeEmitter = _oneTimeEmitter.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Sentry.captureMessage("Exception thrown in MockEditorViewModel: " + throwable.message)
        viewModelScope.launch {
            _oneTimeEmitter.emit(
                OneTimeEmitter(
                    actionId = ACTION_UNKNOWN,
                    message = errorMapper(0)
                )
            )
        }
    }

    fun getLocationInformation(
        location: LatLng,
        isOrigin: Boolean
    ) = viewModelScope.launch(exceptionHandler) {
        locationInfoRepository.getLocationInformation(
            location.latitude,
            location.longitude
        ).collect { response ->
            response.ifSuccessful { result ->
                if (isOrigin) {
                    _mockEditorState.value = _mockEditorState.value.copy(
                        originAddress = result.fullAddress,
                        originLocation = location
                    )
                } else {
                    _mockEditorState.value = _mockEditorState.value.copy(
                        destinationAddress = result.fullAddress,
                        destinationLocation = location
                    )
                }
            }
            response.ifNotSuccessful { exceptionType ->
                _oneTimeEmitter.emit(
                    OneTimeEmitter(
                        actionId =
                        if (isOrigin) ACTION_ORIGIN_LOCATION_INFORMATION
                        else ACTION_DESTINATION_LOCATION_INFORMATION,
                        message = errorMapper(exceptionType)
                    )
                )
            }
        }
    }

    fun getRouteInformation() = viewModelScope.launch(exceptionHandler) {
        val originLocation = _mockEditorState.value.originLocation
        val destinationLocation = _mockEditorState.value.destinationLocation
        if (originLocation == null || destinationLocation == null) {
            _oneTimeEmitter.emit(
                OneTimeEmitter(
                    actionId = ACTION_ROUTE_INFORMATION,
                    message = errorMapper(RoutingInfoRepositoryImpl.UNKNOWN_EXCEPTION)
                )
            )
            return@launch
        }
        routingInfoRepository.getRoutingInformation(
            origin = originLocation.locationFormat(),
            destination = destinationLocation.locationFormat()
        ).collect { response ->
            response.ifSuccessful { result ->
                _mockEditorState.value = _mockEditorState.value.copy(
                    lineVector = result.routeModels[0].getRouteLineVector()
                )
            }
            response.ifNotSuccessful { exceptionType ->
                _oneTimeEmitter.emit(
                    OneTimeEmitter(
                        actionId = ACTION_ROUTE_INFORMATION,
                        message = errorMapper(exceptionType)
                    )
                )
            }
        }
    }

    fun saveMockInformation(
        name: String,
        description: String,
        originLocation: LatLng,
        destinationLocation: LatLng,
        speed: Int
    ) = viewModelScope.launch(exceptionHandler) {
        mockRepository.saveMockInformation(
            name = name,
            description = description,
            type = Constant.TYPE_CUSTOM_CREATE,
            originLocation = originLocation,
            destinationLocation = destinationLocation,
            originAddress = _mockEditorState.value.originAddress,
            destinationAddress = _mockEditorState.value.destinationAddress,
            speed = speed,
            lineVector = mockEditorState.value.lineVector,
            bearing = 0f,
            accuracy = 1F,
            provider = Constant.PROVIDER_GPS
        ).collect { response ->
            response.ifSuccessful { mockId ->
                _mockEditorState.value = _mockEditorState.value.copy(
                    mockId = mockId
                )
            }
            response.ifNotSuccessful { exceptionType ->
                _oneTimeEmitter.emit(
                    OneTimeEmitter(
                        actionId = ACTION_SAVE_MOCK_INFORMATION,
                        message = errorMapper(exceptionType)
                    )
                )
            }
        }
    }

    fun clearTripInformation(
        clearOrigin: Boolean
    ) {
        if (clearOrigin) {
            _mockEditorState.value = _mockEditorState.value.copy(
                destinationAddress = null,
                originAddress = null,
                lineVector = null,
                originLocation = null,
                destinationLocation = null,
                speed = null,
                mockId = null,
                mockInformation = null
            )
        } else {
            _mockEditorState.value = _mockEditorState.value.copy(
                destinationAddress = null,
                lineVector = null,
                destinationLocation = null
            )
        }
    }

    fun getMockInformationFromId(
        id: Long
    ) = viewModelScope.launch(exceptionHandler) {
        mockRepository.getMock(id).collect { response ->
            response.ifSuccessful { mockData ->
                _mockEditorState.value = _mockEditorState.value.copy(
                    mockInformation = mockData
                )
            }
            response.ifNotSuccessful { exceptionType ->
                _oneTimeEmitter.emit(
                    OneTimeEmitter(
                        actionId = ACTION_GET_MOCK_INFORMATION,
                        message = errorMapper(exceptionType)
                    )
                )
            }
        }
    }

    fun deleteMock() = viewModelScope.launch(exceptionHandler) {
        mockRepository.deleteMock(_mockEditorState.value.mockInformation!!)
        _mockEditorState.value = _mockEditorState.value.copy(
            mockInformation = null
        )
    }

    fun loadMockWithOriginAndDestinationLocation(
        originLocation: String,
        destinationLocation: String,
        speed: String
    ) = viewModelScope.launch {
        val realOrigin = originLocation.locationFormat()
        val realDestination = destinationLocation.locationFormat()
        val realSpeed = speed.toInt()
        _mockEditorState.value = _mockEditorState.value.copy(
            speed = realSpeed,
            originLocation = realOrigin,
            destinationLocation = realDestination
        )
        withContext(Dispatchers.Default) { getLocationInformation(realOrigin, true) }
        withContext(Dispatchers.Default) { getLocationInformation(realDestination, false) }
        withContext(Dispatchers.Default) { getRouteInformation() }
    }

    fun hasMockData(): Boolean {
        if (_mockEditorState.value.lineVector == null) {
            return _mockEditorState.value.mockInformation != null
        }
        return true
    }

    private fun errorMapper(errorType: Int): Int {
        return when (errorType) {
            LocationInfoRepositoryImpl.UNKNOWN_EXCEPTION -> MockEditorActivity.LOCATION_INFORMATION_EXCEPTION_MESSAGE
            RoutingInfoRepositoryImpl.UNKNOWN_EXCEPTION -> MockEditorActivity.ROUTE_INFORMATION_EXCEPTION_MESSAGE
            MockRepositoryImpl.LINE_VECTOR_NULL_EXCEPTION,
            MockRepositoryImpl.DATABASE_EMPTY_LINE_EXCEPTION,
            MockRepositoryImpl.DATABASE_INSERTION_EXCEPTION -> MockEditorActivity.MOCK_INFORMATION_IS_WRONG_MESSAGE
            else -> MockEditorActivity.UNKNOWN_ERROR_MESSAGE
        }
    }
}