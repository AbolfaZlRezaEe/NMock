package me.abolfazl.nmock.repository.locationInfo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import me.abolfazl.nmock.model.apiService.RoutingApiService
import me.abolfazl.nmock.model.apiService.models.locationInfo.LocationInfoModel
import me.abolfazl.nmock.repository.models.LocationInfoDataclass
import me.abolfazl.nmock.utils.locationFormat
import me.abolfazl.nmock.utils.response.*
import me.abolfazl.nmock.utils.response.exceptions.EXCEPTION_UNKNOWN
import me.abolfazl.nmock.utils.response.exceptions.ExceptionMapper
import me.abolfazl.nmock.utils.response.exceptions.NMockException
import org.neshan.common.model.LatLng
import javax.inject.Inject

class LocationInfoRepositoryImpl @Inject constructor(
    private val apiService: RoutingApiService
) : LocationInfoRepository {

    override fun getLocationInformation(
        latitude: Double,
        longitude: Double
    ): Flow<Response<LocationInfoDataclass, NMockException>> = flow {
        val response = apiService.getLocationInformation(latitude, longitude)
        if (response.isSuccessful) {
            response.body()?.let { result ->
                emit(Success(toLocationInfoDataclass(result)))
                return@flow
            }
            emit(Failure(NMockException(type = EXCEPTION_UNKNOWN)))
        } else {
            emit(Failure(NMockException(type = ExceptionMapper.map(response.code()))))
        }
    }

    private fun toLocationInfoDataclass(
        locationInfoModel: LocationInfoModel
    ): LocationInfoDataclass = LocationInfoDataclass(
        fullAddress = locationInfoModel.formattedAddress,
    )

}