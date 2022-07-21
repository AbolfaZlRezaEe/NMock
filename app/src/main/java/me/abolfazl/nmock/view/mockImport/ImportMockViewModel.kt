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
import me.abolfazl.nmock.utils.logger.NMockLogger
import me.abolfazl.nmock.utils.response.OneTimeEmitter
import me.abolfazl.nmock.view.editor.MockEditorState
import me.abolfazl.nmock.view.editor.MockEditorViewModel
import javax.inject.Inject

@HiltViewModel
class ImportMockViewModel @Inject constructor(
    private val importedMockRepository: ImportedMockRepository,
    private val logger: NMockLogger,
) : ViewModel() {

    // for handling states
    private val _importMockState = MutableStateFlow(MockEditorState())
    val importMockState = _importMockState.asStateFlow()

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

    private fun actionMapper(errorType: Int): Int {
        return when (errorType) {
            else -> 0
        }
    }
}