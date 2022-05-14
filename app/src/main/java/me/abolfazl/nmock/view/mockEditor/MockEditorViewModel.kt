package me.abolfazl.nmock.view.mockEditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.abolfazl.nmock.repository.locationInfo.LocationInfoRepository
import me.abolfazl.nmock.repository.mock.MockRepository
import me.abolfazl.nmock.repository.models.MockDataClass
import me.abolfazl.nmock.repository.routingInfo.RoutingInfoRepository
import me.abolfazl.nmock.utils.Constant
import me.abolfazl.nmock.utils.response.SUCCESS_TYPE_MOCK_INSERTION
import me.abolfazl.nmock.utils.response.exceptions.EXCEPTION_FORCE_CLOSE
import me.abolfazl.nmock.utils.response.exceptions.EXCEPTION_INSERTION_ERROR
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
    private val _oneTimeEmitter = MutableSharedFlow<String>()
    val oneTimeEmitter = _oneTimeEmitter.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e("Exception thrown in MockEditorViewModel: " + throwable.message)
        viewModelScope.launch {
            _oneTimeEmitter.emit("$EXCEPTION_FORCE_CLOSE thrown. please check the logcat!")
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
                // for now...
                _oneTimeEmitter.emit(exception.type)
                Timber.e(exception.type)
            }
        }
    }

    fun getRouteInformation(
        originLocation: LatLng,
        destinationLocation: LatLng
    ) = viewModelScope.launch(exceptionHandler) {
        routingInfoRepository.getRoutingInformation(
            origin = getLocationFormattedForServer(originLocation),
            destination = getLocationFormattedForServer(destinationLocation)
        ).collect { response ->
            response.ifSuccessful { result ->
                _mockEditorState.value = _mockEditorState.value.copy(
                    lineVector = result.routeModels[0].getRouteLineVector()
                )
            }
            response.ifNotSuccessful { exception ->
                // for now...
                _oneTimeEmitter.emit(exception.type)
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
            _oneTimeEmitter.emit(EXCEPTION_INSERTION_ERROR)
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
            response.ifSuccessful {
                _oneTimeEmitter.emit(SUCCESS_TYPE_MOCK_INSERTION)
            }
            response.ifNotSuccessful { exception ->
                _oneTimeEmitter.emit(exception.type)
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
                lineVector = null
            )
        } else {
            _mockEditorState.value = _mockEditorState.value.copy(
                destinationAddress = null,
                lineVector = null
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
                _oneTimeEmitter.emit(exception.type)
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

    private fun getLocationFormattedForServer(
        location: LatLng
    ): String {
        return "${location.latitude},${location.longitude}"
    }
}