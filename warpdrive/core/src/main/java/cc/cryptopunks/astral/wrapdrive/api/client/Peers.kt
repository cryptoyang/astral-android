package cc.cryptopunks.astral.wrapdrive.api.client

import cc.cryptopunks.astral.enc.EncNetwork
import cc.cryptopunks.astral.ext.byte
import cc.cryptopunks.astral.ext.decodeArray
import cc.cryptopunks.astral.ext.query
import cc.cryptopunks.astral.wrapdrive.api.Peer
import cc.cryptopunks.astral.wrapdrive.api.SenPeers

suspend fun EncNetwork.peers(): Array<Peer> =
    query(SenPeers) {
        val peers = decodeArray<Peer>()
        byte = 0
        peers
    }
