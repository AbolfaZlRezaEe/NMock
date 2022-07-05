package me.abolfazl.nmock.view.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.SentryLevel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.abolfazl.nmock.repository.mock.MockRepository
import me.abolfazl.nmock.utils.logger.NMockLogger
import me.abolfazl.nmock.utils.response.OneTimeEmitter
import javax.inject.Inject

@HiltViewModel
class MockArchiveViewModel @Inject constructor(
    private val mockRepository: MockRepository,
    private val logger: NMockLogger
) : ViewModel() {

    companion object {
        const val ACTION_UNKNOWN = "UNKNOWN_EXCEPTION"
    }

    private val _mockArchiveState = MutableStateFlow(MockArchiveState())
    val mockArchiveState = _mockArchiveState.asStateFlow()

    private val _oneTimeEmitter = MutableSharedFlow<OneTimeEmitter>()
    val oneTimeEmitter = _oneTimeEmitter.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logger.writeLog(value = "Exception thrown in MockArchiveViewModel: ${throwable.message}")
        logger.sendLogsFile(
            fromExceptionHandler = true,
            message = "Exception thrown in MockArchiveViewModel: ${throwable.message}",
            sentryEventLevel = SentryLevel.ERROR
        )
        viewModelScope.launch {
            _oneTimeEmitter.emit(
                OneTimeEmitter(
                    actionId = ACTION_UNKNOWN,
                    message = actionMapper()
                )
            )
        }
    }

    init {
        logger.disableLogHeaderForThisClass()
        logger.setClassInformationForEveryLog(javaClass.simpleName)
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

    private fun actionMapper(): Int {
        return MockArchiveActivity.UNKNOWN_ERROR_MESSAGE
    }
}