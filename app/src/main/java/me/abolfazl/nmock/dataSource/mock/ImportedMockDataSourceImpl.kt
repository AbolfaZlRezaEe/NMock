package me.abolfazl.nmock.dataSource.mock

import me.abolfazl.nmock.model.database.mocks.importedMock.ImportedMockDao
import me.abolfazl.nmock.model.database.mocks.importedMock.ImportedMockEntity
import me.abolfazl.nmock.model.database.positions.importedPositions.ImportedPositionDao
import me.abolfazl.nmock.model.database.positions.importedPositions.ImportedPositionEntity
import javax.inject.Inject

class ImportedMockDataSourceImpl @Inject constructor(
    private val importedMockDao: ImportedMockDao,
    private val importedPositionDao: ImportedPositionDao,
) : ImportedMockDataSource {

    override suspend fun saveMockInformation(
        importedMockEntity: ImportedMockEntity,
    ): Long {
        return importedMockDao.insertImportedMock(importedMockEntity)
    }

    override suspend fun saveMockPositionsInformation(
        importedPositionEntities: List<ImportedPositionEntity>
    ) {
        importedPositionEntities.forEach { positionEntity ->
            importedPositionDao.insertImportedMockPosition(positionEntity)
        }
    }

    override suspend fun updateMockInformation(
        importedMockEntity: ImportedMockEntity,
        importedPositionEntities: List<ImportedPositionEntity>
    ): Long {
        importedMockDao.updateImportedMockInformation(importedMockEntity)

        importedPositionDao.deleteImportedRouteInformation(importedMockEntity.id!!)

        importedPositionEntities.forEach { positionEntity ->
            importedPositionDao.insertImportedMockPosition(positionEntity)
        }

        return importedMockEntity.id
    }

    override suspend fun deleteMock(id: Long) {
        importedMockDao.deleteImportedMockEntity(id)
    }

    override suspend fun deleteAllMocks() {
        importedMockDao.deleteAllImportedMocks()
    }

    override suspend fun getMockInformationById(id: Long): ImportedMockEntity? {
        return importedMockDao.getImportedMockFromId(id)
    }

    override suspend fun getMocksInformation(): List<ImportedMockEntity> {
        return importedMockDao.getAllImportedMocks()
    }

    override suspend fun getMockPositionInformationById(id: Long): List<ImportedPositionEntity> {
        return importedPositionDao.getImportedMockPositionListFromId(id)
    }
}