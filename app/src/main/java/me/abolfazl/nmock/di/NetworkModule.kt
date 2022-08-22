package me.abolfazl.nmock.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.abolfazl.nmock.model.apiService.AuthApiService
import me.abolfazl.nmock.model.apiService.RoutingApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Base URLs
    private const val ROUTING_BASE_URL = "https://api.neshan.org/"
    private const val AUTH_BASE_URL = "https://abolfazlrezaee.ir/"

    // For Retrofits
    private const val ROUTING_API_RETROFIT_INSTANCE = "ROUTING_RETROFIT_INSTANCE"
    private const val AUTH_API_RETROFIT_INSTANCE = "AUTH_RETROFIT_INSTANCE"

    // For clients
    private const val ROUTING_API_CLIENT = "ROUTING_API_CLIENT"
    private const val AUTH_API_CLIENT = "AUTH_API_CLIENT"

    // Header Keys
    private const val ROUTING_HEADER_API_KEY = "Api-Key"
    const val AUTH_HEADER_ACCEPT_KEY = "Accept"
    const val AUTH_HEADER_AUTHORIZATION_KEY = "Authorization"

    // Header values
    private const val ROUTING_HEADER_VALUE_API_KEY =
        "service.2xpUYE0D5pjJZOSSUwmzlhjQrKB4g68pcg9wzDJg"
    const val AUTH_HEADER_VALUE_ACCEPT = "application/json"

    // For Pusher
    const val BEAMS_AUTH_URL_INSTANCE = "BEAMS_AUTH_URL"
    const val PUSHER_INSTANCE_ID_NAME = "PUSHER_INSTANCE_ID"

    @Singleton
    @Provides
    @Named(ROUTING_API_RETROFIT_INSTANCE)
    fun provideRoutingRetrofitInstance(
        @Named(ROUTING_API_CLIENT) client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(ROUTING_BASE_URL)
            .build()
    }

    @Singleton
    @Provides
    @Named(ROUTING_API_CLIENT)
    fun provideRoutingClientInstance(
        httpLoggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { oldInterceptor ->
                val oldRequest = oldInterceptor.request()
                val newRequest = oldRequest.newBuilder()

                newRequest.addHeader(ROUTING_HEADER_API_KEY, ROUTING_HEADER_VALUE_API_KEY)

                newRequest.method(oldRequest.method, oldRequest.body)
                return@addInterceptor oldInterceptor.proceed(newRequest.build())
            }.addInterceptor(httpLoggingInterceptor).build()
    }

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
    }

    @Singleton
    @Provides
    fun provideRoutingApiService(
        @Named(ROUTING_API_RETROFIT_INSTANCE) retrofit: Retrofit
    ): RoutingApiService {
        return retrofit.create(RoutingApiService::class.java)
    }

    @Singleton
    @Provides
    @Named(AUTH_API_RETROFIT_INSTANCE)
    fun provideAuthRetrofitInstance(
        @Named(AUTH_API_CLIENT) client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(AUTH_BASE_URL)
            .build()
    }

    @Singleton
    @Provides
    @Named(AUTH_API_CLIENT)
    fun provideAuthClientInstance(
        httpLoggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { oldInterceptor ->
                val oldRequest = oldInterceptor.request()
                val newRequest = oldRequest.newBuilder()

                newRequest.addHeader(AUTH_HEADER_ACCEPT_KEY, AUTH_HEADER_VALUE_ACCEPT)

                newRequest.method(oldRequest.method, oldRequest.body)
                return@addInterceptor oldInterceptor.proceed(newRequest.build())
            }.addInterceptor(httpLoggingInterceptor).build()
    }

    @Singleton
    @Provides
    fun provideAuthApiService(
        @Named(AUTH_API_RETROFIT_INSTANCE) retrofit: Retrofit
    ): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Singleton
    @Provides
    @Named(BEAMS_AUTH_URL_INSTANCE)
    fun provideBeamsBaseUrl(): String {
        return "${AUTH_BASE_URL}api/auth/beams"
    }

    @Singleton
    @Provides
    @Named(PUSHER_INSTANCE_ID_NAME)
    fun providePusherInstanceId(): String {
        return "b19d3795-6e91-4edc-8e15-79ead0737748"
    }

}