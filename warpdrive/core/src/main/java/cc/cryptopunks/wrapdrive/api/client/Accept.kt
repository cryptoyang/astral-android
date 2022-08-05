package cc.cryptopunks.wrapdrive.api.client

import cc.cryptopunks.astral.ext.byte
import cc.cryptopunks.astral.ext.query
import cc.cryptopunks.astral.ext.string8
import cc.cryptopunks.wrapdrive.api.Astral
import cc.cryptopunks.wrapdrive.api.CmdAccept
import cc.cryptopunks.wrapdrive.api.OfferId
import cc.cryptopunks.wrapdrive.api.Port

suspend fun Astral.accept(offerId: OfferId): Unit =
    query(Port) {
        byte = CmdAccept
        string8 = offerId
        byte
    }
