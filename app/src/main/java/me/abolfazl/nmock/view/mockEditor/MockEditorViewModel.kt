package me.abolfazl.nmock.view.mockEditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun getLocationInformation(
        location: LatLng,
        isOrigin: Boolean
    ) = viewModelScope.launch {
        locationInfoRepository.getLocationInformation(
            location.latitude,
            location.longitude
        ).collect { response ->
            response.ifSuccessful { result ->
                _mockEditorState.value = MockEditorState.build(_mockEditorState.value) {
                    if (isOrigin) originAddress = result.fullAddress
                    else destinationAddress = result.fullAddress
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
    ) = viewModelScope.launch {
        routingInfoRepository.getRoutingInformation(
            origin = getLocationFormattedForServer(originLocation),
            destination = getLocationFormattedForServer(destinationLocation)
        ).collect { response ->
            response.ifSuccessful { result ->
                _mockEditorState.value = MockEditorState.build(_mockEditorState.value) {
                    lineVector = result.routeModels[0].getRouteLineVector()
                }
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
        mockDescription: String
    ) = viewModelScope.launch {
        val lineVector = mockEditorState.value.lineVector
        if (lineVector == null) {
            _oneTimeEmitter.emit(EXCEPTION_INSERTION_ERROR)
            return@launch
        }
        mockRepository.addMock(
            MockDataClass(
                mockName = mockName,
                mockDescription = mockDescription,
                mockType = Constant.TYPE_CUSTOM_CREATE,
                lineVector = lineVector
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

    private fun getLocationFormattedForServer(
        location: LatLng
    ): String {
        return "${location.latitude},${location.longitude}"
    }
}