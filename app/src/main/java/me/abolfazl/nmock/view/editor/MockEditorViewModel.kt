package me.abolfazl.nmock.view.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.abolfazl.nmock.repository.locationInfo.LocationInfoRepository
import me.abolfazl.nmock.repository.mock.MockRepository
import me.abolfazl.nmock.repository.models.MockDataClass
import me.abolfazl.nmock.repository.routingInfo.RoutingInfoRepository
import me.abolfazl.nmock.utils.Constant
import me.abolfazl.nmock.utils.locationFormat
import me.abolfazl.nmock.utils.response.OneTimeEmitter
import me.abolfazl.nmock.utils.response.exceptions.EXCEPTION_INSERTION_ERROR
import me.abolfazl.nmock.utils.response.exceptions.EXCEPTION_UNKNOWN
import me.abolfazl.nmock.utils.response.ifNotSuccessful
import me.abolfazl.nmock.utils.response.ifSuccessful
import org.neshan.common.model.LatLng
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MockEditorViewModel @Inject constructor(
    private val locationInfoRepository: LocationInfoRepository,
    private val routingInfoRepository: RoutingInfoRepository,
    private val mockRepository: MockRepository,
) : ViewModel() {

    // for handling states
    private val _mockEditorState = MutableStateFlow(MockEditorState())
    val mockEditorState = _mockEditorState.asStateFlow()

    // for errors..
    private val _oneTimeEmitter = MutableSharedFlow<OneTimeEmitter<String>>()
    val oneTimeEmitter = _oneTimeEmitter.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e("Exception thrown in MockEditorViewModel: " + throwable.message)
        viewModelScope.launch {
            _oneTimeEmitter.emit(OneTimeEmitter(exception = EXCEPTION_UNKNOWN))
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
                        originAddress = result.fullAddress
                    )
                } else {
                    _mockEditorState.value = _mockEditorState.value.copy(
                        destinationAddress = result.fullAddress
                    )
                }
            }
            response.ifNotSuccessful { exception ->
                _oneTimeEmitter.emit(OneTimeEmitter(exception = exception.type))
                Timber.e(exception.type)
            }
        }
    }

    fun getRouteInformation(
        originLocation: LatLng,
        destinationLocation: LatLng
    ) = viewModelScope.launch(exceptionHandler) {
        routingInfoRepository.getRoutingInformation(
            origin = originLocation.locationFormat(),
            destination = destinationLocation.locationFormat()
        ).collect { response ->
            response.ifSuccessful { result ->
                _mockEditorState.value = _mockEditorState.value.copy(
                    lineVector = result.routeModels[0].getRouteLineVector()
                )
            }
            response.ifNotSuccessful { exception ->
                _oneTimeEmitter.emit(OneTimeEmitter(exception = exception.type))
                Timber.e(exception.type)
            }
        }
    }

    fun saveMockInformation(
        mockName: String,
        mockDescription: String,
        originLocation: LatLng,
        destinationLocation: LatLng,
        speed: Int
    ) = viewModelScope.launch(exceptionHandler) {
        val lineVector = mockEditorState.value.lineVector
        if (lineVector == null) {
            _oneTimeEmitter.emit(OneTimeEmitter(exception = EXCEPTION_INSERTION_ERROR))
            return@launch
        }
        mockRepository.saveMock(
            MockDataClass(
                mockName = mockName,
                mockDescription = mockDescription,
                mockType = Constant.TYPE_CUSTOM_CREATE,
                originLocation = originLocation,
                destinationLocation = destinationLocation,
                originAddress = _mockEditorState.value.originAddress ?: "Unknown",
                destinationAddress = _mockEditorState.value.destinationAddress ?: "Unknown",
                speed = speed,
                lineVector = lineVector,
                bearing = 0f,
                accuracy = 1F,
                provider = Constant.PROVIDER_GPS
            )
        ).collect { response ->
            response.ifSuccessful { mockId ->
                _mockEditorState.value = _mockEditorState.value.copy(
                    mockId = mockId
                )
            }
            response.ifNotSuccessful { exception ->
                _oneTimeEmitter.emit(OneTimeEmitter(exception = exception.type))
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
                originLocation = null
            )
        } else {
            _mockEditorState.value = _mockEditorState.value.copy(
                destinationAddress = null,
                lineVector = null,
                destinationLocation = null
            )
        }
    }

    fun getMockFromId(
        mockId: Long
    ) = viewModelScope.launch(exceptionHandler) {
        mockRepository.getMock(mockId).collect { response ->
            response.ifSuccessful { mockData ->
                _mockEditorState.value = _mockEditorState.value.copy(
                    mockInformation = mockData
                )
            }
            response.ifNotSuccessful { exception ->
                _oneTimeEmitter.emit(OneTimeEmitter(exception = exception.type))
                Timber.e(exception.type)
            }
        }
    }

    fun deleteMock() = viewModelScope.launch(exceptionHandler) {
        mockRepository.deleteMock(_mockEditorState.value.mockInformation!!)
        _mockEditorState.value = _mockEditorState.value.copy(
            mockInformation = null
        )
    }

    fun loadMockData(
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
        withContext(Dispatchers.Default) { getRouteInformation(realOrigin, realDestination) }
    }
}