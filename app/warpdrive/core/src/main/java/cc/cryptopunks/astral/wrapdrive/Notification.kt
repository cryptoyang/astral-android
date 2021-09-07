package cc.cryptopunks.astral.wrapdrive

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cc.cryptopunks.astral.wrapdrive.util.formatSize

object Notify {

    data class Group(
        val name: String,
    )

    data class Item(
        val id: Int,
        val name: String,
        val size: Long,
    )

    interface Manager {
        fun init(group: Group, items: List<Item>)
        fun start(id: Int, indeterminate: Boolean)
        fun setProgress(id: Int, progress: Long)
        fun finish(id: Int, rejected: Boolean)
        fun finishGroup()
    }
}

fun Context.notifyManager(): Notify.Manager = NotifyManager(this)

fun Context.createNotificationChannel() {
    val channel = NotificationChannelCompat.Builder(
        SENDING_CHANNEL_ID,
        NotificationManagerCompat.IMPORTANCE_DEFAULT
    ).setName("Warp Drive sharing").build()
    NotificationManagerCompat.from(this).createNotificationChannel(channel)
}

private const val SENDING_CHANNEL_ID = "warpDriveSharing"

private class NotifyManager(
    private val context: Context,
) : Notify.Manager {

    private lateinit var header: NotificationCompat.Builder
    private lateinit var builders: Map<Int, NotificationCompat.Builder>
    private lateinit var items: Map<Int, Notify.Item>
    private val notificationManager = NotificationManagerCompat.from(context)

    private object Status {
        const val WAITING = "Waiting"
        const val SENDING = "Sending"
        const val FINISHED = "Sent"
        const val REJECTED = "Rejected"
    }

    override fun init(group: Notify.Group, items: List<Notify.Item>) {
        this.items = items.associateBy(Notify.Item::id)
        builders = items.associate { item ->
            item.id to NotificationCompat
                .Builder(context, SENDING_CHANNEL_ID)
                .setSubText("${Status.WAITING} - ${formatSize(item.size)}")
                .setContentTitle(item.name)
                .setSmallIcon(android.R.drawable.ic_menu_send)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setGroup(group.name)
        }.onEach { (id, builder) ->
            builder.notify(id)
        }
        header = NotificationCompat
            .Builder(context, SENDING_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_send)
            .setOngoing(true)
            .setGroup(group.name)
            .setGroupSummary(true)
            .setOnlyAlertOnce(true)
            .notify(0)
    }

    override fun start(id: Int, indeterminate: Boolean) {
        val size = items.getValue(id).size
        builders.getValue(id)
            .setSubText("${Status.SENDING} - ${formatSize(size)}")
            .setProgress(
                if (indeterminate) Int.MAX_VALUE
                else size.toInt(),
                0,
                size < 0
            )
            .notify(id)
    }

    override fun setProgress(id: Int, progress: Long) {
        val size = items.getValue(id).size
        builders.getValue(id)
            .setSubText("${Status.SENDING} - ${formatSize(progress)}/${formatSize(size)}")
            .setProgress(
                size.toInt(),
                progress.toInt(),
                size < 0
            )
            .notify(id)
    }

    override fun finish(id: Int, rejected: Boolean) {
        val size = items.getValue(id).size
        builders.getValue(id)
            .setSubText("${if (rejected) Status.REJECTED else Status.FINISHED} - ${formatSize(size)}")
            .setOngoing(false)
            .setProgress(0, 0, false)
            .notify(id)
    }

    override fun finishGroup() {
        header.setOngoing(false).notify(0)
    }

    private fun NotificationCompat.Builder.notify(id: Int) = apply {
        notificationManager.notify(id, build())
    }
}
