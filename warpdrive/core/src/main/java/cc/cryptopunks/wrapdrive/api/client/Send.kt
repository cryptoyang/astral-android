package cc.cryptopunks.wrapdrive.api.client

import cc.cryptopunks.astral.ext.byte
import cc.cryptopunks.astral.ext.query
import cc.cryptopunks.astral.ext.string8
import cc.cryptopunks.wrapdrive.api.Astral
import cc.cryptopunks.wrapdrive.api.OfferId
import cc.cryptopunks.wrapdrive.api.QuerySend
import cc.cryptopunks.wrapdrive.api.ResultCode

suspend fun Astral.send(
    peerId: String,
    uri: String,
): Pair<OfferId, ResultCode> =
    query(QuerySend) {
        string8 = peerId
        string8 = uri
        string8 to byte
    }
