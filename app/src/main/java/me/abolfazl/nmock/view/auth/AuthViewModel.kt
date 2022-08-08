package me.abolfazl.nmock.view.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import me.abolfazl.nmock.repository.auth.AuthRepository
import me.abolfazl.nmock.repository.auth.AuthRepositoryImpl
import me.abolfazl.nmock.repository.models.SignUpDataclass
import me.abolfazl.nmock.utils.logger.NMockLogger
import me.abolfazl.nmock.utils.response.OneTimeEmitter
import me.abolfazl.nmock.utils.response.ifNotSuccessful
import me.abolfazl.nmock.utils.response.ifSuccessful
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val logger: NMockLogger,
) : ViewModel() {

    companion object {
        const val ACTION_UNKNOWN = "UNKNOWN_EXCEPTION"
        const val ACTION_AUTH_SUCCESSFULLY = "AUTH_SUCCESSFULLY"
        const val ACTION_AUTH_FAILED = "AUTH_FAILED"
    }

    // for errors and actions..
    private val _oneTimeEmitter = MutableSharedFlow<OneTimeEmitter>()
    val oneTimeEmitter = _oneTimeEmitter.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logger.captureExceptionWithLogFile(
            message = "Exception thrown in AuthViewModel: ${throwable.message}",
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

    fun signIn(
        email: String,
        password: String
    ) = viewModelScope.launch(exceptionHandler) {
        authRepository.signIn(
            email = email,
            password = password
        ).collect { response ->
            response.ifSuccessful {
                _oneTimeEmitter.emit(
                    OneTimeEmitter(
                        actionId = ACTION_AUTH_SUCCESSFULLY,
                        message = 0
                    )
                )
            }
            response.ifNotSuccessful { exceptionType ->
                _oneTimeEmitter.emit(
                    OneTimeEmitter(
                        actionId = ACTION_AUTH_FAILED,
                        message = actionMapper(exceptionType)
                    )
                )
            }
        }
    }

    fun signUp(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ) = viewModelScope.launch(exceptionHandler) {
        authRepository.signUp(
            SignUpDataclass(
                email = email,
                firstName = firstName,
                lastName = lastName,
                password = password,
            )
        ).collect { response ->
            response.ifSuccessful {
                _oneTimeEmitter.emit(
                    OneTimeEmitter(
                        actionId = ACTION_AUTH_SUCCESSFULLY,
                        message = 0
                    )
                )
            }
            response.ifNotSuccessful { exceptionType ->
                _oneTimeEmitter.emit(
                    OneTimeEmitter(
                        actionId = ACTION_AUTH_FAILED,
                        message = actionMapper(exceptionType)
                    )
                )
            }
        }
    }

    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }

    private fun actionMapper(exceptionType: Int): Int {
        return when (exceptionType) {
            AuthRepositoryImpl.EMAIL_OR_PASSWORD_IS_NOT_VALID_EXCEPTION ->
                SignInFragment.EMAIL_OR_PASSWORD_IS_NOT_VALID_MESSAGE
            AuthRepositoryImpl.SIGNIN_PROCESS_FAILED_EXCEPTION ->
                SignInFragment.SIGNIN_PROCESS_FAILED_MESSAGE
            AuthRepositoryImpl.SIGNUP_PROCESS_FAILED_EXCEPTION ->
                SignUpFragment.SIGNUP_PROCESS_FAILED_MESSAGE
            else -> AuthActivity.UNKNOWN_ERROR_MESSAGE
        }
    }
}