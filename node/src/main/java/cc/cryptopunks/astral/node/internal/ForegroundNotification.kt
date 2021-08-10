package cc.cryptopunks.astral.node.internal

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.content.getSystemService
import cc.cryptopunks.astral.node.MainActivity
import cc.cryptopunks.astral.node.R

internal fun Service.startForegroundNotification() {
    val channelId = createNotificationChannel()

    val pendingIntent: PendingIntent =
        Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }

    val notification: Notification = Notification.Builder(this, channelId)
        .setContentTitle("Astral")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentIntent(pendingIntent)
        .build()

    // Notification ID cannot be 0.
    startForeground(1, notification)
}


private fun Context.createNotificationChannel(): String =
    NotificationChannel(
        "astral",
        "Astral Service",
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        lightColor = Color.BLUE
        importance = NotificationManager.IMPORTANCE_NONE
        lockscreenVisibility = Notification.VISIBILITY_PRIVATE
    }.also { channel ->
        requireNotNull(getSystemService<NotificationManager>())
            .createNotificationChannel(channel)
    }.id
