package me.abolfazl.nmock.model.fcmService

import android.content.SharedPreferences
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import me.abolfazl.nmock.utils.SHARED_FIREBASE_TOKEN
import me.abolfazl.nmock.utils.managers.SharedManager
import javax.inject.Inject

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

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

            processNotificationData(
                notificationTitle = data.notificationTitle,
                notificationDescription = data.notificationDescription,
                notificationImage = data.notificationImage,
                notificationTarget = data.notificationTarget,
                notificationMetadata = data.notificationMetaData
            )
        }
    }

    private fun processCommandData(
        @CommandParameterType command: String?,
        commandMetadata: String?
    ) {
        TODO("Not yet implemented")
    }

    private fun processNotificationData(
        notificationTitle: String?,
        notificationDescription: String?,
        notificationImage: String?,
        @NotificationTargetType notificationTarget: String?,
        notificationMetadata: String?
    ) {
        TODO("Not yet implemented")
    }
}