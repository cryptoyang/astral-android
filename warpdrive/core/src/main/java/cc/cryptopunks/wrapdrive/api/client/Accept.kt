package cc.cryptopunks.wrapdrive.api.client

import cc.cryptopunks.astral.enc.EncNetwork
import cc.cryptopunks.astral.ext.byte
import cc.cryptopunks.astral.ext.query
import cc.cryptopunks.astral.ext.stringL8
import cc.cryptopunks.wrapdrive.api.OfferId
import cc.cryptopunks.wrapdrive.api.QueryAccept

suspend fun EncNetwork.accept(offerId: OfferId): Unit =
    query(QueryAccept) {
        stringL8 = offerId
        byte
    }
