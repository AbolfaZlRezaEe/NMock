package me.abolfazl.nmock.repository.auth

import kotlinx.coroutines.flow.Flow
import me.abolfazl.nmock.repository.auth.models.SignUpDataclass
import me.abolfazl.nmock.utils.response.Response

interface AuthRepository {

    fun signIn(
        email: String,
        password: String
    ): Flow<Response<Boolean, Int>>

    fun signUp(
        signUpDataclass: SignUpDataclass
    ): Flow<Response<Boolean, Int>>

    suspend fun isUserLoggedIn(): Boolean
}