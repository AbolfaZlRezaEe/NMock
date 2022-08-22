package me.abolfazl.nmock.repository.routingInfo

import io.sentry.Sentry
import io.sentry.SentryLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.abolfazl.nmock.model.apiService.RoutingApiService
import me.abolfazl.nmock.model.apiService.models.routingInfo.LegModel
import me.abolfazl.nmock.model.apiService.models.routingInfo.RouteModel
import me.abolfazl.nmock.model.apiService.models.routingInfo.RoutingInfoModel
import me.abolfazl.nmock.model.apiService.models.routingInfo.StepModel
import me.abolfazl.nmock.repository.routingInfo.models.LegDataclass
import me.abolfazl.nmock.repository.routingInfo.models.RouteDataclass
import me.abolfazl.nmock.repository.routingInfo.models.RoutingInfoDataclass
import me.abolfazl.nmock.repository.routingInfo.models.StepDataclass
import me.abolfazl.nmock.utils.logger.NMockLogger
import me.abolfazl.nmock.utils.response.Failure
import me.abolfazl.nmock.utils.response.Response
import me.abolfazl.nmock.utils.response.Success
import javax.inject.Inject

class RoutingInfoRepositoryImpl @Inject constructor(
    private val apiService: RoutingApiService,
    private val logger: NMockLogger
) : RoutingInfoRepository {

    companion object {
        const val UNKNOWN_EXCEPTION = 310
    }

    init {
        logger.disableLogHeaderForThisClass()
        logger.setClassInformationForEveryLog(javaClass.simpleName)
    }

    override suspend fun getRoutingInformation(
        origin: String,
        destination: String
    ): Flow<Response<RoutingInfoDataclass, Int>> = flow {
        val response = apiService.getRoutingInformation(
            origin = origin,
            destination = destination
        )
        if (response.isSuccessful) {
            response.body()?.let {
                emit(Success(toRoutingInfoDataclass(it)))
                return@flow
            }
            logger.writeLog(
                value = "getRoutingInformation was failed. response is successful but body is null!"
            )
            emit(Failure(UNKNOWN_EXCEPTION))
        } else {
            logger.captureExceptionWithLogFile(
                message = "getRoutingInformation was failed. response code is-> ${response.code()}"
            )
        }
    }

    private fun toRoutingInfoDataclass(
        routingInfoModel: RoutingInfoModel
    ): RoutingInfoDataclass = RoutingInfoDataclass(
        routeModels = toRoutingDataclassList(routingInfoModel.routeModels)
    )

    private fun toRoutingDataclassList(
        routeModel: List<RouteModel>
    ): List<RouteDataclass> {
        val result = ArrayList<RouteDataclass>()
        routeModel.forEach {
            result.add(RouteDataclass(toLegDataclassList(it.legModels)))
        }
        return result
    }

    private fun toLegDataclassList(
        legModel: List<LegModel>
    ): List<LegDataclass> {
        val result = ArrayList<LegDataclass>()
        legModel.forEach {
            result.add(LegDataclass(toStepDataclassList(it.steps)))
        }
        return result
    }

    private fun toStepDataclassList(
        stepModel: List<StepModel>
    ): List<StepDataclass> {
        val result = ArrayList<StepDataclass>()
        stepModel.forEach {
            result.add(StepDataclass(polyline = it.polyline))
        }
        return result
    }
}