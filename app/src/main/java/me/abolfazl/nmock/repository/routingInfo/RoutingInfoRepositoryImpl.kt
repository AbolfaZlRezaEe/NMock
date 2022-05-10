package me.abolfazl.nmock.repository.routingInfo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.abolfazl.nmock.model.apiService.RoutingApiService
import me.abolfazl.nmock.model.apiService.models.routingInfo.LegModel
import me.abolfazl.nmock.model.apiService.models.routingInfo.RouteModel
import me.abolfazl.nmock.model.apiService.models.routingInfo.RoutingInfoModel
import me.abolfazl.nmock.model.apiService.models.routingInfo.StepModel
import me.abolfazl.nmock.model.database.dao.MockDao
import me.abolfazl.nmock.model.database.dao.PositionDao
import me.abolfazl.nmock.model.database.models.MockEntity
import me.abolfazl.nmock.repository.models.routingInfo.LegDataclass
import me.abolfazl.nmock.repository.models.routingInfo.RouteDataclass
import me.abolfazl.nmock.repository.models.routingInfo.RoutingInfoDataclass
import me.abolfazl.nmock.repository.models.routingInfo.StepDataclass
import me.abolfazl.nmock.utils.response.*
import me.abolfazl.nmock.utils.response.exceptions.EXCEPTION_UNKNOWN
import me.abolfazl.nmock.utils.response.exceptions.ExceptionMapper
import me.abolfazl.nmock.utils.response.exceptions.NMockException
import javax.inject.Inject

class RoutingInfoRepositoryImpl @Inject constructor(
    private val apiService: RoutingApiService
) : RoutingInfoRepository {

    override suspend fun getRoutingInformation(
        origin: String,
        destination: String
    ): Flow<Response<RoutingInfoDataclass, NMockException>> = flow {
        val response = apiService.getRoutingInformation(
            origin = origin,
            destination = destination
        )
        if (response.isSuccessful) {
            response.body()?.let {
                emit(Success(toRoutingInfoDataclass(it)))
                return@flow
            }
            emit(Failure(NMockException(type = EXCEPTION_UNKNOWN)))
        } else {
            emit(Failure(NMockException(type = ExceptionMapper.map(response.code()))))
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