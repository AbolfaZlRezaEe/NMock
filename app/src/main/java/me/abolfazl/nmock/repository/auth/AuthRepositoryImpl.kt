package me.abolfazl.nmock.repository.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import me.abolfazl.nmock.model.apiService.AuthApiService
import me.abolfazl.nmock.repository.models.SignUpDataclass
import me.abolfazl.nmock.utils.isValidEmail
import me.abolfazl.nmock.utils.logger.NMockLogger
import me.abolfazl.nmock.utils.response.*
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val logger: NMockLogger
) : AuthRepository {

    companion object {
        private const val LOGIN_FAILED_HTTP_CODE = 400

        const val EMAIL_OR_PASSWORD_IS_NOT_VALID_EXCEPTION = 410
        const val SIGNIN_PROCESS_FAILED_EXCEPTION = 411
        const val UNKNOWN_EXCEPTION = 412
        const val SIGNUP_PROCESS_FAILED_EXCEPTION = 413
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
            return@flow
        }
        val response = authApiService.signIn(
            email = email,
            password = password
        )
        if (response.isSuccessful) {
            response.body()?.let {
                // todo: save the token!
                emit(Success(true))
            }
        } else {
            if (response.code() == LOGIN_FAILED_HTTP_CODE) {
                emit(Failure(SIGNIN_PROCESS_FAILED_EXCEPTION))
            } else {
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
            signIn(
                email = signUpDataclass.email,
                password = signUpDataclass.password
            ).collect { response ->
                response.ifSuccessful { _ ->
                    emit(Success(true))
                    return@collect
                }

                response.ifNotSuccessful { _ ->
                    emit(Failure(SIGNUP_PROCESS_FAILED_EXCEPTION))
                    return@collect
                }
            }
        } else {
            // todo: we should check the http code if needed!
            emit(Failure(SIGNUP_PROCESS_FAILED_EXCEPTION))
        }
    }
}