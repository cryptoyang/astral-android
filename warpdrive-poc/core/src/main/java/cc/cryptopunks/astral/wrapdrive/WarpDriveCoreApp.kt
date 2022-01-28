package cc.cryptopunks.astral.wrapdrive

import android.app.Application

open class WarpDriveCoreApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
}
