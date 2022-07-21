package me.abolfazl.nmock.repository.normalMock

import android.os.SystemClock
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.sentry.SentryLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.abolfazl.nmock.BuildConfig
import me.abolfazl.nmock.di.UtilsModule
import me.abolfazl.nmock.model.database.mocks.MockProvider
import me.abolfazl.nmock.model.database.mocks.MockType
import me.abolfazl.nmock.model.database.mocks.normalMock.NormalMockDao
import me.abolfazl.nmock.model.database.positions.normalPositions.NormalPositionDao
import me.abolfazl.nmock.model.database.mocks.normalMock.NormalMockEntity
import me.abolfazl.nmock.model.database.positions.normalPositions.NormalPositionEntity
import me.abolfazl.nmock.repository.normalMock.models.MockDataClass
import me.abolfazl.nmock.repository.normalMock.models.exportModels.LineExportJsonModel
import me.abolfazl.nmock.repository.normalMock.models.exportModels.MockExportJsonModel
import me.abolfazl.nmock.repository.normalMock.models.exportModels.MockInformationExportJsonModel
import me.abolfazl.nmock.repository.normalMock.models.exportModels.RouteInformationExportJsonModel
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

class NormalMockRepositoryImpl @Inject constructor(
    private val normalMockDao: NormalMockDao,
    private val normalPositionDao: NormalPositionDao,
    private val logger: NMockLogger,
    @Named(UtilsModule.INJECT_STRING_ANDROID_ID)
    private val androidId: String,
    @Named(UtilsModule.INJECT_STRING_MAIN_DIRECTORY)
    private val mainDirectoryPath: String
) : NormalMockRepository {

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
        val mockId = normalMockDao.insertMockInformation(
            NormalMockEntity(
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
            normalMockDao.updateMockInformation(
                NormalMockEntity(
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
            normalPositionDao.deleteRouteInformation(id)
            saveRoutingInformation(id, lineVector)
            emit(Success(id))
        }

    private suspend fun saveRoutingInformation(
        mockId: Long,
        lineVector: ArrayList<List<LatLng>>
    ) {
        lineVector.forEach { listOfLatLng ->
            listOfLatLng.forEach { latLng ->
                normalPositionDao.insertMockPosition(
                    NormalPositionEntity(
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
        return fromMockEntityList(normalMockDao.getAllMocks())
    }

    override suspend fun getMock(
        mockId: Long
    ): Flow<Response<MockDataClass, Int>> = flow {
        val mockObject = normalMockDao.getMockFromId(mockId)
        val positionList = normalPositionDao.getMockPositionListFromId(mockId)
        if (positionList.isEmpty()) {
            logger.writeLog(value = "getMock was failed. position list is empty!")
            emit(Failure(DATABASE_EMPTY_LINE_EXCEPTION))
            return@flow
        }
        emit(Success(fromMockEntity(mockObject, createLineVector(positionList))))
    }

    override suspend fun deleteAllMocks() {
        normalMockDao.deleteAllMocks()
    }

    override suspend fun deleteMock(id: Long?) {
        if (id == null) return
        normalMockDao.deleteMockEntity(id)
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
            logger.captureEventWithLogFile(
                fromRepository = true,
                exception = exception,
                sentryEventLevel = SentryLevel.ERROR
            )
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
                logger.captureEventWithLogFile(
                    fromRepository = true,
                    exception = exception,
                    sentryEventLevel = SentryLevel.ERROR
                )
                emit(Failure(CREATE_EXPORT_FILE_EXCEPTION))
            }
        }
    }

    private fun toMockExportJsonModel(
        mockInformation: NormalMockEntity,
        lineInformation: List<NormalPositionEntity>
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
    ): Pair<NormalMockEntity, List<NormalPositionEntity>> {
        val mockObject = normalMockDao.getMockFromId(mockId)
        val positionList = normalPositionDao.getMockPositionListFromId(mockId)
        return mockObject to positionList
    }

    private fun toLineExportJsonModel(
        lineVectors: List<NormalPositionEntity>
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
        normalMockEntity: NormalMockEntity,
        lineVector: ArrayList<List<LatLng>>? = null
    ): MockDataClass {
        return MockDataClass(
            id = normalMockEntity.id!!,
            name = normalMockEntity.name,
            description = normalMockEntity.description,
            type = normalMockEntity.type,
            originLocation = normalMockEntity.originLocation.locationFormat(),
            destinationLocation = normalMockEntity.destinationLocation.locationFormat(),
            originAddress = normalMockEntity.originAddress,
            destinationAddress = normalMockEntity.destinationAddress,
            speed = normalMockEntity.speed,
            lineVector = lineVector,
            bearing = normalMockEntity.bearing,
            accuracy = normalMockEntity.accuracy,
            provider = normalMockEntity.provider,
            createdAt = normalMockEntity.createdAt,
            updatedAt = normalMockEntity.updatedAt
        )
    }

    private fun createLineVector(
        positionList: List<NormalPositionEntity>
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
        list: List<NormalMockEntity>
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