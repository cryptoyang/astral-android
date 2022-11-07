package cc.cryptopunks.wrapdrive.proto

import cc.cryptopunks.astral.client.Stream
import cc.cryptopunks.astral.client.enc.NetworkEncoder
import cc.cryptopunks.astral.client.enc.StreamEncoder
import cc.cryptopunks.astral.client.enc.encoder
import cc.cryptopunks.astral.client.ext.byte
import cc.cryptopunks.astral.client.tcp.astralTcpNetwork
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

val astral: NetworkEncoder = astralTcpNetwork().encoder()

fun warpdrive(): StreamEncoder = astral.connect(WarpdrivePort)

fun Stream.end() {
    byte = CmdClose
}

suspend fun <T> warpdrive(
    context: CoroutineContext = Dispatchers.IO,
    once: suspend StreamEncoder.() -> T,
): T =
    withContext(context) {
        warpdrive().use { stream ->
            stream.once().apply {
                stream.end()
            }
        }
    }

fun <R> warpdriveFlow(
    block: StreamEncoder.() -> () -> R,
) =
    channelFlow {
        warpdrive().run {
            var ok = false
            launch {
                val next = block()
                while (isActive) try {
                    val data = next()
                    trySend(data)
                } catch (e: Throwable) {
                    if (!ok) throw e
                }
            }
            awaitClose {
                closeSubscriptionScope.launch {
                    use {
                        unsubscribe()
                        end()
                        ok = true
                    }
                }
            }
        }
    }

private val closeSubscriptionScope = CoroutineScope(
    SupervisorJob() + Dispatchers.IO.limitedParallelism(8) + CoroutineExceptionHandler { _, _ -> }
)

fun CoroutineScope.warpdriveStatus() = channelFlow {
    val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    while (true) {
        runCatching {
            warpdrive(context = dispatcher) {
                val call = ping()
                while (true) {
                    call(1)
                    send(true)
                    delay(1.seconds)
                }
            }
        }.onFailure { e ->
            println("Cannot connect warpdrive cause: ${e.message}")
        }
        send(false)
        delay(1.5.seconds)
    }
}.stateIn(this, SharingStarted.Eagerly, false)
