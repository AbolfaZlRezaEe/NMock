package me.abolfazl.nmock.repository.mock

import android.os.SystemClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.abolfazl.nmock.model.database.dao.MockDao
import me.abolfazl.nmock.model.database.dao.PositionDao
import me.abolfazl.nmock.model.database.models.MockEntity
import me.abolfazl.nmock.model.database.models.PositionEntity
import me.abolfazl.nmock.repository.models.MockDataClass
import me.abolfazl.nmock.utils.response.Failure
import me.abolfazl.nmock.utils.response.Response
import me.abolfazl.nmock.utils.response.Success
import me.abolfazl.nmock.utils.response.exceptions.EXCEPTION_DATABASE_GETTING_ERROR
import me.abolfazl.nmock.utils.response.exceptions.EXCEPTION_INSERTION_ERROR
import me.abolfazl.nmock.utils.response.exceptions.NMockException
import org.neshan.common.model.LatLng
import java.util.*
import javax.inject.Inject

class MockRepositoryImpl @Inject constructor(
    private val mockDao: MockDao,
    private val positionDao: PositionDao,
) : MockRepository {

    override fun addMock(
        mockDataClass: MockDataClass
    ): Flow<Response<Boolean, NMockException>> = flow {
        val mockId = mockDao.insertMockInformation(
            toMockEntity(mockDataClass)
        )
        if (mockId == -1L) {
            emit(Failure(NMockException(type = EXCEPTION_INSERTION_ERROR)))
            return@flow
        }
        mockDataClass.lineVector?.forEach { listOfLatLng ->
            listOfLatLng.forEach { latLng ->
                positionDao.insertMockPosition(
                    PositionEntity(
                        mockId = mockId,
                        latitude = latLng.latitude,
                        longitude = latLng.longitude,
                        time = System.currentTimeMillis(),
                        elapsedRealTime = SystemClock.elapsedRealtimeNanos(),
                    )
                )
            }
        }
        emit(Success(true))
    }

    override suspend fun getMocks(): Flow<Response<List<MockDataClass>, NMockException>> = flow {
        val mockList = mockDao.getAllMocks()
        emit(Success(fromMockEntityList(mockList)))
    }

    override suspend fun getMock(
        mockId: Long
    ): Flow<Response<MockDataClass, NMockException>> = flow {
        val mockObject = mockDao.getMockFromId(mockId)
        val positionList = positionDao.getMockPositionListFromId(mockId)
        if (positionList.isEmpty()) {
            emit(Failure(NMockException(type = EXCEPTION_DATABASE_GETTING_ERROR)))
            return@flow
        }
        emit(Success(fromMockEntity(mockObject, createLineVector(positionList))))
    }

    override suspend fun deleteMock(mockEntity: MockEntity) {
        TODO("Not yet implemented")
    }

    override suspend fun updateMock(mockEntity: MockEntity) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAllMocks() {
        mockDao.deleteAllMocks()
    }

    private fun toMockEntity(
        mockDataClass: MockDataClass,
        updatedAt: String? = null
    ): MockEntity {
        return MockEntity(
            mockType = mockDataClass.mockType,
            mockName = mockDataClass.mockName,
            description = mockDataClass.mockDescription,
            accuracy = mockDataClass.accuracy,
            bearing = mockDataClass.bearing,
            speed = mockDataClass.speed,
            createdAt = Calendar.getInstance().time.toString(),
            updatedAt = updatedAt ?: Calendar.getInstance().time.toString(),
            provider = mockDataClass.provider
        )
    }

    private fun fromMockEntity(
        mockEntity: MockEntity,
        lineVector: ArrayList<List<LatLng>>? = null
    ): MockDataClass {
        return MockDataClass(
            id = mockEntity.id!!,
            mockName = mockEntity.mockName,
            mockDescription = mockEntity.description,
            mockType = mockEntity.mockType,
            speed = mockEntity.speed,
            lineVector = lineVector,
            bearing = mockEntity.bearing,
            accuracy = mockEntity.accuracy,
            provider = mockEntity.provider
        )
    }

    private fun createLineVector(
        positionList: List<PositionEntity>
    ): ArrayList<List<LatLng>> {
        val result = ArrayList<List<LatLng>>()
        val list = mutableListOf<LatLng>()
        positionList.forEach { positionEntity ->
            list.add(LatLng(positionEntity.latitude, positionEntity.longitude))
        }
        result.add(list)
        return result
    }

    private fun fromMockEntityList(
        list: List<MockEntity>
    ): List<MockDataClass> {
        val result = mutableListOf<MockDataClass>()
        list.forEach { mockEntity ->
            result.add(fromMockEntity(mockEntity))
        }
        return result
    }
}