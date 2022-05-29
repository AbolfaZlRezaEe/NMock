package me.abolfazl.nmock.repository.locationInfo

import kotlinx.coroutines.flow.Flow
import me.abolfazl.nmock.repository.models.LocationInfoDataclass
import me.abolfazl.nmock.utils.response.Response

interface LocationInfoRepository {

    fun getLocationInformation(
        latitude: Double,
        longitude: Double
    ): Flow<Response<LocationInfoDataclass, Int>>

}