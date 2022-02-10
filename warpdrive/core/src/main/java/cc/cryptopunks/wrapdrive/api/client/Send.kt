package cc.cryptopunks.wrapdrive.api.client

import cc.cryptopunks.astral.enc.EncNetwork
import cc.cryptopunks.astral.ext.byte
import cc.cryptopunks.astral.ext.query
import cc.cryptopunks.astral.ext.stringL8
import cc.cryptopunks.wrapdrive.api.OfferId
import cc.cryptopunks.wrapdrive.api.QuerySend
import cc.cryptopunks.wrapdrive.api.ResultCode

suspend fun EncNetwork.send(
    peerId: String,
    uri: String,
): Pair<OfferId, ResultCode> =
    query(QuerySend) {
        stringL8 = peerId
        stringL8 = uri
        stringL8 to byte
    }
