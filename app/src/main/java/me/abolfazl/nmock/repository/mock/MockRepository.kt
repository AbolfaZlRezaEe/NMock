package me.abolfazl.nmock.repository.mock

import kotlinx.coroutines.flow.Flow
import me.abolfazl.nmock.model.database.MockDatabaseType
import me.abolfazl.nmock.repository.mock.models.viewModels.MockDataClass
import me.abolfazl.nmock.utils.response.Response
import java.io.File

interface MockRepository {

    fun saveMockInformation(
        mockDataClass: MockDataClass,
    ): Flow<Response<Long, Int>>

    fun updateMockInformation(
        mockDataClass: MockDataClass,
    ): Flow<Response<Long, Int>>

    suspend fun deleteMock(
        @MockDatabaseType mockDatabaseType: String,
        mockId: Long,
    )

    suspend fun deleteAllMocks(
        @MockDatabaseType mockDatabaseType: String
    )

    fun getMocksInformation(
        @MockDatabaseType mockDatabaseType: String
    ): Flow<Response<List<MockDataClass>, Int>>

    fun getMockInformationFromId(
        @MockDatabaseType mockDatabaseType: String,
        id: Long
    ): Flow<Response<MockDataClass, Int>>

    fun createMockExportFile(
        @MockDatabaseType mockDatabaseType: String,
        id: Long
    ): Flow<Response<File, Int>>

    fun parseJsonDataModelString(
        json: String
    ): Flow<Response<MockDataClass, Int>>
}