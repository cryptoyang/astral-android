package cc.cryptopunks.astral.wrapdrive.api.client

import cc.cryptopunks.astral.enc.EncNetwork
import cc.cryptopunks.astral.ext.readMessage
import cc.cryptopunks.astral.ext.stringL8
import cc.cryptopunks.astral.wrapdrive.api.Offer
import cc.cryptopunks.astral.wrapdrive.api.OffersFilter
import cc.cryptopunks.astral.wrapdrive.api.QuerySubscribe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun EncNetwork.subscribe(filter: OffersFilter): Flow<Offer> = channelFlow {
    val conn = withContext(Dispatchers.IO) {
        query(QuerySubscribe).apply {
            stringL8 = filter
            launch {
                val offerType = Offer::class.java
                while (true) {
                    val line = async(Dispatchers.IO) { readMessage() }.await() ?: continue
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
