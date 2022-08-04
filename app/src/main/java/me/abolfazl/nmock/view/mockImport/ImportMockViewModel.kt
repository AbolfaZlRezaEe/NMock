package me.abolfazl.nmock.view.mockImport

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
import me.abolfazl.nmock.repository.mock.importedMock.ImportedMockRepository
import me.abolfazl.nmock.repository.mock.importedMock.ImportedMockRepositoryImpl
import me.abolfazl.nmock.utils.logger.NMockLogger
import me.abolfazl.nmock.utils.response.OneTimeEmitter
import me.abolfazl.nmock.utils.response.SingleEvent
import me.abolfazl.nmock.utils.response.ifNotSuccessful
import me.abolfazl.nmock.utils.response.ifSuccessful
import me.abolfazl.nmock.view.editor.MockEditorViewModel
import javax.inject.Inject

@HiltViewModel
class ImportMockViewModel @Inject constructor(
    private val importedMockRepository: ImportedMockRepository,
    private val logger: NMockLogger,
) : ViewModel() {

    companion object {
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
        logger.writeLog(value = "Exception thrown in ImportMockViewModel: ${throwable.message}")
        logger.captureEventWithLogFile(
            fromExceptionHandler = true,
            message = "Exception thrown in ImportMockViewModel: ${throwable.message}",
            sentryEventLevel = SentryLevel.ERROR
        )
        viewModelScope.launch {
            _oneTimeEmitter.emit(
                OneTimeEmitter(
                    actionId = MockEditorViewModel.ACTION_UNKNOWN,
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
        importedMockRepository.parseJsonDataString(jsonString).collect { response ->
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
        importedMockRepository.saveMockInformation(
            name = mockDataClass.name,
            description = mockDataClass.description,
            originLocation = mockDataClass.originLocation,
            destinationLocation = mockDataClass.destinationLocation,
            originAddress = mockDataClass.originAddress,
            destinationAddress = mockDataClass.destinationAddress,
            type = mockDataClass.type,
            speed = mockDataClass.speed,
            lineVector = mockDataClass.lineVector,
            bearing = mockDataClass.bearing,
            accuracy = mockDataClass.accuracy,
            provider = mockDataClass.provider,
            fileCreatedAt = mockDataClass.fileCreatedAt,
            fileOwner = mockDataClass.fileOwner,
            versionCode = mockDataClass.versionCode
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
            ImportedMockRepositoryImpl.JSON_PROBLEM_EXCEPTION -> ImportActivity.JSON_STRUCTURE_PROBLEM_MESSAGE
            ImportedMockRepositoryImpl.JSON_PROCESS_FAILED_EXCEPTION -> ImportActivity.JSON_PARSE_PROCESS_PROBLEM_MESSAGE
            ImportedMockRepositoryImpl.LINE_VECTOR_NULL_EXCEPTION,
            ImportedMockRepositoryImpl.DATABASE_INSERTION_EXCEPTION -> ImportActivity.MOCK_INFORMATION_HAS_PROBLEM
            else -> ImportActivity.UNKNOWN_ERROR_MESSAGE
        }
    }
}