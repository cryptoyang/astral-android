package cc.cryptopunks.wrapdrive.api.client

import cc.cryptopunks.astral.enc.EncNetwork
import cc.cryptopunks.astral.ext.stringL8
import cc.cryptopunks.wrapdrive.api.OffersFilter
import cc.cryptopunks.wrapdrive.api.QueryStatus
import cc.cryptopunks.wrapdrive.api.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun EncNetwork.status(filter: OffersFilter): Flow<Status> = channelFlow {
    val conn = withContext(Dispatchers.IO) {
        query(QueryStatus).apply {
            stringL8 = filter
            launch {
                val offerType = Status::class.java
                val reader = input.bufferedReader()
                while (true) {
                    val line = async(Dispatchers.IO) { reader.readLine() }.await() ?: continue
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
