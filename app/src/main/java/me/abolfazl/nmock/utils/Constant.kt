package me.abolfazl.nmock.utils

object Constant {
    const val BASE_URL = "https://api.neshan.org/"

    // Sentry
    const val SENTRY_DSN =
        "https://0221c991ee9f4fdc9a92def0bf4ee29e@o1040806.ingest.sentry.io/6421362"
    const val ENVIRONMENT_DEBUG = "Debug-Mode"
    const val ENVIRONMENT_RELEASE = "Release-Mode"
    const val LOGGER_FILE_NAME = "Helper"

    // Mock types
    const val TYPE_CUSTOM_CREATE = "CUSTOM_CREATION"
    const val TYPE_AUTOMATIC_CREATE = "AUTOMATIC_CREATION"

    // Mock Provider names
    const val PROVIDER_GPS = "GPS_PROVIDER"
    const val PROVIDER_NETWORK = "NETWORK_PROVIDER"

    // Mock Provider types
    const val TYPE_GPS = "gps"

    const val DATABASE_NAME = "nmock_db"

    const val HEADER_API_KEY = "Api-Key"
    const val HEADER_VALUE_API_KEY = "service.2xpUYE0D5pjJZOSSUwmzlhjQrKB4g68pcg9wzDJg"

    // Location
    const val LOCATION_REQUEST = 1005
    const val LOCATION_INTERVAL: Long = 1000
    const val LOCATION_FASTEST_INTERVAL: Long = 500
    const val DEFAULT_RATIO = 0.00006
    const val DEFAULT_SPEED = 30

    // Notification
    const val APPLICATION_NOTIFICATION_CHANNEL_NAME = "NMock Notification"
    const val PUSH_NOTIFICATION_CHANNEL_NAME = "Push Notification"
    const val APPLICATION_NOTIFICATION_ID = 15435
    const val PUSH_NOTIFICATION_ID = 15436

    // Shared
    const val SHARED_PREFERENCES_NAME = "mock_shared"
}