package cc.cryptopunks.wrapdrive.api.client

import cc.cryptopunks.astral.enc.EncNetwork
import cc.cryptopunks.astral.ext.byte
import cc.cryptopunks.astral.ext.decodeList
import cc.cryptopunks.astral.ext.queryResult
import cc.cryptopunks.wrapdrive.api.Peer
import cc.cryptopunks.wrapdrive.api.QueryPeers

suspend fun EncNetwork.peers(): List<Peer> =
    queryResult(QueryPeers) {
        result = decodeList()
        byte = 0
    }
