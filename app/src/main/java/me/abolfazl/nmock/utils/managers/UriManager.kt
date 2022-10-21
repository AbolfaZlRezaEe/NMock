package me.abolfazl.nmock.utils.managers

import android.net.Uri
import com.google.android.gms.maps.model.LatLng

object UriManager {

    private const val SHARED_URI_SCHEME = "https"
    const val SHARED_URI_AUTHORITY = "shared.nmock.trip"
    private const val SHARED_URI_PATH = "editor"
    const val SHARED_URI_SPEED_KEY = "speed"
    const val SHARED_URI_ORIGIN_KEY = "origin"
    const val SHARED_URI_DESTINATION_KEY = "destination"

    fun createShareUri(
        origin: LatLng,
        destination: LatLng,
        speed: Int
    ): Uri {
        return Uri.Builder()
            .scheme(SHARED_URI_SCHEME)
            .authority(SHARED_URI_AUTHORITY)
            .appendPath(SHARED_URI_PATH)
            .appendQueryParameter(SHARED_URI_SPEED_KEY, speed.toString())
            .appendQueryParameter(SHARED_URI_ORIGIN_KEY, "${origin.latitude},${origin.longitude}")
            .appendQueryParameter(
                SHARED_URI_DESTINATION_KEY,
                "${destination.latitude},${destination.longitude}"
            )
            .build()
    }

    fun createNavigationUri(
        destination: LatLng
    ): Uri {
        return Uri.parse("geo:${destination.latitude},${destination.longitude}")
    }
}