package me.abolfazl.nmock.utils

object Constant {

    // Sentry
    const val SENTRY_DSN =
        "https://0221c991ee9f4fdc9a92def0bf4ee29e@o1040806.ingest.sentry.io/6421362"
    const val ENVIRONMENT_DEBUG = "Debug-Mode"
    const val ENVIRONMENT_RELEASE = "Release-Mode"
    const val LOGGER_FILE_NAME = "Helper"

    // Mock Provider names
    const val PROVIDER_GPS = "GPS_PROVIDER"
    const val PROVIDER_NETWORK = "NETWORK_PROVIDER"

    // Mock Provider types
    const val TYPE_GPS = "gps"

    const val DATABASE_NAME = "nmock_db"

    // Location
    const val LOCATION_REQUEST = 1005
    const val LOCATION_INTERVAL: Long = 1000
    const val LOCATION_FASTEST_INTERVAL: Long = 500
    const val DEFAULT_RATIO = 0.00006
    const val DEFAULT_SPEED = 30

    // Notification
    const val APPLICATION_NOTIFICATION_ID = 15435
    const val PUSH_NOTIFICATION_ID = 15436

    // Shared
    const val SHARED_PREFERENCES_NAME = "mock_shared"

    // Directory names
    const val DIRECTORY_NAME_EXPORT_FILES = "EXM"

    const val EXPORT_MOCK_FILE_FORMAT = ".json"
}