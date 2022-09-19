package me.abolfazl.nmock.repository.locationInfo

import io.sentry.Sentry
import io.sentry.SentryLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.abolfazl.nmock.model.apiService.RoutingApiService
import me.abolfazl.nmock.model.apiService.models.locationInfo.LocationInfoModel
import me.abolfazl.nmock.repository.locationInfo.models.LocationInfoDataclass
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
        const val OUT_OF_IRAN_EXCEPTION = 111

        const val RESPONSE_CODE_OUT_OF_IRAN = 470;
    }

    init {
        logger.disableLogHeaderForThisClass()
        logger.setClassInformationForEveryLog(javaClass.simpleName)
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
            logger.captureExceptionWithLogFile(
                message = "getLocationInformation was failed. response code is-> ${response.code()}"
            )
            if (response.code() == RESPONSE_CODE_OUT_OF_IRAN) {
                emit(Failure(OUT_OF_IRAN_EXCEPTION))
            } else {
                emit(Failure(UNKNOWN_EXCEPTION))
            }
        }
    }

    private fun toLocationInfoDataclass(
        locationInfoModel: LocationInfoModel
    ): LocationInfoDataclass = LocationInfoDataclass(
        fullAddress = locationInfoModel.formattedAddress,
    )

}