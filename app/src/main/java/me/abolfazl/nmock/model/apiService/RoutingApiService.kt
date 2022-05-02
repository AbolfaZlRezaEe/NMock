package me.abolfazl.nmock.model.apiService

import me.abolfazl.nmock.model.apiService.models.locationInfo.LocationInfoModel
import me.abolfazl.nmock.model.apiService.models.routingInfo.RoutingInfoModel
import retrofit2.http.GET
import retrofit2.http.Query

interface RoutingApiService {

    @GET("v2/reverse")
    suspend fun getLocationInformation(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double
    ): LocationInfoModel

    @GET("v4/direction/no-traffic")
    suspend fun getRoutingInformation(
        @Query("type") type: String = "car",
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("avoidTrafficZone") avoidTrafficZone: Boolean = false,
        @Query("avoidOddEvenZone") avoidOddEvenZone: Boolean = false,
    ): RoutingInfoModel
}