package me.abolfazl.nmock.dataSource.mock

import me.abolfazl.nmock.model.database.mocks.normalMock.NormalMockEntity
import me.abolfazl.nmock.model.database.positions.normalPositions.NormalPositionEntity

interface NormalMockDataSource {

    suspend fun saveMockInformation(
        normalMockEntity: NormalMockEntity,
    ): Long

    suspend fun saveMockPositionsInformation(
        normalPositionEntities: List<NormalPositionEntity>
    )

    suspend fun updateMockInformation(
        normalMockEntity: NormalMockEntity,
        normalPositionEntities: List<NormalPositionEntity>
    ): Long

    suspend fun deleteMock(id: Long)

    suspend fun deleteAllMocks()

    suspend fun getMocksInformation(): List<NormalMockEntity>

    suspend fun getMockInformationById(id: Long): NormalMockEntity?

    suspend fun getMockPositionInformationById(id: Long): List<NormalPositionEntity>
}