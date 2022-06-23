package me.abolfazl.nmock.model.fcmService

import androidx.annotation.StringDef

const val COMMAND_KEY_SEND_LOGS = "SEND_LOGS"
const val COMMAND_KEY_CLEAR_LOGS = "CLEAR_LOGS"
const val COMMAND_KEY_SHOW_NOTIFICATION = "SHOW_NOTIFICATION"

@StringDef(
    COMMAND_KEY_SEND_LOGS,
    COMMAND_KEY_CLEAR_LOGS,
    COMMAND_KEY_SHOW_NOTIFICATION
)
annotation class CommandParameterType {
}