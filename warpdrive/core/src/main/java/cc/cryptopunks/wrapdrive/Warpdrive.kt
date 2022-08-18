package cc.cryptopunks.wrapdrive

import android.app.Application
import cc.cryptopunks.wrapdrive.proto.network
import cc.cryptopunks.wrapdrive.proto.ping
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
        network.runCatching {
            ping().collect {
                emit(true)
            }
        }.onFailure { e ->
            println("Cannot connect warpdrive cause: ${e.message}")
        }
        emit(false)
        delay(1500)
    }
}
