package cc.cryptopunks.wrapdrive.api.client

import cc.cryptopunks.astral.ext.byte
import cc.cryptopunks.astral.ext.string8
import cc.cryptopunks.wrapdrive.api.Astral
import cc.cryptopunks.wrapdrive.api.CmdStatus
import cc.cryptopunks.wrapdrive.api.CmdSubscribe
import cc.cryptopunks.wrapdrive.api.Offer
import cc.cryptopunks.wrapdrive.api.OffersFilter
import cc.cryptopunks.wrapdrive.api.Port
import cc.cryptopunks.wrapdrive.api.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.net.SocketException


fun Astral.status(filter: OffersFilter): Flow<Status> =
    filteredFlow(CmdStatus, filter)

fun Astral.subscribe(filter: OffersFilter): Flow<Offer> =
    filteredFlow(CmdSubscribe, filter)

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
