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
import kotlin.text.StringBuilder

class MockRepositoryImpl @Inject constructor(
    private val mockDao: MockDao,
    private val positionDao: PositionDao,
) : MockRepository {

    override fun saveMock(
        mockDataClass: MockDataClass
    ): Flow<Response<Boolean, NMockException>> = flow {
        // when we want to update a mock:
        mockDataClass.id?.let {
            mockDao.updateMockInformation(
                toMockEntity(
                    mockDataClass = mockDataClass,
                    updatedAt = getTime()
                )
            )
            emit(Success(true))
            return@flow
        }

        // when we want to insert a mock:
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

    override suspend fun deleteAllMocks() {
        mockDao.deleteAllMocks()
    }

    override suspend fun deleteMock(
        mockDataClass: MockDataClass
    ) {
        mockDao.deleteMockEntity(mockDataClass.id!!)
    }

    private fun toMockEntity(
        mockDataClass: MockDataClass,
        updatedAt: String? = null
    ): MockEntity {
        return MockEntity(
            mockType = mockDataClass.mockType,
            mockName = mockDataClass.mockName,
            description = mockDataClass.mockDescription,
            originLocation = fromLatLng(mockDataClass.originLocation),
            destinationLocation = fromLatLng(mockDataClass.destinationLocation),
            originAddress = mockDataClass.originAddress,
            destinationAddress = mockDataClass.destinationAddress,
            accuracy = mockDataClass.accuracy,
            bearing = mockDataClass.bearing,
            speed = mockDataClass.speed,
            createdAt = getTime(),
            updatedAt = updatedAt ?: getTime(),
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
            originLocation = toLatLang(mockEntity.originLocation),
            destinationLocation = toLatLang(mockEntity.destinationLocation),
            originAddress = mockEntity.originAddress,
            destinationAddress = mockEntity.destinationAddress,
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

    private fun toLatLang(
        rawLocation: String
    ): LatLng {
        val origin = StringBuilder()
        var destination: String? = null
        run operation@{
            rawLocation.forEachIndexed { index, character ->
                if (character == ',') {
                    destination = rawLocation.substring(index + 1)
                    return@operation
                }
                origin.append(character)
            }
        }
        return LatLng(origin.toString().toDouble(), destination.toString().toDouble())
    }

    private fun fromLatLng(
        latLng: LatLng
    ): String {
        return "${latLng.latitude},${latLng.longitude}"
    }

    private fun getTime(): String {
        return Calendar.getInstance().time.toString()
    }
}