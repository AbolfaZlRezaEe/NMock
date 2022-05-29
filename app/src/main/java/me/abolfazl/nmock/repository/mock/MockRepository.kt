package me.abolfazl.nmock.repository.mock

import kotlinx.coroutines.flow.Flow
import me.abolfazl.nmock.repository.models.MockDataClass
import me.abolfazl.nmock.utils.response.Response

interface MockRepository {

    fun saveMock(
        mockDataClass: MockDataClass
    ): Flow<Response<Long, Int>>

    suspend fun deleteMock(
        mockDataClass: MockDataClass
    )

    suspend fun deleteAllMocks()

    suspend fun getMocks(): List<MockDataClass>

    suspend fun getMock(
        mockId: Long
    ): Flow<Response<MockDataClass, Int>>
}