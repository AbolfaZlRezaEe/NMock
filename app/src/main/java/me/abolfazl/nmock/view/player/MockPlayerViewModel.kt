package me.abolfazl.nmock.view.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.abolfazl.nmock.repository.mock.MockRepository
import me.abolfazl.nmock.repository.mock.MockRepositoryImpl
import me.abolfazl.nmock.utils.response.OneTimeEmitter
import me.abolfazl.nmock.utils.response.exceptions.EXCEPTION_UNKNOWN
import me.abolfazl.nmock.utils.response.ifNotSuccessful
import me.abolfazl.nmock.utils.response.ifSuccessful
import me.abolfazl.nmock.view.editor.MockEditorActivity
import me.abolfazl.nmock.view.editor.MockEditorViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MockPlayerViewModel @Inject constructor(
    private val mockRepository: MockRepository
) : ViewModel() {

    companion object {
        const val ACTION_UNKNOWN = "UNKNOWN_EXCEPTION"
        const val ACTION_GET_MOCK_INFORMATION = "GET_MOCK_INFORMATION"
        const val ACTION_UPDATE_MOCK_INFORMATION = "UPDATE_MOCK_INFORMATION"
    }

    // for handling states
    private val _mockPlayerState = MutableStateFlow(MockPlayerState())
    val mockPlayerState = _mockPlayerState.asStateFlow()

    // for errors..
    private val _oneTimeEmitter = MutableSharedFlow<OneTimeEmitter>()
    val oneTimeEmitter = _oneTimeEmitter.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Sentry.captureMessage("Exception thrown in MockPlayerViewModel: " + throwable.message)
        viewModelScope.launch {
            _oneTimeEmitter.emit(
                OneTimeEmitter(
                    actionId = ACTION_UNKNOWN,
                    message = errorMapper(0)
                )
            )
        }
    }

    fun getMockInformation(mockId: Long) = viewModelScope.launch(exceptionHandler) {
        mockRepository.getMock(mockId).collect { response ->
            response.ifSuccessful { mockInformation ->
                _mockPlayerState.value = _mockPlayerState.value.copy(
                    mockInformation = mockInformation
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

    fun changeMockSpeed(mockSpeed: Int) = viewModelScope.launch(exceptionHandler) {
        val mock = _mockPlayerState.value.mockInformation!!
        mock.speed = mockSpeed
        _mockPlayerState.value = _mockPlayerState.value.copy(
            mockInformation = mock
        )
        mockRepository.updateMockInformation(_mockPlayerState.value.mockInformation!!)
            .collect { response ->
                response.ifNotSuccessful { exceptionType ->
                    _oneTimeEmitter.emit(
                        OneTimeEmitter(
                            actionId = ACTION_UPDATE_MOCK_INFORMATION,
                            message = errorMapper(exceptionType)
                        )
                    )
                }
            }
    }

    private fun errorMapper(exceptionType: Int): Int {
        return when (exceptionType) {
            MockRepositoryImpl.LINE_VECTOR_NULL_EXCEPTION,
            MockRepositoryImpl.DATABASE_EMPTY_LINE_EXCEPTION,
            MockRepositoryImpl.DATABASE_INSERTION_EXCEPTION -> MockPlayerActivity.MOCK_INFORMATION_IS_WRONG_MESSAGE
            else -> MockPlayerActivity.UNKNOWN_ERROR_MESSAGE
        }
    }
}