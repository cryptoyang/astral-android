package cc.cryptopunks.wrapdrive

import android.app.Application
import cc.cryptopunks.wrapdrive.proto.warpdriveStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow

open class Warpdrive :
    Application(),
    CoroutineScope {

    override val coroutineContext = SupervisorJob()

    val isConnected: StateFlow<Boolean> = warpdriveStatus()

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}

private lateinit var instance: Warpdrive

val app get() = instance
