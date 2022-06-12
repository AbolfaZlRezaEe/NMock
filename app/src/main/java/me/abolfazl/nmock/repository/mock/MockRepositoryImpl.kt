package me.abolfazl.nmock.repository.mock

import android.os.SystemClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.abolfazl.nmock.model.database.MockProvider
import me.abolfazl.nmock.model.database.MockType
import me.abolfazl.nmock.model.database.dao.MockDao
import me.abolfazl.nmock.model.database.dao.PositionDao
import me.abolfazl.nmock.model.database.models.MockEntity
import me.abolfazl.nmock.model.database.models.PositionEntity
import me.abolfazl.nmock.repository.models.MockDataClass
import me.abolfazl.nmock.utils.locationFormat
import me.abolfazl.nmock.utils.logger.NMockLogger
import me.abolfazl.nmock.utils.response.Failure
import me.abolfazl.nmock.utils.response.Response
import me.abolfazl.nmock.utils.response.Success
import org.neshan.common.model.LatLng
import java.util.*
import javax.inject.Inject

class MockRepositoryImpl @Inject constructor(
    private val mockDao: MockDao,
    private val positionDao: PositionDao,
    private val logger: NMockLogger
) : MockRepository {

    companion object {
        const val DATABASE_INSERTION_EXCEPTION = 210
        const val DATABASE_EMPTY_LINE_EXCEPTION = 211
        const val LINE_VECTOR_NULL_EXCEPTION = 212
    }

    init {
        logger.disableLogHeaderForThisClass()
        logger.setClassInformationForEveryLog(javaClass.simpleName)
    }

    override fun saveMockInformation(
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
    ): Flow<Response<Long, Int>> = flow {
        // check the lineVector doesn't null!
        if (lineVector == null) {
            logger.writeLog(value = "saveMockInformation was failed. lineVector is null!")
            emit(Failure(LINE_VECTOR_NULL_EXCEPTION))
            return@flow
        }
        val mockId = mockDao.insertMockInformation(
            MockEntity(
                id = null,
                type = type,
                name = name,
                description = description,
                originLocation = originLocation.locationFormat(),
                destinationLocation = destinationLocation.locationFormat(),
                originAddress = originAddress,
                destinationAddress = destinationAddress,
                accuracy = accuracy,
                bearing = bearing,
                speed = speed,
                createdAt = getTime(),
                updatedAt = getTime(),
                provider = provider,
            )
        )
        if (mockId == -1L) {
            logger.writeLog(value = "saveMockInformation was failed. mockId was -1!")
            emit(Failure(DATABASE_INSERTION_EXCEPTION))
            return@flow
        }
        saveRoutingInformation(mockId, lineVector)
        emit(Success(mockId))
    }

    override fun updateMockInformation(
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
    ): Flow<Response<Long, Int>> =
        flow {
            // check the lineVector doesn't null!
            if (lineVector == null) {
                logger.writeLog(value = "updateMockInformation was failed. lineVector is null!")
                emit(Failure(LINE_VECTOR_NULL_EXCEPTION))
                return@flow
            }
            mockDao.updateMockInformation(
                MockEntity(
                    id = id,
                    type = type,
                    name = name,
                    description = description,
                    originLocation = originLocation.locationFormat(),
                    destinationLocation = destinationLocation.locationFormat(),
                    originAddress = originAddress,
                    destinationAddress = destinationAddress,
                    accuracy = accuracy,
                    bearing = bearing,
                    speed = speed,
                    createdAt = createdAt,
                    updatedAt = getTime(),
                    provider = provider
                )
            )
            positionDao.deleteRouteInformation(id)
            saveRoutingInformation(id, lineVector)
            emit(Success(id))
        }

    private suspend fun saveRoutingInformation(
        mockId: Long,
        lineVector: ArrayList<List<LatLng>>
    ) {
        lineVector.forEach { listOfLatLng ->
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
    }

    override suspend fun getMocks(): List<MockDataClass> {
        return fromMockEntityList(mockDao.getAllMocks())
    }

    override suspend fun getMock(
        mockId: Long
    ): Flow<Response<MockDataClass, Int>> = flow {
        val mockObject = mockDao.getMockFromId(mockId)
        val positionList = positionDao.getMockPositionListFromId(mockId)
        if (positionList.isEmpty()) {
            logger.writeLog(value = "getMock was failed. position list is empty!")
            emit(Failure(DATABASE_EMPTY_LINE_EXCEPTION))
            return@flow
        }
        emit(Success(fromMockEntity(mockObject, createLineVector(positionList))))
    }

    override suspend fun deleteAllMocks() {
        mockDao.deleteAllMocks()
    }

    override suspend fun deleteMock(id: Long?) {
        if (id == null) return
        mockDao.deleteMockEntity(id)
    }

    private fun fromMockEntity(
        mockEntity: MockEntity,
        lineVector: ArrayList<List<LatLng>>? = null
    ): MockDataClass {
        return MockDataClass(
            id = mockEntity.id!!,
            name = mockEntity.name,
            description = mockEntity.description,
            type = mockEntity.type,
            originLocation = mockEntity.originLocation.locationFormat(),
            destinationLocation = mockEntity.destinationLocation.locationFormat(),
            originAddress = mockEntity.originAddress,
            destinationAddress = mockEntity.destinationAddress,
            speed = mockEntity.speed,
            lineVector = lineVector,
            bearing = mockEntity.bearing,
            accuracy = mockEntity.accuracy,
            provider = mockEntity.provider,
            createdAt = mockEntity.createdAt,
            updatedAt = mockEntity.updatedAt
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

    private fun getTime(): String {
        return Calendar.getInstance().time.toString()
    }
}