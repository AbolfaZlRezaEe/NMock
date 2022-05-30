package me.abolfazl.nmock.view.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.abolfazl.nmock.repository.mock.MockRepository
import me.abolfazl.nmock.utils.response.OneTimeEmitter
import javax.inject.Inject

@HiltViewModel
class MockArchiveViewModel @Inject constructor(
    private val mockRepository: MockRepository
) : ViewModel() {

    companion object {
        const val ACTION_UNKNOWN = "UNKNOWN_EXCEPTION"
    }

    private val _mockArchiveState = MutableStateFlow(MockArchiveState())
    val mockArchiveState = _mockArchiveState.asStateFlow()

    private val _oneTimeEmitter = MutableSharedFlow<OneTimeEmitter>()
    val oneTimeEmitter = _oneTimeEmitter.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Sentry.captureMessage("Exception thrown in MockArchiveViewModel: " + throwable.message)
        viewModelScope.launch {
            _oneTimeEmitter.emit(
                OneTimeEmitter(
                    actionId = ACTION_UNKNOWN,
                    message = errorMapper(0)
                )
            )
        }
    }

    fun getMocks() = viewModelScope.launch(exceptionHandler) {
        val mockList = mockRepository.getMocks()
        _mockArchiveState.value = _mockArchiveState.value.copy(
            mockList = mockList
        )
    }

    fun deleteAllMocks() = viewModelScope.launch(exceptionHandler) {
        mockRepository.deleteAllMocks()
        _mockArchiveState.value = _mockArchiveState.value.copy(
            mockList = null
        )
    }

    private fun errorMapper(errorType: Int): Int {
        return MockArchiveActivity.UNKNOWN_ERROR_MESSAGE
    }
}