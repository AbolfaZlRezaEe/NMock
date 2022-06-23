package me.abolfazl.nmock.model.fcmService

import org.json.JSONObject

class FirebaseData constructor(
    firebaseMetaData: Map<String, String>
) {

    companion object {
        // Primary keys
        private const val FIRE_KEY_COMMAND_DATA = "Command_data"
        private const val FIRE_KEY_NOTIFICATION_DATA = "notification_data"

        // Notification data keys
        private const val NOTIFICATION_TITLE_KEY = "notification_title"
        private const val NOTIFICATION_DESCRIPTION_KEY = "notification_description"
        private const val NOTIFICATION_TARGET_KEY = "notification_target"
        private const val NOTIFICATION_SMALL_ICON_KEY = "notification_small_icon"
        private const val NOTIFICATION_METADATA_KEY = "notification_metadata"
        private const val NOTIFICATION_SILENT_KEY = "notification_silent_state"

        // Command data keys
        private const val COMMAND_DATA_KEY = "command_data"
        private const val COMMAND_METADATA_KEY = "command_metadata"
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
    var notificationSmallIcon: String? = null
    var notificationIsSilent: Boolean = false
    var notificationMetaData: String? = null

    init {
        val commandData = firebaseMetaData[FIRE_KEY_COMMAND_DATA]
        val notificationData = firebaseMetaData[FIRE_KEY_NOTIFICATION_DATA]

        parseCommandData(commandData)
        parseNotificationData(notificationData)
    }

    private fun parseNotificationData(notificationData: String?) {
        if (notificationData == null) return

        val jsonObject = JSONObject(notificationData)
        notificationTitle = jsonObject.get(NOTIFICATION_TITLE_KEY).toString()
        notificationDescription = jsonObject.get(NOTIFICATION_DESCRIPTION_KEY).toString()
        notificationTarget = jsonObject.get(NOTIFICATION_TARGET_KEY).toString()
        notificationSmallIcon = jsonObject.get(NOTIFICATION_SMALL_ICON_KEY).toString()
        notificationMetaData = jsonObject.get(NOTIFICATION_METADATA_KEY).toString()
        notificationIsSilent = jsonObject.get(NOTIFICATION_SILENT_KEY).toString().toBoolean()
    }

    private fun parseCommandData(commandData: String?) {
        if (commandData == null) return

        val jsonObject = JSONObject(commandData)
        command = jsonObject.get(COMMAND_DATA_KEY).toString()
        commandMateData = jsonObject.get(COMMAND_METADATA_KEY).toString()
    }
}