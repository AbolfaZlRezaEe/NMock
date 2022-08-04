package me.abolfazl.nmock.dataSource.mock

import me.abolfazl.nmock.model.database.mocks.importedMock.ImportedMockEntity
import me.abolfazl.nmock.model.database.positions.importedPositions.ImportedPositionEntity

interface ImportedMockDataSource {

    suspend fun saveMockInformation(
        importedMockEntity: ImportedMockEntity,
        importedPositionEntities: List<ImportedPositionEntity>
    ): Long

    suspend fun updateMockInformation(
        importedMockEntity: ImportedMockEntity,
        importedPositionEntities: List<ImportedPositionEntity>
    ): Long

    suspend fun deleteMock(id: Long)

    suspend fun deleteAllMocks()

    suspend fun getMocksInformation(): List<ImportedMockEntity>

    suspend fun getMockInformationById(id: Long): ImportedMockEntity?

    suspend fun getMockPositionInformationById(id: Long): List<ImportedPositionEntity>
}