package cc.cryptopunks.wrapdrive.api.client

import cc.cryptopunks.astral.ext.byte
import cc.cryptopunks.astral.ext.query
import cc.cryptopunks.astral.ext.string8
import cc.cryptopunks.wrapdrive.api.Astral
import cc.cryptopunks.wrapdrive.api.OfferId
import cc.cryptopunks.wrapdrive.api.Port
import cc.cryptopunks.wrapdrive.api.ResultCode
import cc.cryptopunks.wrapdrive.api.CmdSend

suspend fun Astral.send(
    peerId: String,
    uri: String,
): Pair<OfferId, ResultCode> =
    query(Port) {
        byte = CmdSend
        string8 = peerId
        string8 = uri
        string8 to byte
    }
