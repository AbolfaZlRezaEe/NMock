package me.abolfazl.nmock.model.apiService

import me.abolfazl.nmock.model.apiService.models.auth.SignInModel
import me.abolfazl.nmock.model.apiService.models.auth.SignUpModel
import me.abolfazl.nmock.model.apiService.models.auth.UserInformationModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApiService {

    @POST("/api/auth/login")
    suspend fun signIn(
        @Query("email") email: String,
        @Query("password") password: String
    ): Response<SignInModel>

    @POST("/api/auth/register")
    suspend fun signUp(
        @Query("avatar") avatar: String,
        @Query("first_name") firstName: String,
        @Query("last_name") lastName: String,
        @Query("email") email: String,
        @Query("password") password: String
    ): Response<SignUpModel>

    @GET("/api/auth/me")
    suspend fun getUserInformation(
        @Header("Authorization") userToken: String
    ): Response<UserInformationModel>
}