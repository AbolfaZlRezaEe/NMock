package me.abolfazl.nmock.repository.locationInfo

import io.sentry.Sentry
import io.sentry.SentryLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.abolfazl.nmock.model.apiService.RoutingApiService
import me.abolfazl.nmock.model.apiService.models.locationInfo.LocationInfoModel
import me.abolfazl.nmock.repository.models.LocationInfoDataclass
import me.abolfazl.nmock.utils.logger.NMockLogger
import me.abolfazl.nmock.utils.response.Failure
import me.abolfazl.nmock.utils.response.Response
import me.abolfazl.nmock.utils.response.Success
import javax.inject.Inject

class LocationInfoRepositoryImpl @Inject constructor(
    private val apiService: RoutingApiService,
    private val logger: NMockLogger
) : LocationInfoRepository {

    companion object {
        const val UNKNOWN_EXCEPTION = 110
    }

    init {
        logger.disableLogHeaderForThisClass()
    }

    override fun getLocationInformation(
        latitude: Double,
        longitude: Double
    ): Flow<Response<LocationInfoDataclass, Int>> = flow {
        val response = apiService.getLocationInformation(latitude, longitude)
        if (response.isSuccessful) {
            response.body()?.let { result ->
                emit(Success(toLocationInfoDataclass(result)))
                return@flow
            }
            logger.writeLog(
                value = "getLocationInformation was failed. response is successful but body is null!"
            )
            emit(Failure(UNKNOWN_EXCEPTION))
        } else {
            logger.writeLog(
                value = "getLocationInformation was failed. response code is-> ${response.code()}"
            )
            Sentry.captureMessage(
                "getLocationInformation failed! response code is-> ${response.code()}",
                SentryLevel.FATAL
            )
        }
    }

    private fun toLocationInfoDataclass(
        locationInfoModel: LocationInfoModel
    ): LocationInfoDataclass = LocationInfoDataclass(
        fullAddress = locationInfoModel.formattedAddress,
    )

}