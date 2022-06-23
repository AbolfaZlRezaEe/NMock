package me.abolfazl.nmock.model.fcmService

import org.json.JSONObject

class FirebaseData constructor(
    firebaseMetaData: Map<String, String>
) {

    companion object {
        // Primary keys
        private const val FIRE_KEY_COMMAND_DATA = "COMMAND_DATA"
        private const val FIRE_KEY_NOTIFICATION_DATA = "NOTIFICATION_DATA"

        // Notification data keys
        private const val NOTIFICATION_TITLE_KEY = "NOTIFICATION_TITLE"
        private const val NOTIFICATION_DESCRIPTION_KEY = "NOTIFICATION_DESCRIPTION"
        private const val NOTIFICATION_TARGET_KEY = "NOTIFICATION_TARGET"
        private const val NOTIFICATION_IMAGE_KEY = "NOTIFICATION_IMAGE"
        private const val NOTIFICATION_METADATA_KEY = "NOTIFICATION_METADATA"

        // Command data keys
        private const val COMMAND_DATA_KEY = "COMMAND_DATA"
        private const val COMMAND_METADATA_KEY = "COMMAND_METADATA"
    }

    // Command data
    @CommandParameterType
    var command: String? = null
    var commandMateData: String? = null

    // Notification data
    @NotificationTargetType
    var notificationTarget: String? = null
    var notificationTitle: String? = null
    var notificationDescription: String? = null
    var notificationImage: String? = null
    var notificationMetaData: String? = null

    init {
        val commandData = firebaseMetaData[FIRE_KEY_COMMAND_DATA]
        val notificationData = firebaseMetaData[FIRE_KEY_NOTIFICATION_DATA]

        parseCommandData(commandData)
        parseNotificationData(notificationData)
    }

    // Notification data should be json!
    private fun parseNotificationData(notificationData: String?) {
        if (notificationData == null) return

        val jsonObject = JSONObject(notificationData)
        notificationTitle = jsonObject.get(NOTIFICATION_TITLE_KEY).toString()
        notificationDescription = jsonObject.get(NOTIFICATION_DESCRIPTION_KEY).toString()
        notificationTarget = jsonObject.get(NOTIFICATION_TARGET_KEY).toString()
        notificationImage = jsonObject.get(NOTIFICATION_IMAGE_KEY).toString()
        notificationMetaData = jsonObject.get(NOTIFICATION_METADATA_KEY).toString()
    }

    private fun parseCommandData(commandData: String?) {
        if (commandData == null) return

        val jsonObject = JSONObject(commandData)
        command = jsonObject.get(COMMAND_DATA_KEY).toString()
        commandMateData = jsonObject.get(COMMAND_METADATA_KEY).toString()
    }
}