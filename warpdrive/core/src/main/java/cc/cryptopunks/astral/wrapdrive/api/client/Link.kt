package cc.cryptopunks.astral.wrapdrive.api.client

import cc.cryptopunks.astral.enc.EncNetwork
import cc.cryptopunks.astral.ext.byte
import cc.cryptopunks.astral.ext.query
import cc.cryptopunks.astral.wrapdrive.api.Port
import kotlinx.coroutines.flow.channelFlow

fun EncNetwork.link() = channelFlow {
    runCatching {
        query(Port) {
            send(Unit)
            while (true)
                byte
        }
    }
}
