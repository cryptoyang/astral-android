package cc.cryptopunks.astral.wrapdrive.api.client

import cc.cryptopunks.astral.enc.EncNetwork
import cc.cryptopunks.astral.ext.byte
import cc.cryptopunks.astral.ext.decodeList
import cc.cryptopunks.astral.ext.queryResult
import cc.cryptopunks.astral.ext.stringL8
import cc.cryptopunks.astral.wrapdrive.api.Offer
import cc.cryptopunks.astral.wrapdrive.api.OffersFilter
import cc.cryptopunks.astral.wrapdrive.api.QueryOffers

suspend fun EncNetwork.offers(filter: OffersFilter): List<Offer> =
    queryResult(QueryOffers) {
        stringL8 = filter
        result = decodeList()
        byte = 0
    }
