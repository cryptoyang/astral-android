package cc.cryptopunks.astral.wrapdrive

import android.app.Application
import cc.cryptopunks.astral.wrapdrive.api.client.link
import cc.cryptopunks.astral.wrapdrive.api.network
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class Warpdrive :
    Application(),
    CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.IO) {

    val isConnected = MutableStateFlow(false)

    override fun onCreate() {
        super.onCreate()
        instance = this
        launch { isConnected.subscribeConnectionStatus() }
    }

    companion object {
        lateinit var instance: Warpdrive private set
    }
}

val warpdrive get() = Warpdrive.instance

private suspend fun MutableStateFlow<Boolean>.subscribeConnectionStatus() {
    while (true) {
        network.link().collect {
            emit(true)
        }
        emit(false)
        delay(1500)
    }
}
