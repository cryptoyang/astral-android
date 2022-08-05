package cc.cryptopunks.wrapdrive.api.client

import cc.cryptopunks.astral.ext.byte
import cc.cryptopunks.astral.ext.decodeList
import cc.cryptopunks.astral.ext.queryResult
import cc.cryptopunks.wrapdrive.api.Astral
import cc.cryptopunks.wrapdrive.api.CmdPeers
import cc.cryptopunks.wrapdrive.api.Peer
import cc.cryptopunks.wrapdrive.api.Port

suspend fun Astral.peers(): List<Peer> =
    queryResult(Port) {
        byte = CmdPeers
        result = decodeList()
        byte = 0
    }
