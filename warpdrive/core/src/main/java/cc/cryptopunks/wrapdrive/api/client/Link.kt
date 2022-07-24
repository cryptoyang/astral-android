package cc.cryptopunks.wrapdrive.api.client

import cc.cryptopunks.astral.ext.byte
import cc.cryptopunks.astral.ext.query
import cc.cryptopunks.wrapdrive.api.Astral
import cc.cryptopunks.wrapdrive.api.Port
import kotlinx.coroutines.flow.channelFlow

fun Astral.link() = channelFlow {
    runCatching {
        query(Port) {
            send(Unit)
            while (true)
                byte
        }
    }
}
