package me.abolfazl.nmock.repository.auth

import android.content.SharedPreferences
import com.pusher.pushnotifications.BeamsCallback
import com.pusher.pushnotifications.PushNotifications
import com.pusher.pushnotifications.PusherCallbackError
import com.pusher.pushnotifications.auth.AuthData
import com.pusher.pushnotifications.auth.AuthDataGetter
import com.pusher.pushnotifications.auth.BeamsTokenProvider
import io.sentry.Sentry
import io.sentry.SentryLevel
import io.sentry.protocol.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.abolfazl.nmock.di.NetworkModule
import me.abolfazl.nmock.di.UtilsModule
import me.abolfazl.nmock.model.apiService.AuthApiService
import me.abolfazl.nmock.repository.auth.models.SignUpDataclass
import me.abolfazl.nmock.utils.SHARED_AUTH_TOKEN
import me.abolfazl.nmock.utils.SHARED_USER_ID_TOKEN
import me.abolfazl.nmock.utils.isValidEmail
import me.abolfazl.nmock.utils.logger.NMockLogger
import me.abolfazl.nmock.utils.managers.SharedManager
import me.abolfazl.nmock.utils.response.*
import javax.inject.Inject
import javax.inject.Named

class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val sharedPreferences: SharedPreferences,
    @Named(UtilsModule.INJECT_STRING_ANDROID_ID)
    private val androidId: String,
    @Named(NetworkModule.BEAMS_AUTH_URL_INSTANCE)
    private val beamsAuthURL: String,
    private val logger: NMockLogger
) : AuthRepository {

    companion object {
        private const val LOGIN_FAILED_HTTP_CODE = 400

        const val EMAIL_OR_PASSWORD_IS_NOT_VALID_EXCEPTION = 410
        const val SIGNIN_PROCESS_FAILED_EXCEPTION = 411
        const val SIGNUP_PROCESS_FAILED_EXCEPTION = 413
        const val UNKNOWN_EXCEPTION = 412
    }

    init {
        logger.disableLogHeaderForThisClass()
        logger.setClassInformationForEveryLog(javaClass.simpleName)
    }


    override fun signIn(
        email: String,
        password: String
    ): Flow<Response<Boolean, Int>> = flow {
        if (email.isEmpty() || password.isEmpty() || !email.isValidEmail()) {
            emit(Failure(EMAIL_OR_PASSWORD_IS_NOT_VALID_EXCEPTION))
            logger.writeLog(value = "validation of sigIn was failed! email-> $email, password-> $password")
            return@flow
        }
        val response = authApiService.signIn(
            email = email,
            password = password
        )
        if (response.isSuccessful) {
            response.body()?.let { result ->
                SharedManager.putString(
                    sharedPreferences = sharedPreferences,
                    key = SHARED_AUTH_TOKEN,
                    value = result.token
                )
                Sentry.configureScope {
                    val user: User = if (it.user != null) it.user!! else User()
                    user.email = email
                    user.id = androidId
                    it.user = user
                }
                logger.writeLog(value = "token successfully received! we are going to save it!")
                emit(Success(true))
            }
        } else {
            if (response.code() == LOGIN_FAILED_HTTP_CODE) {
                emit(Failure(SIGNIN_PROCESS_FAILED_EXCEPTION))
                logger.writeLog(value = "email or password is wrong!")
            } else {
                logger.writeLog(value = "unknown exception thrown from server for signIn. exception-> ${response.errorBody()}")
                Sentry.captureMessage(
                    "unknown exception thrown from server for signIn. exception-> ${response.errorBody()}",
                    SentryLevel.FATAL
                )
                emit(Failure(UNKNOWN_EXCEPTION))
            }
        }
    }

    override fun signUp(
        signUpDataclass: SignUpDataclass
    ): Flow<Response<Boolean, Int>> = flow {
        val response = authApiService.signUp(
            email = signUpDataclass.email,
            password = signUpDataclass.password,
            avatar = "" /* We don't have avatar feature right now...*/,
            firstName = signUpDataclass.firstName,
            lastName = signUpDataclass.lastName
        )
        if (response.isSuccessful) {
            logger.writeLog(value = "user successfully registered")
            Sentry.configureScope {
                val user = User()
                user.username = "${signUpDataclass.firstName} ${signUpDataclass.lastName}"
                it.user = user
            }
            signIn(
                email = signUpDataclass.email,
                password = signUpDataclass.password
            ).collect { result ->
                result.ifSuccessful {
                    emit(Success(true))
                    return@collect
                }

                result.ifNotSuccessful { exceptionType ->
                    emit(Failure(exceptionType))
                    return@collect
                }
            }
        } else {
            emit(Failure(SIGNUP_PROCESS_FAILED_EXCEPTION))
            logger.writeLog(value = "unknown exception thrown from server for signUp. exception-> ${response.errorBody()}")
            Sentry.captureMessage(
                "unknown exception thrown from server for signUp. exception-> ${response.errorBody()}",
                SentryLevel.FATAL
            )
        }
    }

    override suspend fun isUserLoggedIn(): Boolean {
        val userToken = SharedManager.getString(
            sharedPreferences = sharedPreferences,
            key = SHARED_AUTH_TOKEN,
            defaultValue = null
        ) ?: return false

        return loadUserInformation(token = userToken)
    }

    private suspend fun loadUserInformation(
        token: String
    ): Boolean {
        val response = authApiService.getUserInformation(provideUserToken(token))
        if (response.isSuccessful) {
            response.body()?.let { userInformation ->
                logger.writeLog(value = "We receive user information successfully! userId-> ${userInformation.userId}")
                SharedManager.putInt(
                    sharedPreferences = sharedPreferences,
                    key = SHARED_USER_ID_TOKEN,
                    value = userInformation.userId
                )
                initializePusher(
                    userToken = provideUserToken(token),
                    userId = userInformation.userId.toString()
                )
                return true
            }
            return false
        } else {
            logger.writeLog(value = "unknown exception thrown from server for loadUserInformation. exception-> ${response.errorBody()}")
            Sentry.captureMessage(
                "unknown exception thrown from server for loadUserInformation. exception-> ${response.errorBody()}",
                SentryLevel.FATAL
            )
            return false
        }
    }

    private fun initializePusher(
        userToken: String,
        userId: String
    ) {
        val tokenProvider = BeamsTokenProvider(
            beamsAuthURL,
            object : AuthDataGetter {
                override fun getAuthData(): AuthData {
                    return AuthData(
                        headers = hashMapOf(
                            NetworkModule.AUTH_HEADER_ACCEPT_KEY to NetworkModule.AUTH_HEADER_VALUE_ACCEPT,
                            NetworkModule.AUTH_HEADER_AUTHORIZATION_KEY to userToken
                        ),
                        queryParams = hashMapOf()
                    )
                }
            }
        )

        PushNotifications.setUserId(
            userId,
            tokenProvider,
            object : BeamsCallback<Void, PusherCallbackError> {
                override fun onFailure(error: PusherCallbackError) {
                    logger.captureExceptionWithLogFile(
                        message = "We had an exception on set user id in pusher! ${error.message}"
                    )
                }

                override fun onSuccess(vararg values: Void) {
                    logger.writeLog(value = "We successfully set user id for the user!")
                }
            }
        )
    }

    private fun provideUserToken(
        rawToken: String
    ): String = "Bearer $rawToken"
}