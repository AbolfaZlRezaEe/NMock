package me.abolfazl.nmock.repository.mock

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import io.sentry.SentryLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.abolfazl.nmock.BuildConfig
import me.abolfazl.nmock.dataSource.mock.ImportedMockDataSource
import me.abolfazl.nmock.dataSource.mock.NormalMockDataSource
import me.abolfazl.nmock.di.UtilsModule
import me.abolfazl.nmock.model.database.DATABASE_TYPE_ALL
import me.abolfazl.nmock.model.database.DATABASE_TYPE_IMPORTED
import me.abolfazl.nmock.model.database.DATABASE_TYPE_NORMAL
import me.abolfazl.nmock.repository.mock.models.exportModels.MockExportJsonModel
import me.abolfazl.nmock.repository.mock.models.viewModels.MockDataClass
import me.abolfazl.nmock.utils.Constant
import me.abolfazl.nmock.utils.logger.NMockLogger
import me.abolfazl.nmock.utils.managers.FileManager
import me.abolfazl.nmock.utils.response.*
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class MockRepositoryImpl @Inject constructor(
    private val normalMockDataSource: NormalMockDataSource,
    private val importedMockDataSource: ImportedMockDataSource,
    @Named(UtilsModule.INJECT_STRING_ANDROID_ID)
    private val androidId: String,
    @Named(UtilsModule.INJECT_STRING_MAIN_DIRECTORY)
    private val mainDirectoryPath: String,
    private val logger: NMockLogger,
) : MockRepository {

    companion object {
        const val DATABASE_INSERTION_EXCEPTION = 210
        const val DATABASE_EMPTY_LINE_EXCEPTION = 211
        const val DATABASE_EMPTY_MOCK_INFORMATION = 216
        const val LINE_VECTOR_NULL_EXCEPTION = 212
        const val CONVERT_MOCK_TO_JSON_EXCEPTION = 213
        const val CREATE_EXPORT_FILE_EXCEPTION = 214
        const val DATABASE_WRONG_TYPE_EXCEPTION = 215
        const val JSON_PROCESS_FAILED_EXCEPTION = 217
        const val JSON_PROBLEM_EXCEPTION = 218
    }

    override fun saveMockInformation(
        mockDataClass: MockDataClass
    ): Flow<Response<Long, Int>> = flow {
        if (mockDataClass.lineVector == null) {
            logger.writeLog(value = "saveMockInformation was failed. lineVector is null!")
            emit(Failure(LINE_VECTOR_NULL_EXCEPTION))
            return@flow
        }
        val mockInformation = updateCreateAndUpdateTimeOfMock(mockDataClass)
        when (mockInformation.mockDatabaseType) {
            DATABASE_TYPE_NORMAL -> {
                val mockId = normalMockDataSource.saveMockInformation(
                    normalMockEntity = MockRepositoryTypeConverter.toNormalMockEntity(
                        mockInformation
                    ),
                )
                if (mockId == -1L) {
                    logger.writeLog(value = "saveMockInformation was failed(NormalMockSaving). mockId was -1!")
                    emit(Failure(DATABASE_INSERTION_EXCEPTION))
                    return@flow
                }
                mockInformation.id = mockId
                normalMockDataSource.saveMockPositionsInformation(
                    normalPositionEntities = MockRepositoryTypeConverter.toNormalPositionEntity(
                        mockInformation
                    )
                )
                logger.writeLog(value = "saving normal mock information was successful!")
                emit(Success(mockId))
            }
            DATABASE_TYPE_IMPORTED -> {
                val mockId = importedMockDataSource.saveMockInformation(
                    importedMockEntity = MockRepositoryTypeConverter.toImportedMockEntity(
                        mockInformation
                    ),
                )
                if (mockId == -1L) {
                    logger.writeLog(value = "saveMockInformation was failed(ImportedMockSaving). mockId was -1!")
                    emit(Failure(DATABASE_INSERTION_EXCEPTION))
                    return@flow
                }
                mockInformation.id = mockId
                importedMockDataSource.saveMockPositionsInformation(
                    importedPositionEntities = MockRepositoryTypeConverter.toImportedPositionEntity(
                        mockInformation
                    )
                )
                logger.writeLog(value = "saving imported mock information was successful!")
                emit(Success(mockId))
            }
            else -> {
                logger.writeLog(value = "MockDatabaseType is wrong?! type-> ${mockInformation.mockDatabaseType}")
                emit(Failure(DATABASE_WRONG_TYPE_EXCEPTION))
                if (BuildConfig.DEBUG) {
                    throw IllegalStateException("MockDatabaseType is wrong?! type-> $mockInformation.mockDatabaseType")
                }
            }
        }
    }

    override fun updateMockInformation(
        mockDataClass: MockDataClass
    ): Flow<Response<Long, Int>> = flow {
        if (mockDataClass.lineVector == null) {
            logger.writeLog(value = "updateMockInformation was failed. lineVector is null!")
            emit(Failure(LINE_VECTOR_NULL_EXCEPTION))
            return@flow
        }
        val mockInformation = updateCreateAndUpdateTimeOfMock(mockDataClass)
        when (mockInformation.mockDatabaseType) {
            DATABASE_TYPE_NORMAL -> {
                val mockId = normalMockDataSource.updateMockInformation(
                    normalMockEntity = MockRepositoryTypeConverter.toNormalMockEntity(
                        mockInformation
                    ),
                    normalPositionEntities = MockRepositoryTypeConverter.toNormalPositionEntity(
                        mockInformation
                    )
                )
                logger.writeLog(value = "updating normal mock information was successful!")
                emit(Success(mockId))
            }
            DATABASE_TYPE_IMPORTED -> {
                val mockId = importedMockDataSource.updateMockInformation(
                    importedMockEntity = MockRepositoryTypeConverter.toImportedMockEntity(
                        mockInformation
                    ),
                    importedPositionEntities = MockRepositoryTypeConverter.toImportedPositionEntity(
                        mockInformation
                    )
                )
                logger.writeLog(value = "updating imported mock information was successful!")
                emit(Success(mockId))
            }
            else -> {
                logger.writeLog(value = "MockDatabaseType is wrong?! type-> ${mockInformation.mockDatabaseType}")
                emit(Failure(DATABASE_WRONG_TYPE_EXCEPTION))
                if (BuildConfig.DEBUG) {
                    throw IllegalStateException("MockDatabaseType is wrong?! type-> $mockInformation.mockDatabaseType")
                }
            }
        }
    }

    override suspend fun deleteMock(
        mockDatabaseType: String,
        mockId: Long,
    ) {
        when (mockDatabaseType) {
            DATABASE_TYPE_NORMAL -> {
                normalMockDataSource.deleteMock(mockId)
                logger.writeLog(value = "We deleted normal mock information with mockId:$mockId")
            }
            DATABASE_TYPE_IMPORTED -> {
                importedMockDataSource.deleteMock(mockId)
                logger.writeLog(value = "We deleted imported mock information with mockId:$mockId")
            }
            else -> {
                logger.writeLog(value = "MockDatabaseType is wrong?! type-> ${mockDatabaseType}")
                if (BuildConfig.DEBUG) {
                    throw IllegalStateException("MockDatabaseType is wrong?! type-> $mockDatabaseType")
                }
            }
        }
    }

    override suspend fun deleteAllMocks(
        mockDatabaseType: String
    ) {
        when (mockDatabaseType) {
            DATABASE_TYPE_ALL -> {
                normalMockDataSource.deleteAllMocks()
                importedMockDataSource.deleteAllMocks()
                logger.writeLog(value = "We deleted all mock information!")
            }
            DATABASE_TYPE_NORMAL -> {
                normalMockDataSource.deleteAllMocks()
                logger.writeLog(value = "We deleted normal mock information!")
            }
            DATABASE_TYPE_IMPORTED -> {
                importedMockDataSource.deleteAllMocks()
                logger.writeLog(value = "We deleted imported mock information!")
            }
            else -> {
                logger.writeLog(value = "MockDatabaseType is wrong?! type-> $mockDatabaseType")
                if (BuildConfig.DEBUG) {
                    throw IllegalStateException("MockDatabaseType is wrong?! type-> $mockDatabaseType")
                }
            }
        }
    }

    override fun getMocksInformation(
        mockDatabaseType: String
    ): Flow<Response<List<MockDataClass>, Int>> = flow {
        when (mockDatabaseType) {
            DATABASE_TYPE_ALL -> {
                logger.writeLog(value = "We are going to collect all mock information for user.")
                val normalMockResponse = normalMockDataSource.getMocksInformation()
                val importedMockResponse = importedMockDataSource.getMocksInformation()
                emit(Success(mutableListOf<MockDataClass>().apply {
                    normalMockResponse.forEach { normalMockEntity ->
                        add(MockRepositoryTypeConverter.fromNormalMockEntity(normalMockEntity))
                    }
                    importedMockResponse.forEach { importedMockEntity ->
                        add(MockRepositoryTypeConverter.fromImportedMockEntity(importedMockEntity))
                    }
                }))
            }
            DATABASE_TYPE_NORMAL -> {
                logger.writeLog(value = "We are going to collect normal mock information for user.")
                val response = normalMockDataSource.getMocksInformation()
                val result: List<MockDataClass> = mutableListOf<MockDataClass>().apply {
                    response.forEach { normalMockEntity ->
                        add(MockRepositoryTypeConverter.fromNormalMockEntity(normalMockEntity))
                    }
                }
                emit(Success(result))
            }
            DATABASE_TYPE_IMPORTED -> {
                logger.writeLog(value = "We are going to collect imported mock information for user.")
                val response = importedMockDataSource.getMocksInformation()
                val result: List<MockDataClass> = mutableListOf<MockDataClass>().apply {
                    response.forEach { importedMockEntity ->
                        add(MockRepositoryTypeConverter.fromImportedMockEntity(importedMockEntity))
                    }
                }
                emit(Success(result))
            }
            else -> {
                logger.writeLog(value = "MockDatabaseType is wrong?! type-> $mockDatabaseType")
                emit(Failure(DATABASE_WRONG_TYPE_EXCEPTION))
                if (BuildConfig.DEBUG) {
                    throw IllegalStateException("MockDatabaseType is wrong?! type-> $mockDatabaseType")
                }
            }
        }
    }

    override fun getMockInformationFromId(
        mockDatabaseType: String,
        id: Long
    ): Flow<Response<MockDataClass, Int>> = flow {
        when (mockDatabaseType) {
            DATABASE_TYPE_NORMAL -> {
                logger.writeLog(value = "We are going to collect normal mock information with mock id->$id")
                val mockInformationResponse = normalMockDataSource.getMockInformationById(id)
                if (mockInformationResponse == null) {
                    logger.writeLog(value = "mock information is null!!")
                    emit(Failure(DATABASE_EMPTY_MOCK_INFORMATION))
                    return@flow
                }
                val positionInformationResponse =
                    normalMockDataSource.getMockPositionInformationById(id)
                if (positionInformationResponse.isEmpty()) {
                    logger.writeLog(value = "route information of mock is null!!")
                    emit(Failure(DATABASE_EMPTY_LINE_EXCEPTION))
                    return@flow
                }
                val result =
                    MockRepositoryTypeConverter.fromNormalMockEntity(mockInformationResponse)
                result.lineVector =
                    MockRepositoryTypeConverter.fromNormalPositionEntity(positionInformationResponse)
                emit(Success(result))
            }
            DATABASE_TYPE_IMPORTED -> {
                logger.writeLog(value = "We are going to collect imported mock information with mock id->$id")
                val mockInformationResponse = importedMockDataSource.getMockInformationById(id)
                if (mockInformationResponse == null) {
                    logger.writeLog(value = "mock information is null!!")
                    emit(Failure(DATABASE_EMPTY_MOCK_INFORMATION))
                    return@flow
                }
                val positionInformationResponse =
                    importedMockDataSource.getMockPositionInformationById(id)
                if (positionInformationResponse.isEmpty()) {
                    logger.writeLog(value = "route information of mock is null!!")
                    emit(Failure(DATABASE_EMPTY_LINE_EXCEPTION))
                    return@flow
                }
                val result =
                    MockRepositoryTypeConverter.fromImportedMockEntity(mockInformationResponse)
                result.lineVector =
                    MockRepositoryTypeConverter.fromImportedPositionEntity(
                        positionInformationResponse
                    )
                emit(Success(result))
            }
            else -> {
                logger.writeLog(value = "MockDatabaseType is wrong?! type-> $mockDatabaseType")
                emit(Failure(DATABASE_WRONG_TYPE_EXCEPTION))
                if (BuildConfig.DEBUG) {
                    throw IllegalStateException("MockDatabaseType is wrong?! type-> $mockDatabaseType")
                }
            }
        }
    }

    override fun createMockExportFile(
        mockDatabaseType: String,
        id: Long
    ): Flow<Response<File, Int>> = flow {
        generateMockExportJsonModelByMockId(
            mockDatabaseType = mockDatabaseType,
            id = id
        ).collect { response ->
            response.ifSuccessful { mockExportJsonModel ->
                var finalJson: String? = null
                try {
                    val moshi: Moshi = Moshi.Builder().build()
                    val jsonAdapter: JsonAdapter<MockExportJsonModel> =
                        moshi.adapter(MockExportJsonModel::class.java)
                    finalJson = jsonAdapter.toJson(mockExportJsonModel)
                } catch (exception: Exception) {
                    logger.captureExceptionWithLogFile(
                        message = "we had problem on creating json from model: ${exception.message}",
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
                        logger.captureExceptionWithLogFile(
                            message = "we had a problem on creating export mock file: ${exception.message}",
                            sentryEventLevel = SentryLevel.ERROR
                        )
                        emit(Failure(CREATE_EXPORT_FILE_EXCEPTION))
                    }
                }
            }
            response.ifNotSuccessful { exceptionType ->
                emit(Failure(exceptionType))
            }
        }
    }


    override fun parseJsonDataModelString(
        json: String
    ): Flow<Response<MockDataClass, Int>> = flow {
        if (json.isEmpty()) {
            emit(Failure(JSON_PROBLEM_EXCEPTION))
            return@flow
        }

        var mockJsonModel: MockExportJsonModel? = null
        try {
            val moshi: Moshi = Moshi.Builder().build()
            val jsonAdapter: JsonAdapter<MockExportJsonModel> =
                moshi.adapter(MockExportJsonModel::class.java)

            mockJsonModel = jsonAdapter.fromJson(json)
        } catch (jsonDataException: JsonDataException) {
            emit(Failure(JSON_PROCESS_FAILED_EXCEPTION))
            logger.writeLog(value = "The json format is not acceptable for parsing. we emit failed!")
        }

        if (mockJsonModel == null) {
            emit(Failure(JSON_PROCESS_FAILED_EXCEPTION))
            logger.writeLog(value = "We had exception on parsing json file. why json file is null?!")
            return@flow
        }

        emit(Success(MockRepositoryTypeConverter.fromExportedMockModel(mockJsonModel)))
    }

    private fun generateMockExportJsonModelByMockId(
        mockDatabaseType: String,
        id: Long,
    ): Flow<Response<MockExportJsonModel, Int>> = flow {
        var mockExportJsonModel: MockExportJsonModel?
        logger.writeLog(value = "We are going to generate export mock json model.")
        when (mockDatabaseType) {
            DATABASE_TYPE_NORMAL -> {
                val mockInformation = normalMockDataSource.getMockInformationById(id)
                if (mockInformation == null) {
                    logger.writeLog(value = "normal mock information is null!!")
                    emit(Failure(DATABASE_EMPTY_MOCK_INFORMATION))
                    return@flow
                }
                val positionsInformation = normalMockDataSource.getMockPositionInformationById(id)
                if (positionsInformation.isEmpty()) {
                    logger.writeLog(value = "normal mock route information is null!!")
                    emit(Failure(DATABASE_EMPTY_LINE_EXCEPTION))
                    return@flow
                }
                mockExportJsonModel = MockRepositoryTypeConverter.toMockExportJsonModel(
                    normalMockEntity = mockInformation,
                    time = getTime(),
                    androidId = androidId
                )
                mockExportJsonModel = MockRepositoryTypeConverter.toMockRoutingJsonModel(
                    mockExportJsonModel = mockExportJsonModel,
                    normalMockEntity = mockInformation,
                    normalPositionEntities = positionsInformation
                )
            }
            DATABASE_TYPE_IMPORTED -> {
                val mockInformation = importedMockDataSource.getMockInformationById(id)
                if (mockInformation == null) {
                    logger.writeLog(value = "imported mock information is null!!")
                    emit(Failure(DATABASE_EMPTY_MOCK_INFORMATION))
                    return@flow
                }
                val positionsInformation = importedMockDataSource.getMockPositionInformationById(id)
                if (positionsInformation.isEmpty()) {
                    logger.writeLog(value = "imported mock route information is null!!")
                    emit(Failure(DATABASE_EMPTY_LINE_EXCEPTION))
                    return@flow
                }
                mockExportJsonModel = MockRepositoryTypeConverter.toMockExportJsonModel(
                    importedMockEntity = mockInformation,
                    time = getTime(),
                    androidId = androidId
                )
                mockExportJsonModel = MockRepositoryTypeConverter.toMockRoutingJsonModel(
                    mockExportJsonModel = mockExportJsonModel,
                    importedMockEntity = mockInformation,
                    importedPositionEntities = positionsInformation
                )
            }
            else -> {
                logger.writeLog(value = "MockDatabaseType is wrong?! type-> $mockDatabaseType")
                emit(Failure(DATABASE_WRONG_TYPE_EXCEPTION))
                if (BuildConfig.DEBUG) {
                    throw IllegalStateException("MockDatabaseType is wrong?! type-> $mockDatabaseType")
                }
                return@flow
            }
        }
        emit(Success(mockExportJsonModel))
    }

    private fun updateCreateAndUpdateTimeOfMock(
        mockDataClass: MockDataClass
    ): MockDataClass = mockDataClass.apply {
        createdAt = if (createdAt == null) getTime() else createdAt
        updatedAt = getTime()
    }

    private fun getTime(): String {
        return Calendar.getInstance().time.toString()
    }
}