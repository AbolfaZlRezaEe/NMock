package me.abolfazl.nmock.repository.locationInfo

import kotlinx.coroutines.flow.Flow
import me.abolfazl.nmock.repository.models.LocationInfoDataclass
import me.abolfazl.nmock.utils.response.Response
import me.abolfazl.nmock.utils.response.exceptions.NMockException

interface LocationInfoRepository {

    fun getLocationInformation(
        latitude: Double,
        longitude: Double
    ): Flow<Response<LocationInfoDataclass, NMockException>>

}