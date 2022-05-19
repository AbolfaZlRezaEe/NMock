package me.abolfazl.nmock.repository.routingInfo

import kotlinx.coroutines.flow.Flow
import me.abolfazl.nmock.repository.models.routingInfo.RoutingInfoDataclass
import me.abolfazl.nmock.utils.response.Response
import me.abolfazl.nmock.utils.response.exceptions.NMockException

interface RoutingInfoRepository {

    suspend fun getRoutingInformation(
        origin: String,
        destination: String
    ): Flow<Response<RoutingInfoDataclass, NMockException>>
}