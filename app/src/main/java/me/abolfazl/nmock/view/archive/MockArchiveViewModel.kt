package me.abolfazl.nmock.view.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.abolfazl.nmock.model.database.DATABASE_TYPE_ALL
import me.abolfazl.nmock.repository.mock.MockRepository
import me.abolfazl.nmock.repository.mock.MockRepositoryImpl
import me.abolfazl.nmock.repository.mock.models.viewModels.MockDataClass
import me.abolfazl.nmock.utils.logger.NMockLogger
import me.abolfazl.nmock.utils.response.OneTimeEmitter
import me.abolfazl.nmock.utils.response.SingleEvent
import me.abolfazl.nmock.utils.response.ifNotSuccessful
import me.abolfazl.nmock.utils.response.ifSuccessful
import javax.inject.Inject

@HiltViewModel
class MockArchiveViewModel @Inject constructor(
    private val mockRepository: MockRepository,
    private val logger: NMockLogger
) : ViewModel() {

    companion object {
        const val ACTION_UNKNOWN = "UNKNOWN_EXCEPTION"
        const val ACTION_PROCESS_EXPORTING_MOCK = "ACTION_EXPORTING_MOCK"
    }

    private val _mockArchiveState = MutableStateFlow(MockArchiveState())
    val mockArchiveState = _mockArchiveState.asStateFlow()

    private val _oneTimeEmitter = MutableSharedFlow<OneTimeEmitter>()
    val oneTimeEmitter = _oneTimeEmitter.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logger.captureExceptionWithLogFile(
            message = "Exception thrown in MockArchiveViewModel: ${throwable.message}"
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

    fun getMocksInformation() = viewModelScope.launch(exceptionHandler) {
        mockRepository.getMocksInformation(DATABASE_TYPE_ALL)
            .collect { response ->
                response.ifSuccessful { mockList ->
                    _mockArchiveState.value = _mockArchiveState.value.copy(
                        mockList = SingleEvent(mockList)
                    )
                }
                response.ifNotSuccessful { exceptionType ->
                    _oneTimeEmitter.emit(
                        OneTimeEmitter(
                            actionId = ACTION_UNKNOWN,
                            message = actionMapper(exceptionType)
                        )
                    )
                }
            }
    }

    fun deleteAllMocks() = viewModelScope.launch(exceptionHandler) {
        mockRepository.deleteAllMocks(DATABASE_TYPE_ALL)
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
            mockRepository.createMockExportFile(
                mockDatabaseType = mockDataClass.mockDatabaseType!!,
                id = mockDataClass.id!!
            ).collect { response ->
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
                            actionId = ACTION_PROCESS_EXPORTING_MOCK,
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
            MockRepositoryImpl.DATABASE_EMPTY_MOCK_INFORMATION,
            MockRepositoryImpl.DATABASE_WRONG_TYPE_EXCEPTION,
            MockRepositoryImpl.CREATE_EXPORT_FILE_EXCEPTION -> MockArchiveActivity.EXPORTING_MOCK_FAILED_MESSAGE
            else -> MockArchiveActivity.UNKNOWN_ERROR_MESSAGE
        }
    }
}