package me.abolfazl.nmock.repository.mock.normalMock

import kotlinx.coroutines.flow.Flow
import me.abolfazl.nmock.model.database.mocks.MockProvider
import me.abolfazl.nmock.model.database.mocks.MockType
import me.abolfazl.nmock.repository.mock.models.MockDataClass
import me.abolfazl.nmock.utils.response.Response
import org.neshan.common.model.LatLng
import java.io.File

interface NormalMockRepository {

    fun saveMockInformation(
        name: String,
        description: String,
        originLocation: LatLng,
        destinationLocation: LatLng,
        originAddress: String?,
        destinationAddress: String?,
        @MockType type: String,
        speed: Int,
        lineVector: ArrayList<List<LatLng>>?,
        bearing: Float,
        accuracy: Float,
        @MockProvider provider: String,
    ): Flow<Response<Long, Int>>

    fun updateMockInformation(
        id: Long,
        name: String,
        description: String,
        originLocation: LatLng,
        destinationLocation: LatLng,
        originAddress: String?,
        destinationAddress: String?,
        @MockType type: String,
        speed: Int,
        lineVector: ArrayList<List<LatLng>>?,
        bearing: Float,
        accuracy: Float,
        @MockProvider provider: String,
        createdAt: String
    ): Flow<Response<Long, Int>>

    suspend fun deleteMock(id: Long?)

    suspend fun deleteAllMocks()

    suspend fun getMocks(): List<MockDataClass>

    suspend fun getMock(
        mockId: Long
    ): Flow<Response<MockDataClass, Int>>

    suspend fun createMockExportFile(
        mockId: Long
    ): Flow<Response<File, Int>>
}