package me.abolfazl.nmock.view.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.abolfazl.nmock.repository.mock.MockRepository
import me.abolfazl.nmock.utils.response.OneTimeEmitter
import me.abolfazl.nmock.utils.response.exceptions.EXCEPTION_UNKNOWN
import me.abolfazl.nmock.utils.response.ifNotSuccessful
import me.abolfazl.nmock.utils.response.ifSuccessful
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MockPlayerViewModel @Inject constructor(
    private val mockRepository: MockRepository
) : ViewModel() {

    // for handling states
    private val _mockPlayerState = MutableStateFlow(MockPlayerState())
    val mockPlayerState = _mockPlayerState.asStateFlow()

    // for errors..
    private val _oneTimeEmitter = MutableSharedFlow<OneTimeEmitter<String>>()
    val oneTimeEmitter = _oneTimeEmitter.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e("Exception thrown in MockPlayerViewModel: " + throwable.message)
        viewModelScope.launch {
            _oneTimeEmitter.emit(OneTimeEmitter(exception = EXCEPTION_UNKNOWN))
        }
    }

    fun getMockInformation(mockId: Long) = viewModelScope.launch(exceptionHandler) {
        mockRepository.getMock(mockId).collect { response ->
            response.ifSuccessful { mockInformation ->
                _mockPlayerState.value = _mockPlayerState.value.copy(
                    mockInformation = mockInformation
                )
            }
            response.ifNotSuccessful { exception ->
                _oneTimeEmitter.emit(OneTimeEmitter(exception = exception.type))
                Timber.e(exception.type)
            }
        }
    }

    fun changeMockSpeed(mockSpeed: Int) = viewModelScope.launch(exceptionHandler) {
        val mock = _mockPlayerState.value.mockInformation!!
        mock.speed = mockSpeed
        _mockPlayerState.value = _mockPlayerState.value.copy(
            mockInformation = mock
        )
        mockRepository.saveMock(_mockPlayerState.value.mockInformation!!).collect { response ->
            response.ifNotSuccessful { exception ->
                _oneTimeEmitter.emit(OneTimeEmitter(exception = exception.type))
            }
        }
    }
}