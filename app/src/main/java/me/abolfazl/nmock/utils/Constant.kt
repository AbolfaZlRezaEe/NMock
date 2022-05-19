package me.abolfazl.nmock.utils

object Constant {
    const val BASE_URL = "https://api.neshan.org/"

    // Mock types
    const val TYPE_CUSTOM_CREATE = "CUSTOM_CREATION"
    const val TYPE_AUTOMATIC_CREATE = "AUTOMATIC_CREATION"

    // Mock Provider names
    const val PROVIDER_GPS = "GPS_PROVIDER"
    const val PROVIDER_NETWORK = "NETWORK_PROVIDER"

    // Mock Provider types
    const val TYPE_GPS = "gps"

    const val DATABASE_NAME = "nmock_db"

    const val CREATED_AT = "Created at:"
    const val UPDATED_AT = "Updated at:"

    const val HEADER_API_KEY = "Api-Key"
    const val HEADER_VALUE_API_KEY = "service.2xpUYE0D5pjJZOSSUwmzlhjQrKB4g68pcg9wzDJg"

    // Location
    const val LOCATION_REQUEST = 1005
    const val LOCATION_INTERVAL: Long = 1000
    const val LOCATION_FASTEST_INTERVAL: Long = 500
    const val DEFAULT_RATIO = 0.00006
    const val DEFAULT_SPEED = 30

    // Notification
    const val NOTIFICATION_CHANNEL_NAME = "Mock_Provider"
    const val NOTIFICATION_CHANNEL_ID = "NMOCK_NOTIFICATION_ID"
    const val NOTIFICATION_CHANNEL_DESCRIPTION =
        "This Notification used for Broadcasting Mock around the Android System!"
    const val NOTIFICATION_ID = 15435

    // Shared
    const val SHARED_PREFERENCES_NAME = "mock_shared"
}