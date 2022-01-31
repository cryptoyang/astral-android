package cc.cryptopunks.astral.wrapdrive.api.client

import cc.cryptopunks.astral.enc.EncNetwork
import cc.cryptopunks.astral.ext.byte
import cc.cryptopunks.astral.ext.query
import cc.cryptopunks.astral.ext.stringL8
import cc.cryptopunks.astral.wrapdrive.api.OfferId
import cc.cryptopunks.astral.wrapdrive.api.ResultCode
import cc.cryptopunks.astral.wrapdrive.api.SenSend

suspend fun EncNetwork.send(
    peerId: String,
    uri: String,
): Pair<OfferId, ResultCode> =
    query(SenSend) {
        println("peerId $peerId ${peerId.length}")
        stringL8 = peerId
        stringL8 = uri
        stringL8 to byte
    }
