package cc.cryptopunks.wrapdrive.proto

import cc.cryptopunks.astral.client.enc.StreamEncoder
import cc.cryptopunks.astral.client.ext.byte
import cc.cryptopunks.astral.client.ext.decodeList
import cc.cryptopunks.astral.client.ext.queryResult
import cc.cryptopunks.astral.client.ext.string8
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import java.net.SocketException
import kotlin.system.measureNanoTime
import kotlin.time.Duration.Companion.seconds

suspend fun Astral.offers(
    filter: Filter,
): List<Offer> = queryResult(PortLocal) {
    byte = CmdListOffers
    byte = filter
    result = decodeList()
    end()
}

suspend fun Astral.accept(
    offerId: OfferId,
): Unit = queryResult(PortLocal) {
    byte = CmdAcceptOffer
    string8 = offerId
    byte
    end()
}

suspend fun Astral.send(
    peerId: String,
    uri: String,
): Pair<OfferId, ResultCode> = queryResult(PortLocal) {
    byte = CmdCreateOffer
    string8 = peerId
    string8 = uri
    result = string8 to byte
    end()
}

suspend fun Astral.peers(
): List<Peer> = queryResult(PortLocal) {
    byte = CmdListPeers
    result = decodeList()
    end()
}


fun Astral.ping(
): Flow<Long> = channelFlow {
    val conn = withContext(Dispatchers.IO) {
        query(PortInfo)
    }
    conn.runCatching {
        byte = CmdPing
        var code: Byte = 1
        while (isActive) {
            val time = measureNanoTime {
                withTimeout(3.seconds) {
                    byte = code
                    code = byte
                }
            }
            send(time)
            delay(1.seconds)
        }
    }
    awaitClose {
        closeSubscriptionScope.launch {
            runCatching {
                withTimeoutOrNull(5000) {
                    conn.byte = 0
                    conn.end()
                }
            }
            runCatching {
                conn.close()
            }
        }
    }
}

fun Astral.status(filter: Filter): Flow<Status> = filteredFlow(CmdListenStatus, filter)

fun Astral.subscribe(filter: Filter): Flow<Offer> = filteredFlow(CmdListenOffers, filter)

private inline fun <reified T> Astral.filteredFlow(
    cmd: Byte,
    filter: Filter,
): Flow<T> = channelFlow {
    val conn = withContext(Dispatchers.IO) {
        query(PortLocal)
    }
    launch(Dispatchers.IO) {
        conn.apply {
            byte = cmd
            byte = filter
        }
        val reader = conn.input.bufferedReader()
        val type = T::class.java
        runCatching {
            while (isActive) {
                val line = reader.readLine() ?: break
                val item = encoder.decode(line, type)
                trySend(item)
            }
        }.onFailure {
            if (it !is SocketException)
                it.printStackTrace()
        }
    }
    awaitClose {
        closeSubscriptionScope.launch {
            runCatching {
                withTimeoutOrNull(5000) {
                    conn.byte = 0
                    conn.end()
                }
            }
            runCatching {
                conn.close()
            }
        }
    }
}

fun StreamEncoder.end() {
    byte = CmdClose
}

private val closeSubscriptionScope = CoroutineScope(
    SupervisorJob() + Dispatchers.IO.limitedParallelism(8)
)
