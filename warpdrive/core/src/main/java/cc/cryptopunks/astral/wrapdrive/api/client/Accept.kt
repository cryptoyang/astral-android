package cc.cryptopunks.astral.wrapdrive.api.client

import cc.cryptopunks.astral.enc.EncNetwork
import cc.cryptopunks.astral.ext.byte
import cc.cryptopunks.astral.ext.query
import cc.cryptopunks.astral.ext.stringL8
import cc.cryptopunks.astral.wrapdrive.api.OfferId
import cc.cryptopunks.astral.wrapdrive.api.RecAccept

suspend fun EncNetwork.accept(offerId: OfferId): Unit =
    query(RecAccept) {
        stringL8 = offerId
        byte
    }
