package me.abolfazl.nmock.utils.managers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import me.abolfazl.nmock.R

object NotificationManager {

    private const val DRAWABLE_DEF = "drawable"

    fun createForegroundNotification(
        context: Context,
        channelId: String,
        channelDescription: String,
        @DrawableRes smallIcon: Int,
        title: String,
        description: String,
        onGoing: Boolean,
        autoCancel: Boolean,
    ): Notification {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .setOngoing(onGoing)
            .setAutoCancel(autoCancel)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                context.resources.getString(R.string.applicationNotificationChannelName),
                NotificationManager.IMPORTANCE_NONE
            )
            notificationChannel.description = channelDescription
            notificationManager.createNotificationChannel(notificationChannel)
        }
        return notification
    }

    fun createPushNotification(
        context: Context,
        channelId: String,
        title: String,
        description: String?,
        @DrawableRes smallIcon: Int
    ): Notification {
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setSmallIcon(smallIcon)

        description?.let { text ->
            notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(text))
        }
        return notificationBuilder.build()
    }

    fun createPushNotificationChannel(
        context: Context,
        silentChannel: Boolean,
        channelName: String,
        description: String
    ): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId =
                if (silentChannel) context.resources.getString(R.string.silentPushNotificationChannelId)
                else context.resources.getString(R.string.pushNotificationChannelId)
            val channelImportance =
                if (silentChannel) NotificationManager.IMPORTANCE_LOW
                else NotificationManager.IMPORTANCE_HIGH

            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                channelImportance
            )
            notificationChannel.description = description

            if (!silentChannel) {
                notificationChannel.enableVibration(true)
                notificationChannel.enableLights(true)
                notificationChannel.lightColor = Color.YELLOW
            }
            notificationManager.createNotificationChannel(notificationChannel)
            return notificationChannel.id
        }
        return context.resources.getString(R.string.pushNotificationChannelId)
    }

    @DrawableRes
    fun getSmallIconFromNotificationData(
        rawIconData: String?,
        context: Context
    ): Int {
        return if (rawIconData != null) {
            val drawableRes =
                context.resources.getIdentifier(rawIconData, DRAWABLE_DEF, context.packageName)
            if (drawableRes == 0) R.drawable.nmock_logo_notifcation else drawableRes
        } else {
            R.drawable.nmock_logo_notifcation
        }
    }
}