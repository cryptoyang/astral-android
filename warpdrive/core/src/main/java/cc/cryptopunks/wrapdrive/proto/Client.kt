package cc.cryptopunks.wrapdrive.proto

import cc.cryptopunks.astral.client.ext.byte
import cc.cryptopunks.astral.client.ext.decodeList
import cc.cryptopunks.astral.client.ext.query
import cc.cryptopunks.astral.client.ext.queryResult
import cc.cryptopunks.astral.client.ext.string8
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    filter: OffersFilter,
): List<Offer> = queryResult(Port) {
    byte = CmdOffers
    string8 = filter
    result = decodeList()
    byte = 0
}

suspend fun Astral.accept(
    offerId: OfferId,
): Unit = query(Port) {
    byte = CmdAccept
    string8 = offerId
    byte
}

suspend fun Astral.send(
    peerId: String,
    uri: String,
): Pair<OfferId, ResultCode> = query(Port) {
    byte = CmdSend
    string8 = peerId
    string8 = uri
    string8 to byte
}

suspend fun Astral.peers(
): List<Peer> = queryResult(Port) {
    byte = CmdPeers
    result = decodeList()
    byte = 0
}


fun Astral.ping(
): Flow<Long> = channelFlow {
    query(Port) {
        byte = CmdPing
        var code: Byte = 0
        while (true) {
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
}

fun Astral.status(
    filter: OffersFilter,
): Flow<Status> = filteredFlow(CmdStatus, filter)

fun Astral.subscribe(
    filter: OffersFilter,
): Flow<Offer> = filteredFlow(CmdSubscribe, filter)

private inline fun <reified T> Astral.filteredFlow(
    cmd: Byte,
    filter: OffersFilter,
): Flow<T> = channelFlow {
    val conn = withContext(Dispatchers.IO) {
        query(Port)
    }
    launch(Dispatchers.IO) {
        conn.apply {
            byte = cmd
            string8 = filter
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
        CoroutineScope(Dispatchers.IO).launch {
            withTimeoutOrNull(1000) { conn.byte = 0 }
            runCatching { conn.close() }
        }
    }
}
