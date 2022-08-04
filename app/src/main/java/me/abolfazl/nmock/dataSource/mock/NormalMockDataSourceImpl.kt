package me.abolfazl.nmock.dataSource.mock

import me.abolfazl.nmock.model.database.mocks.normalMock.NormalMockDao
import me.abolfazl.nmock.model.database.mocks.normalMock.NormalMockEntity
import me.abolfazl.nmock.model.database.positions.normalPositions.NormalPositionDao
import me.abolfazl.nmock.model.database.positions.normalPositions.NormalPositionEntity
import javax.inject.Inject

class NormalMockDataSourceImpl @Inject constructor(
    private val normalMockDao: NormalMockDao,
    private val normalPositionDao: NormalPositionDao,
) : NormalMockDataSource {

    override suspend fun saveMockInformation(
        normalMockEntity: NormalMockEntity,
        normalPositionEntities: List<NormalPositionEntity>
    ): Long {
        val mockId = normalMockDao.insertMockInformation(normalMockEntity)

        if (mockId == -1L) return mockId

        normalPositionEntities.forEach { positionEntity ->
            normalPositionDao.insertMockPosition(positionEntity)
        }
        return mockId
    }

    override suspend fun updateMockInformation(
        normalMockEntity: NormalMockEntity,
        normalPositionEntities: List<NormalPositionEntity>
    ): Long {
        normalMockDao.updateMockInformation(normalMockEntity)

        normalPositionEntities.forEach { positionEntity ->
            normalPositionDao.updateMockPosition(positionEntity)
        }

        return normalMockEntity.id!!
    }

    override suspend fun deleteMock(id: Long) {
        normalMockDao.deleteMockEntity(id)
    }

    override suspend fun deleteAllMocks() {
        normalMockDao.deleteAllMocks()
    }

    override suspend fun getMocksInformation(): List<NormalMockEntity> {
        return normalMockDao.getAllMocks()
    }

    override suspend fun getMockInformationById(id: Long): NormalMockEntity? {
        return normalMockDao.getMockFromId(id)
    }

    override suspend fun getMockPositionInformationById(id: Long): List<NormalPositionEntity> {
        return normalPositionDao.getMockPositionListFromId(mockId = id)
    }
}