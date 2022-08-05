package cc.cryptopunks.wrapdrive.api.client

import cc.cryptopunks.astral.enc.NetworkEncoder
import cc.cryptopunks.astral.ext.byte
import cc.cryptopunks.astral.ext.string8
import cc.cryptopunks.wrapdrive.api.Offer
import cc.cryptopunks.wrapdrive.api.OffersFilter
import cc.cryptopunks.wrapdrive.api.Port
import cc.cryptopunks.wrapdrive.api.CmdSubscribe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun NetworkEncoder.subscribe(filter: OffersFilter): Flow<Offer> = channelFlow {
    val conn = withContext(Dispatchers.IO) {
        query(Port).apply {
            byte = CmdSubscribe
            string8 = filter
            launch(Dispatchers.IO) {
                val offerType = Offer::class.java
                val reader = input.bufferedReader()
                while (true) {
                    val line = reader.readLine() ?: break
                    val offer = encoder.decode(line, offerType)
                    trySend(offer)
                }
            }
        }
    }
    awaitClose {
        conn.close()
    }
}
