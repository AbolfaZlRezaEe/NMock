package me.abolfazl.nmock.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.abolfazl.nmock.model.apiService.RoutingApiService
import me.abolfazl.nmock.utils.Constant
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideRetrofitInstance(
        client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(Constant.BASE_URL)
            .build()
    }

    @Singleton
    @Provides
    fun provideLoggerInstance(
        httpLoggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { oldInterceptor ->
                val oldRequest = oldInterceptor.request()
                val newRequest = oldRequest.newBuilder()

                newRequest.addHeader(Constant.HEADER_API_KEY, Constant.HEADER_VALUE_API_KEY)

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
        retrofit: Retrofit
    ): RoutingApiService {
        return retrofit.create(RoutingApiService::class.java)
    }

}