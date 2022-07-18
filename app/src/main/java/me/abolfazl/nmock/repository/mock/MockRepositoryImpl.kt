package me.abolfazl.nmock.repository.mock

import android.os.SystemClock
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.abolfazl.nmock.BuildConfig
import me.abolfazl.nmock.di.UtilsModule
import me.abolfazl.nmock.model.database.MockProvider
import me.abolfazl.nmock.model.database.MockType
import me.abolfazl.nmock.model.database.dao.MockDao
import me.abolfazl.nmock.model.database.dao.PositionDao
import me.abolfazl.nmock.model.database.models.MockEntity
import me.abolfazl.nmock.model.database.models.PositionEntity
import me.abolfazl.nmock.repository.mock.models.MockDataClass
import me.abolfazl.nmock.repository.mock.models.exportModels.LineExportJsonModel
import me.abolfazl.nmock.repository.mock.models.exportModels.MockExportJsonModel
import me.abolfazl.nmock.repository.mock.models.exportModels.MockInformationExportJsonModel
import me.abolfazl.nmock.repository.mock.models.exportModels.RouteInformationExportJsonModel
import me.abolfazl.nmock.utils.Constant
import me.abolfazl.nmock.utils.locationFormat
import me.abolfazl.nmock.utils.logger.NMockLogger
import me.abolfazl.nmock.utils.managers.FileManager
import me.abolfazl.nmock.utils.response.Failure
import me.abolfazl.nmock.utils.response.Response
import me.abolfazl.nmock.utils.response.Success
import org.neshan.common.model.LatLng
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class MockRepositoryImpl @Inject constructor(
    private val mockDao: MockDao,
    private val positionDao: PositionDao,
    private val logger: NMockLogger,
    @Named(UtilsModule.INJECT_STRING_ANDROID_ID)
    private val androidId: String,
    @Named(UtilsModule.INJECT_STRING_MAIN_DIRECTORY)
    private val mainDirectoryPath: String
) : MockRepository {

    companion object {
        const val DATABASE_INSERTION_EXCEPTION = 210
        const val DATABASE_EMPTY_LINE_EXCEPTION = 211
        const val LINE_VECTOR_NULL_EXCEPTION = 212
        const val CONVERT_MOCK_TO_JSON_EXCEPTION = 213
        const val CREATE_EXPORT_FILE_EXCEPTION = 214
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

    override suspend fun createMockExportFile(
        mockId: Long
    ): Flow<Response<File, Int>> = flow {
        val response = getMockAndPositionsInformationForExporting(mockId)
        if (response.second.isEmpty()) {
            logger.writeLog(value = "getMock was failed. position list is empty!")
            emit(Failure(DATABASE_EMPTY_LINE_EXCEPTION))
            return@flow
        }
        val mockExportJsonModel = toMockExportJsonModel(
            mockInformation = response.first,
            lineInformation = response.second
        )

        var finalJson: String? = null
        try {
            val moshi: Moshi = Moshi.Builder().build()
            val jsonAdapter: JsonAdapter<MockExportJsonModel> =
                moshi.adapter(MockExportJsonModel::class.java)
            finalJson = jsonAdapter.toJson(mockExportJsonModel)
        } catch (exception: Exception) {
            logger.writeLog(value = "we had problem on creating json from model: ${exception.message}")
            emit(Failure(CONVERT_MOCK_TO_JSON_EXCEPTION))
        }

        finalJson?.let {
            try {
                val file = FileManager.writeTextToFileWithPath(
                    mainDirectory = mainDirectoryPath,
                    directoryName = Constant.DIRECTORY_NAME_EXPORT_FILES,
                    fileName = mockExportJsonModel.mockInformation.name + Constant.EXPORT_MOCK_FILE_FORMAT,
                    text = finalJson
                )
                emit(Success(file))
            } catch (exception: Exception) {
                logger.writeLog(value = "we had a problem on creating export mock file: ${exception.message}")
                emit(Failure(CREATE_EXPORT_FILE_EXCEPTION))
            }
        }
    }

    private fun toMockExportJsonModel(
        mockInformation: MockEntity,
        lineInformation: List<PositionEntity>
    ): MockExportJsonModel {
        val mockInformationExportJsonModel = MockInformationExportJsonModel(
            type = mockInformation.type,
            name = mockInformation.name,
            description = mockInformation.description,
            originAddress = mockInformation.originAddress,
            destinationAddress = mockInformation.destinationAddress,
            speed = mockInformation.speed,
            bearing = mockInformation.bearing,
            accuracy = mockInformation.accuracy,
            provider = mockInformation.provider,
            createdAt = mockInformation.createdAt,
            updatedAt = mockInformation.updatedAt
        )
        val routeInformationExportJsonModel = RouteInformationExportJsonModel(
            originLocation = mockInformation.originLocation,
            destinationLocation = mockInformation.destinationLocation,
            routeLines = toLineExportJsonModel(lineInformation)
        )
        return MockExportJsonModel(
            fileCreatedAt = getTime(),
            fileOwner = androidId,
            versionCode = BuildConfig.VERSION_CODE,
            mockInformation = mockInformationExportJsonModel,
            routeInformation = routeInformationExportJsonModel
        )
    }

    private suspend fun getMockAndPositionsInformationForExporting(
        mockId: Long
    ): Pair<MockEntity, List<PositionEntity>> {
        val mockObject = mockDao.getMockFromId(mockId)
        val positionList = positionDao.getMockPositionListFromId(mockId)
        return mockObject to positionList
    }

    private fun toLineExportJsonModel(
        lineVectors: List<PositionEntity>
    ): List<LineExportJsonModel> {
        val result = mutableListOf<LineExportJsonModel>()
        lineVectors.forEach { positionEntity ->
            result.add(
                LineExportJsonModel(
                    id = positionEntity.id!!,
                    latitude = positionEntity.latitude,
                    longitude = positionEntity.longitude,
                    time = positionEntity.time,
                    elapsedRealTime = positionEntity.elapsedRealTime
                )
            )
        }
        return result
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