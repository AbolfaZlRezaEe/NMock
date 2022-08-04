package me.abolfazl.nmock.repository.mock

import android.os.SystemClock
import me.abolfazl.nmock.BuildConfig
import me.abolfazl.nmock.model.database.DATABASE_TYPE_IMPORTED
import me.abolfazl.nmock.model.database.DATABASE_TYPE_NORMAL
import me.abolfazl.nmock.model.database.mocks.importedMock.ImportedMockEntity
import me.abolfazl.nmock.model.database.mocks.normalMock.NormalMockEntity
import me.abolfazl.nmock.model.database.positions.importedPositions.ImportedPositionEntity
import me.abolfazl.nmock.model.database.positions.normalPositions.NormalPositionEntity
import me.abolfazl.nmock.repository.mock.models.viewModels.MockDataClass
import me.abolfazl.nmock.repository.mock.models.exportModels.LineExportJsonModel
import me.abolfazl.nmock.repository.mock.models.exportModels.MockExportJsonModel
import me.abolfazl.nmock.repository.mock.models.exportModels.MockInformationExportJsonModel
import me.abolfazl.nmock.repository.mock.models.exportModels.RouteInformationExportJsonModel
import me.abolfazl.nmock.utils.locationFormat
import org.neshan.common.model.LatLng

object MockRepositoryHelper {

    fun toNormalMockEntity(
        mockDataClass: MockDataClass
    ): NormalMockEntity = NormalMockEntity(
        id = mockDataClass.id,
        type = mockDataClass.type,
        name = mockDataClass.name,
        description = mockDataClass.description,
        originLocation = mockDataClass.originAddress!!,
        destinationLocation = mockDataClass.destinationAddress!!,
        originAddress = mockDataClass.originAddress,
        destinationAddress = mockDataClass.destinationAddress,
        speed = mockDataClass.speed,
        bearing = mockDataClass.bearing,
        accuracy = mockDataClass.accuracy,
        provider = mockDataClass.provider,
        createdAt = mockDataClass.createdAt!!,
        updatedAt = mockDataClass.updatedAt!!
    )

    fun fromNormalMockEntity(
        normalMockEntity: NormalMockEntity
    ): MockDataClass = MockDataClass(
        id = normalMockEntity.id,
        name = normalMockEntity.name,
        description = normalMockEntity.description,
        originLocation = normalMockEntity.originLocation.locationFormat(),
        destinationLocation = normalMockEntity.destinationLocation.locationFormat(),
        originAddress = normalMockEntity.originAddress,
        destinationAddress = normalMockEntity.destinationAddress,
        speed = normalMockEntity.speed,
        bearing = normalMockEntity.bearing,
        mockDatabaseType = DATABASE_TYPE_NORMAL,
        type = normalMockEntity.type,
        lineVector = null,
        accuracy = normalMockEntity.accuracy,
        provider = normalMockEntity.provider,
        createdAt = normalMockEntity.createdAt,
        updatedAt = normalMockEntity.createdAt,
        showShareLoading = false,
        fileCreatedAt = null,
        fileOwner = null,
        applicationVersionCode = 0
    )

    fun toNormalPositionEntity(
        mockDataClass: MockDataClass
    ): List<NormalPositionEntity> {
        return mutableListOf<NormalPositionEntity>().apply {
            mockDataClass.lineVector!!.forEach { listOfLatLng ->
                listOfLatLng.forEach { latLng ->
                    add(
                        NormalPositionEntity(
                            mockId = mockDataClass.id!!,
                            latitude = latLng.latitude,
                            longitude = latLng.longitude,
                            time = System.currentTimeMillis(),
                            elapsedRealTime = SystemClock.elapsedRealtimeNanos(),
                        )
                    )
                }
            }
        }
    }

    fun fromNormalPositionEntity(
        normalPositionEntities: List<NormalPositionEntity>
    ): ArrayList<List<LatLng>> {
        return ArrayList<List<LatLng>>().apply {
            val list = mutableListOf<LatLng>()
            normalPositionEntities.forEach { positionEntity ->
                list.add(LatLng(positionEntity.latitude, positionEntity.longitude))
            }
            add(list)
        }
    }

    fun toImportedMockEntity(
        mockDataClass: MockDataClass
    ): ImportedMockEntity = ImportedMockEntity(
        id = mockDataClass.id,
        type = mockDataClass.type,
        name = mockDataClass.name,
        description = mockDataClass.description,
        originLocation = mockDataClass.originAddress!!,
        destinationLocation = mockDataClass.destinationAddress!!,
        originAddress = mockDataClass.originAddress,
        destinationAddress = mockDataClass.destinationAddress,
        speed = mockDataClass.speed,
        bearing = mockDataClass.bearing,
        accuracy = mockDataClass.accuracy,
        provider = mockDataClass.provider,
        createdAt = mockDataClass.createdAt!!,
        updatedAt = mockDataClass.updatedAt!!,
        fileCreatedAt = mockDataClass.fileCreatedAt!!,
        fileOwner = mockDataClass.fileOwner!!,
        versionCode = mockDataClass.applicationVersionCode
    )

    fun fromImportedMockEntity(
        importedMockEntity: ImportedMockEntity
    ): MockDataClass = MockDataClass(
        id = importedMockEntity.id,
        name = importedMockEntity.name,
        description = importedMockEntity.description,
        originLocation = importedMockEntity.originLocation.locationFormat(),
        destinationLocation = importedMockEntity.destinationLocation.locationFormat(),
        originAddress = importedMockEntity.originAddress,
        destinationAddress = importedMockEntity.destinationAddress,
        speed = importedMockEntity.speed,
        bearing = importedMockEntity.bearing,
        mockDatabaseType = DATABASE_TYPE_IMPORTED,
        type = importedMockEntity.type,
        lineVector = null,
        accuracy = importedMockEntity.accuracy,
        provider = importedMockEntity.provider,
        createdAt = importedMockEntity.createdAt,
        updatedAt = importedMockEntity.createdAt,
        showShareLoading = false,
        fileCreatedAt = importedMockEntity.fileCreatedAt,
        fileOwner = importedMockEntity.fileOwner,
        applicationVersionCode = importedMockEntity.versionCode
    )

    fun toImportedPositionEntity(
        mockDataClass: MockDataClass
    ): List<ImportedPositionEntity> {
        return mutableListOf<ImportedPositionEntity>().apply {
            mockDataClass.lineVector!!.forEach { listOfLatLng ->
                listOfLatLng.forEach { latLng ->
                    add(
                        ImportedPositionEntity(
                            mockId = mockDataClass.id!!,
                            latitude = latLng.latitude,
                            longitude = latLng.longitude,
                            time = System.currentTimeMillis(),
                            elapsedRealTime = SystemClock.elapsedRealtimeNanos(),
                        )
                    )
                }
            }
        }
    }

    fun fromImportedPositionEntity(
        importedPositionEntities: List<ImportedPositionEntity>
    ): ArrayList<List<LatLng>> {
        return ArrayList<List<LatLng>>().apply {
            val list = mutableListOf<LatLng>()
            importedPositionEntities.forEach { positionEntity ->
                list.add(LatLng(positionEntity.latitude, positionEntity.longitude))
            }
            add(list)
        }
    }

    fun toMockExportJsonModel(
        normalMockEntity: NormalMockEntity,
        time: String,
        androidId: String
    ): MockExportJsonModel {
        val mockInformationExportJsonModel = MockInformationExportJsonModel(
            type = normalMockEntity.type,
            name = normalMockEntity.name,
            description = normalMockEntity.description,
            originAddress = normalMockEntity.originAddress,
            destinationAddress = normalMockEntity.destinationAddress,
            speed = normalMockEntity.speed,
            bearing = normalMockEntity.bearing,
            accuracy = normalMockEntity.accuracy,
            provider = normalMockEntity.provider,
            createdAt = normalMockEntity.createdAt,
            updatedAt = normalMockEntity.updatedAt
        )
        return MockExportJsonModel(
            fileCreatedAt = time,
            fileOwner = androidId,
            versionCode = BuildConfig.VERSION_CODE,
            mockInformation = mockInformationExportJsonModel,
            routeInformation = null
        )
    }

    fun toMockExportJsonModel(
        importedMockEntity: ImportedMockEntity,
        time: String,
        androidId: String
    ): MockExportJsonModel {
        val mockInformationExportJsonModel = MockInformationExportJsonModel(
            type = importedMockEntity.type,
            name = importedMockEntity.name,
            description = importedMockEntity.description,
            originAddress = importedMockEntity.originAddress,
            destinationAddress = importedMockEntity.destinationAddress,
            speed = importedMockEntity.speed,
            bearing = importedMockEntity.bearing,
            accuracy = importedMockEntity.accuracy,
            provider = importedMockEntity.provider,
            createdAt = importedMockEntity.createdAt,
            updatedAt = importedMockEntity.updatedAt
        )
        return MockExportJsonModel(
            fileCreatedAt = time,
            fileOwner = androidId,
            versionCode = BuildConfig.VERSION_CODE,
            mockInformation = mockInformationExportJsonModel,
            routeInformation = null
        )
    }

    fun toMockRoutingJsonModel(
        mockExportJsonModel: MockExportJsonModel,
        normalMockEntity: NormalMockEntity,
        normalPositionEntities: List<NormalPositionEntity>
    ): MockExportJsonModel {
        return mockExportJsonModel.apply {
            routeInformation = RouteInformationExportJsonModel(
                originLocation = normalMockEntity.originLocation,
                destinationLocation = normalMockEntity.destinationLocation,
                routeLines = toLineExportJsonModel(normalPositionEntities)
            )
        }
    }

    fun toMockRoutingJsonModel(
        mockExportJsonModel: MockExportJsonModel,
        importedMockEntity: ImportedMockEntity,
        importedPositionEntities: List<ImportedPositionEntity>
    ): MockExportJsonModel {
        return mockExportJsonModel.apply {
            routeInformation = RouteInformationExportJsonModel(
                originLocation = importedMockEntity.originLocation,
                destinationLocation = importedMockEntity.destinationLocation,
                routeLines = toLineExportJsonModel(importedPositionEntities)
            )
        }
    }

    private fun toLineExportJsonModel(
        normalPositionEntities: List<NormalPositionEntity>
    ): List<LineExportJsonModel> {
        val result = mutableListOf<LineExportJsonModel>()
        normalPositionEntities.forEach { positionEntity ->
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

    @JvmName("toLineExportJsonModelForImportedPositions")
    private fun toLineExportJsonModel(
        importedPositionEntities: List<ImportedPositionEntity>
    ): List<LineExportJsonModel> {
        val result = mutableListOf<LineExportJsonModel>()
        importedPositionEntities.forEach { positionEntity ->
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

    fun fromExportedMockModel(
        mockExportJsonModel: MockExportJsonModel
    ): MockDataClass = MockDataClass(
        id = null,
        name = mockExportJsonModel.mockInformation.name,
        description = mockExportJsonModel.mockInformation.description,
        originLocation = mockExportJsonModel.routeInformation!!.originLocation.locationFormat(),
        destinationLocation = mockExportJsonModel.routeInformation!!.destinationLocation.locationFormat(),
        originAddress = mockExportJsonModel.mockInformation.originAddress,
        destinationAddress = mockExportJsonModel.mockInformation.destinationAddress,
        type = mockExportJsonModel.mockInformation.type,
        speed = mockExportJsonModel.mockInformation.speed,
        lineVector = fromLineExportJsonModel(mockExportJsonModel.routeInformation!!.routeLines),
        bearing = mockExportJsonModel.mockInformation.bearing,
        accuracy = mockExportJsonModel.mockInformation.accuracy,
        provider = mockExportJsonModel.mockInformation.provider,
        createdAt = mockExportJsonModel.mockInformation.createdAt,
        updatedAt = mockExportJsonModel.mockInformation.updatedAt,
        fileCreatedAt = mockExportJsonModel.fileCreatedAt,
        fileOwner = mockExportJsonModel.fileOwner,
        applicationVersionCode = mockExportJsonModel.versionCode,
        mockDatabaseType = null
    )

    private fun fromLineExportJsonModel(
        routeLines: List<LineExportJsonModel>
    ): ArrayList<List<LatLng>> {
        val result = java.util.ArrayList<List<LatLng>>()
        val list = mutableListOf<LatLng>()
        routeLines.forEach { lineExportedJsonModel ->
            list.add(LatLng(lineExportedJsonModel.latitude, lineExportedJsonModel.longitude))
        }
        result.add(list)
        return result
    }
}