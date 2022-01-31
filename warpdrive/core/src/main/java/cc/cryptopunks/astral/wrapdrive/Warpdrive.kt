package cc.cryptopunks.astral.wrapdrive

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class Warpdrive :
    Application(),
    CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.IO) {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: Warpdrive private set
    }
}

val application get() = Warpdrive.instance
