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
import me.abolfazl.nmock.repository.mock.MockRepositoryImpl
import me.abolfazl.nmock.repository.mock.models.viewModels.MockDataClass
import me.abolfazl.nmock.utils.logger.NMockLogger
import me.abolfazl.nmock.utils.response.OneTimeEmitter
import me.abolfazl.nmock.utils.response.SingleEvent
import me.abolfazl.nmock.utils.response.ifNotSuccessful
import me.abolfazl.nmock.utils.response.ifSuccessful
import me.abolfazl.nmock.view.editor.MockEditorViewModel
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
        logger.captureEventWithLogFile(
            fromExceptionHandler = true,
            message = "Exception thrown in MockArchiveViewModel: ${throwable.message}",
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

    init {
        logger.disableLogHeaderForThisClass()
        logger.setClassInformationForEveryLog(javaClass.simpleName)
    }

    fun getMocks() = viewModelScope.launch(exceptionHandler) {
        val mockList = mockRepository.getMocks()
        _mockArchiveState.value = _mockArchiveState.value.copy(
            mockList = SingleEvent(mockList)
        )
    }

    fun deleteAllMocks() = viewModelScope.launch(exceptionHandler) {
        mockRepository.deleteAllMocks()
        _mockArchiveState.value = _mockArchiveState.value.copy(
            mockList = null
        )
    }

    fun processExportingMock(mockDataClass: MockDataClass) =
        viewModelScope.launch(exceptionHandler) {
            _mockArchiveState.value = _mockArchiveState.value.copy(
                sharedMockDataClassState = SingleEvent(mockDataClass.apply {
                    showShareLoading = true
                })
            )
            mockRepository.createMockExportFile(mockDataClass.id!!).collect { response ->
                _mockArchiveState.value = _mockArchiveState.value.copy(
                    sharedMockDataClassState = SingleEvent(mockDataClass.apply {
                        showShareLoading = false
                    })
                )
                response.ifSuccessful { file ->
                    _mockArchiveState.value = _mockArchiveState.value.copy(
                        file = SingleEvent(file)
                    )
                }
                response.ifNotSuccessful { exceptionType ->
                    _oneTimeEmitter.emit(
                        OneTimeEmitter(
                            actionId = MockEditorViewModel.ACTION_LOCATION_INFORMATION,
                            message = actionMapper(exceptionType)
                        )
                    )
                }
            }
        }

    private fun actionMapper(action: Int): Int {
        return when (action) {
            MockRepositoryImpl.DATABASE_EMPTY_LINE_EXCEPTION -> MockArchiveActivity.MOCK_INFORMATION_IS_WRONG_MESSAGE
            MockRepositoryImpl.CONVERT_MOCK_TO_JSON_EXCEPTION,
            MockRepositoryImpl.CREATE_EXPORT_FILE_EXCEPTION -> MockArchiveActivity.EXPORTING_MOCK_FAILED_MESSAGE
            else -> MockArchiveActivity.UNKNOWN_ERROR_MESSAGE
        }
    }
}