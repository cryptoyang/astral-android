package cc.cryptopunks.wrapdrive

import android.app.Application
import cc.cryptopunks.wrapdrive.api.client.ping
import cc.cryptopunks.wrapdrive.api.network
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class Warpdrive :
    Application(),
    CoroutineScope {

    override val coroutineContext =
        SupervisorJob() + Executors.newSingleThreadExecutor().asCoroutineDispatcher()

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

val app get() = Warpdrive.instance

private suspend fun MutableStateFlow<Boolean>.subscribeConnectionStatus() {
    while (true) {
        network.ping().collect {
            emit(true)
        }
        emit(false)
        delay(1500)
    }
}
