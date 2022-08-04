package me.abolfazl.nmock.repository.mock.importedMock

import kotlinx.coroutines.flow.Flow
import me.abolfazl.nmock.model.database.mocks.MockProvider
import me.abolfazl.nmock.model.database.mocks.MockType
import me.abolfazl.nmock.repository.mock.models.MockDataClass
import me.abolfazl.nmock.repository.mock.models.MockImportedDataClass
import me.abolfazl.nmock.utils.response.Response
import org.neshan.common.model.LatLng

interface ImportedMockRepository {

    fun parseJsonDataString(
        json: String
    ): Flow<Response<MockImportedDataClass, Int>>

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
        fileCreatedAt: String,
        fileOwner: String,
        versionCode: Int
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
        createdAt: String,
        fileCreatedAt: String,
        fileOwner: String,
        versionCode: Int
    ): Flow<Response<Long, Int>>

    suspend fun deleteMock(id: Long?)

    suspend fun deleteAllMocks()

    suspend fun getMocks(): List<MockImportedDataClass>

    suspend fun getMock(
        mockId: Long
    ): Flow<Response<MockImportedDataClass, Int>>
}