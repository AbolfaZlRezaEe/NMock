package me.abolfazl.nmock.repository.mock.importedMock

import android.os.SystemClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.abolfazl.nmock.model.database.mocks.importedMock.ImportedMockDao
import me.abolfazl.nmock.model.database.mocks.importedMock.ImportedMockEntity
import me.abolfazl.nmock.model.database.positions.importedPositions.ImportedPositionDao
import me.abolfazl.nmock.model.database.positions.importedPositions.ImportedPositionEntity
import me.abolfazl.nmock.repository.mock.models.MockDataClass
import me.abolfazl.nmock.utils.locationFormat
import me.abolfazl.nmock.utils.logger.NMockLogger
import me.abolfazl.nmock.utils.response.Failure
import me.abolfazl.nmock.utils.response.Response
import me.abolfazl.nmock.utils.response.Success
import org.neshan.common.model.LatLng
import java.util.*
import javax.inject.Inject

class ImportedMockRepositoryImpl @Inject constructor(
    private val importedMockDao: ImportedMockDao,
    private val importedPositionDao: ImportedPositionDao,
    private val logger: NMockLogger
) : ImportedMockRepository {

    companion object {
        const val DATABASE_INSERTION_EXCEPTION = 410
        const val DATABASE_EMPTY_LINE_EXCEPTION = 411
        const val LINE_VECTOR_NULL_EXCEPTION = 412
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
        type: String,
        speed: Int,
        lineVector: ArrayList<List<LatLng>>?,
        bearing: Float,
        accuracy: Float,
        provider: String,
        fileCreatedAt: String,
        fileOwner: String,
        versionCode: Int
    ): Flow<Response<Long, Int>> = flow {
        // check the lineVector doesn't null!
        if (lineVector == null) {
            logger.writeLog(value = "saveImportedMockInformation was failed. lineVector is null!")
            emit(Failure(LINE_VECTOR_NULL_EXCEPTION))
            return@flow
        }
        val mockId = importedMockDao.insertImportedMock(
            ImportedMockEntity(
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
                fileCreatedAt = fileCreatedAt,
                fileOwner = fileOwner,
                versionCode = versionCode
            )
        )
        if (mockId == -1L) {
            logger.writeLog(value = "saveImportedMockInformation was failed. mockId was -1!")
            emit(Failure(DATABASE_INSERTION_EXCEPTION))
            return@flow
        }
        saveImportedRoutingInformation(mockId, lineVector)
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
        type: String,
        speed: Int,
        lineVector: ArrayList<List<LatLng>>?,
        bearing: Float,
        accuracy: Float,
        provider: String,
        createdAt: String,
        fileCreatedAt: String,
        fileOwner: String,
        versionCode: Int
    ): Flow<Response<Long, Int>> = flow {
        // check the lineVector doesn't null!
        if (lineVector == null) {
            logger.writeLog(value = "updateImportedMockInformation was failed. lineVector is null!")
            emit(Failure(LINE_VECTOR_NULL_EXCEPTION))
            return@flow
        }
        importedMockDao.updateImportedMockInformation(
            ImportedMockEntity(
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
                provider = provider,
                fileCreatedAt = fileCreatedAt,
                fileOwner = fileOwner,
                versionCode = versionCode
            )
        )
        importedPositionDao.deleteImportedRouteInformation(id)
        saveImportedRoutingInformation(id, lineVector)
        emit(Success(id))
    }

    override suspend fun deleteMock(id: Long?) {
        if (id == null) return
        importedMockDao.deleteImportedMockEntity(id)
    }

    override suspend fun deleteAllMocks() {
        importedMockDao.deleteAllImportedMocks()
    }

    override suspend fun getMocks(): List<MockDataClass> {
        return fromMockEntityList(importedMockDao.getAllImportedMocks())
    }

    override suspend fun getMock(mockId: Long): Flow<Response<MockDataClass, Int>> = flow {
        val mockObject = importedMockDao.getImportedMockFromId(mockId)
        val positionList = importedPositionDao.getImportedMockPositionListFromId(mockId)
        if (positionList.isEmpty()) {
            logger.writeLog(value = "getImportedMock was failed. position list is empty!")
            emit(Failure(DATABASE_EMPTY_LINE_EXCEPTION))
            return@flow
        }
        emit(Success(fromMockEntity(mockObject, createLineVector(positionList))))
    }

    private suspend fun saveImportedRoutingInformation(
        mockId: Long,
        lineVector: ArrayList<List<LatLng>>
    ) {
        lineVector.forEach { listOfLatLng ->
            listOfLatLng.forEach { latLng ->
                importedPositionDao.insertImportedMockPosition(
                    ImportedPositionEntity(
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

    private fun createLineVector(
        positionList: List<ImportedPositionEntity>
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
        list: List<ImportedMockEntity>
    ): List<MockDataClass> {
        val result = mutableListOf<MockDataClass>()
        list.forEach { mockEntity ->
            result.add(fromMockEntity(mockEntity))
        }
        return result
    }

    private fun fromMockEntity(
        importedMockEntity: ImportedMockEntity,
        lineVector: ArrayList<List<LatLng>>? = null
    ): MockDataClass {
        return MockDataClass(
            id = importedMockEntity.id!!,
            name = importedMockEntity.name,
            description = importedMockEntity.description,
            type = importedMockEntity.type,
            originLocation = importedMockEntity.originLocation.locationFormat(),
            destinationLocation = importedMockEntity.destinationLocation.locationFormat(),
            originAddress = importedMockEntity.originAddress,
            destinationAddress = importedMockEntity.destinationAddress,
            speed = importedMockEntity.speed,
            lineVector = lineVector,
            bearing = importedMockEntity.bearing,
            accuracy = importedMockEntity.accuracy,
            provider = importedMockEntity.provider,
            createdAt = importedMockEntity.createdAt,
            updatedAt = importedMockEntity.updatedAt,
            fileCreatedAt = importedMockEntity.fileCreatedAt,
            fileOwner = importedMockEntity.fileOwner,
            versionCode = importedMockEntity.versionCode,
        )
    }

    private fun getTime(): String {
        return Calendar.getInstance().time.toString()
    }
}