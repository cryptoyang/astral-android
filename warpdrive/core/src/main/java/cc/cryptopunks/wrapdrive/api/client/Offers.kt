package cc.cryptopunks.wrapdrive.api.client

import cc.cryptopunks.astral.ext.byte
import cc.cryptopunks.astral.ext.decodeList
import cc.cryptopunks.astral.ext.queryResult
import cc.cryptopunks.astral.ext.string8
import cc.cryptopunks.wrapdrive.api.Astral
import cc.cryptopunks.wrapdrive.api.Offer
import cc.cryptopunks.wrapdrive.api.OffersFilter
import cc.cryptopunks.wrapdrive.api.QueryOffers

suspend fun Astral.offers(filter: OffersFilter): List<Offer> =
    queryResult(QueryOffers) {
        string8 = filter
        result = decodeList()
        byte = 0
    }
