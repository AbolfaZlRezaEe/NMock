package me.abolfazl.nmock.view.mockImport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject

@HiltViewModel
class ImportMockViewModel @Inject constructor(
    private val mockRepository: MockRepository,
    private val logger: NMockLogger,
) : ViewModel() {

    companion object {
        const val ACTION_UNKNOWN = "UNKNOWN_EXCEPTION"
        const val ACTION_PARSE_JSON_MOCK = "ACTION_PARSE_JSON_MOCK"
        const val ACTION_FORCE_CLOSE_IMPORT_PREVIEW = "FORCE_CLOSE_IMPORT_PREVIEW"
        const val ACTION_MOCK_DOES_NOT_SAVED = "ACTION_MOCK_DOES_NOT_SAVED"
    }

    // for handling states
    private val _importedMockState = MutableStateFlow(ImportedMockState())
    val importMockState = _importedMockState.asStateFlow()

    // for actions..
    private val _oneTimeEmitter = MutableSharedFlow<OneTimeEmitter>()
    val oneTimeEmitter = _oneTimeEmitter.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logger.captureExceptionWithLogFile(
            message = "Exception thrown in ImportMockViewModel: ${throwable.message}",
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

    fun parseJsonData(
        jsonString: String
    ) = viewModelScope.launch(exceptionHandler) {
        _importedMockState.value = _importedMockState.value.copy(
            showImportLoading = SingleEvent(true)
        )
        mockRepository.parseJsonDataModelString(jsonString).collect { response ->
            _importedMockState.value = _importedMockState.value.copy(
                showImportLoading = SingleEvent(false)
            )
            response.ifSuccessful { result ->
                _importedMockState.value = _importedMockState.value.copy(
                    mockImportedInformation = SingleEvent(result)
                )
            }
            response.ifNotSuccessful { exceptionType ->
                _oneTimeEmitter.emit(
                    OneTimeEmitter(
                        actionId = ACTION_PARSE_JSON_MOCK,
                        message = actionMapper(exceptionType)
                    )
                )
            }
        }
    }

    fun saveMockInformation(
        shouldOpenOnEditor: Boolean
    ) = viewModelScope.launch(exceptionHandler) {
        _importedMockState.value = _importedMockState.value.copy(
            showSaveLoading = SingleEvent(true)
        )
        _importedMockState.value = _importedMockState.value.copy(
            shouldOpenOnEditor = shouldOpenOnEditor
        )
        val mockDataClass = _importedMockState.value.mockImportedInformation?.getRawValue()
        if (mockDataClass == null) {
            _oneTimeEmitter.emit(
                OneTimeEmitter(
                    actionId = ACTION_FORCE_CLOSE_IMPORT_PREVIEW,
                    actionMapper(0)
                )
            )
            return@launch
        }
        mockRepository.saveMockInformation(
            MockDataClass(
                name = mockDataClass.name,
                description = mockDataClass.description,
                originLocation = mockDataClass.originLocation,
                destinationLocation = mockDataClass.destinationLocation,
                originAddress = mockDataClass.originAddress,
                destinationAddress = mockDataClass.destinationAddress,
                creationType = mockDataClass.creationType,
                speed = mockDataClass.speed,
                lineVector = mockDataClass.lineVector,
                bearing = mockDataClass.bearing,
                accuracy = mockDataClass.accuracy,
                provider = mockDataClass.provider,
                fileCreatedAt = mockDataClass.fileCreatedAt,
                fileOwner = mockDataClass.fileOwner,
                applicationVersionCode = mockDataClass.applicationVersionCode,
                mockDatabaseType = mockDataClass.mockDatabaseType,
            )
        ).collect { response ->
            response.ifSuccessful { importedMockId ->
                _importedMockState.value = _importedMockState.value.copy(
                    finalMockId = SingleEvent(importedMockId)
                )
            }
            response.ifNotSuccessful { exceptionType ->
                _oneTimeEmitter.emit(
                    OneTimeEmitter(
                        actionId = ACTION_MOCK_DOES_NOT_SAVED,
                        actionMapper(exceptionType)
                    )
                )
            }
        }
    }

    fun clearImportMockState() {
        _importedMockState.value = ImportedMockState()
    }

    private fun actionMapper(errorType: Int): Int {
        return when (errorType) {
            MockRepositoryImpl.JSON_PROBLEM_EXCEPTION -> MockImportActivity.JSON_STRUCTURE_PROBLEM_MESSAGE
            MockRepositoryImpl.JSON_PROCESS_FAILED_EXCEPTION -> MockImportActivity.JSON_PARSE_PROCESS_PROBLEM_MESSAGE
            MockRepositoryImpl.LINE_VECTOR_NULL_EXCEPTION,
            MockRepositoryImpl.DATABASE_WRONG_TYPE_EXCEPTION,
            MockRepositoryImpl.DATABASE_INSERTION_EXCEPTION -> MockImportActivity.MOCK_INFORMATION_HAS_PROBLEM
            else -> MockImportActivity.UNKNOWN_ERROR_MESSAGE
        }
    }
}