package me.abolfazl.nmock.view.mockEditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.abolfazl.nmock.repository.locationInfo.LocationInfoRepository
import me.abolfazl.nmock.repository.routingInfo.RoutingInfoRepository
import me.abolfazl.nmock.utils.response.ifNotSuccessful
import me.abolfazl.nmock.utils.response.ifSuccessful
import org.neshan.common.model.LatLng
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MockEditorViewModel @Inject constructor(
    private val locationInfoRepository: LocationInfoRepository,
    private val routingInfoRepository: RoutingInfoRepository
) : ViewModel() {

    // for handling states
    private val _mockEditorState: MutableStateFlow<MockEditorState> =
        MutableStateFlow(MockEditorState())
    val mockEditorState: StateFlow<MockEditorState> = _mockEditorState

    // for errors..
    private val _errorEmitter: MutableSharedFlow<String> = MutableSharedFlow()
    val errorEmitter: SharedFlow<String> = _errorEmitter

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
                _errorEmitter.emit(exception.type)
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
                _errorEmitter.emit(exception.type)
                Timber.e(exception.type)
            }
        }
    }

    private fun getLocationFormattedForServer(
        location: LatLng
    ): String {
        return "${location.latitude},${location.longitude}"
    }
}