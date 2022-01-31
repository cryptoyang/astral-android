package cc.cryptopunks.astral.wrapdrive.api.client

import cc.cryptopunks.astral.enc.EncNetwork
import cc.cryptopunks.astral.ext.byte
import cc.cryptopunks.astral.ext.decodeMap
import cc.cryptopunks.astral.ext.query
import cc.cryptopunks.astral.wrapdrive.api.Offers
import cc.cryptopunks.astral.wrapdrive.api.RecReceived
import cc.cryptopunks.astral.wrapdrive.api.SenSent

suspend fun EncNetwork.sent(): Offers =
    query(SenSent) {
        val offers: Offers = decodeMap()
        byte = 0
        offers
    }

suspend fun EncNetwork.received(): Offers =
    query(RecReceived) {
        val offers: Offers = decodeMap()
        byte = 0
        offers
    }
