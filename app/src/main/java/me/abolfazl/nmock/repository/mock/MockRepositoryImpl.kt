package me.abolfazl.nmock.repository.mock

import android.os.SystemClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.abolfazl.nmock.model.database.dao.MockDao
import me.abolfazl.nmock.model.database.dao.PositionDao
import me.abolfazl.nmock.model.database.models.MockEntity
import me.abolfazl.nmock.model.database.models.PositionEntity
import me.abolfazl.nmock.repository.models.MockDataClass
import me.abolfazl.nmock.repository.models.routingInfo.RoutingInfoDataclass
import me.abolfazl.nmock.utils.Constant
import me.abolfazl.nmock.utils.response.Failure
import me.abolfazl.nmock.utils.response.Response
import me.abolfazl.nmock.utils.response.Success
import me.abolfazl.nmock.utils.response.exceptions.EXCEPTION_INSERTION_ERROR
import me.abolfazl.nmock.utils.response.exceptions.NMockException
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
        mockDataClass.lineVector.forEach { listOfLatLng ->
            listOfLatLng.forEach { latLng ->
                positionDao.insertMockPosition(
                    PositionEntity(
                        mockId = mockId,
                        latitude = latLng.latitude,
                        longitude = latLng.longitude,
                        // for now...
                        speed = Constant.DEFAULT_MOCK_SPEED,
                        time = System.currentTimeMillis(),
                        elapsedRealTime = SystemClock.elapsedRealtimeNanos(),
                        bearing = 0F,
                        accuracy = 1F,
                        // for now...
                        provider = Constant.PROVIDER_GPS
                    )
                )
            }
        }
        emit(Success(true))
    }

    override suspend fun deleteMock(mockEntity: MockEntity) {
        TODO("Not yet implemented")
    }

    override suspend fun updateMock(mockEntity: MockEntity) {
        TODO("Not yet implemented")
    }

    private fun toMockEntity(
        mockDataClass: MockDataClass,
        updatedAt: String? = null
    ): MockEntity {
        return MockEntity(
            mockType = mockDataClass.mockType,
            mockName = mockDataClass.mockName,
            description = mockDataClass.mockDescription,
            createdAt = Calendar.getInstance().time.toString(),
            updatedAt = updatedAt ?: Calendar.getInstance().time.toString()
        )
    }
}