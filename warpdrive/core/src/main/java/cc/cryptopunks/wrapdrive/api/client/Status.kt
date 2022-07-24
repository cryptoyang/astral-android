package cc.cryptopunks.wrapdrive.api.client

import cc.cryptopunks.astral.ext.string8
import cc.cryptopunks.wrapdrive.api.Astral
import cc.cryptopunks.wrapdrive.api.OffersFilter
import cc.cryptopunks.wrapdrive.api.QueryStatus
import cc.cryptopunks.wrapdrive.api.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun Astral.status(filter: OffersFilter): Flow<Status> = channelFlow {
    val conn = withContext(Dispatchers.IO) {
        query(QueryStatus).apply {
            string8 = filter
            launch(Dispatchers.IO) {
                val offerType = Status::class.java
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
