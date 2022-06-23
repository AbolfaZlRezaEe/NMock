package me.abolfazl.nmock.model.fcmService

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import me.abolfazl.nmock.R
import me.abolfazl.nmock.utils.Constant
import me.abolfazl.nmock.utils.SHARED_FIREBASE_TOKEN
import me.abolfazl.nmock.utils.logger.NMockLogger
import me.abolfazl.nmock.utils.managers.NotificationManager
import me.abolfazl.nmock.utils.managers.SharedManager
import javax.inject.Inject

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var logger: NMockLogger

    private var firebaseData: FirebaseData? = null

    override fun onNewToken(token: String) {
        SharedManager.putString(
            sharedPreferences = sharedPreferences,
            key = SHARED_FIREBASE_TOKEN,
            value = token
        )
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val pushData: Map<String, String> = message.data

        firebaseData = FirebaseData(pushData)

        firebaseData?.let { data ->
            processCommandData(
                command = data.command,
                commandMetadata = data.commandMateData
            )
        }
    }

    private fun processCommandData(
        @CommandParameterType command: String?,
        commandMetadata: String?
    ) {
        command?.let { cmd ->
            when (cmd) {
                COMMAND_KEY_SEND_LOGS -> {
                    logger.sendLogsFile()
                }
                COMMAND_KEY_CLEAR_LOGS -> {
                    logger.clearLogsFile()
                }
                COMMAND_KEY_SHOW_NOTIFICATION -> {
                    processNotificationData()
                }
            }
        }
        commandMetadata?.let {
            // this section is not implemented yet
        }
    }

    private fun processNotificationData() {
        firebaseData?.let { data ->
            if (data.notificationTitle == null) return // We can't show notification without title!
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            val notificationChannelId = NotificationManager.createNotificationChannel(
                context = this,
                silentChannel = data.notificationIsSilent,
                channelName = Constant.PUSH_NOTIFICATION_CHANNEL_NAME,
                description = resources.getString(R.string.pushNotificationChannelDescription)
            )
            val notification = NotificationManager.createPushNotification(
                context = this,
                notificationChannelId = notificationChannelId,
                title = data.notificationTitle!!,
                description = data.notificationDescription,
                smallIcon = NotificationManager.getSmallIconFromNotificationData(
                    context = this,
                    rawIconData = data.notificationSmallIcon
                )
            )
            notificationManager.notify(Constant.PUSH_NOTIFICATION_ID, notification)
        }
    }
}