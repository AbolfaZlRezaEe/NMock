package me.abolfazl.nmock.repository.routingInfo

import kotlinx.coroutines.flow.Flow
import me.abolfazl.nmock.model.database.MockType
import me.abolfazl.nmock.model.database.models.MockEntity
import me.abolfazl.nmock.repository.models.routingInfo.RoutingInfoDataclass
import me.abolfazl.nmock.utils.response.exceptions.NMockException
import me.abolfazl.nmock.utils.response.Response

interface RoutingInfoRepository {

    suspend fun getRoutingInformation(
        origin: String,
        destination: String
    ): Flow<Response<RoutingInfoDataclass, NMockException>>

    suspend fun addMock(
        @MockType mockType: String,
        mockName:String,
        mockDescription:String? = null,
        routingInfoDataclass: RoutingInfoDataclass
    )

    suspend fun deleteMock(
        mockEntity: MockEntity
    )

    suspend fun updateMock(
        mockEntity: MockEntity
    )
}