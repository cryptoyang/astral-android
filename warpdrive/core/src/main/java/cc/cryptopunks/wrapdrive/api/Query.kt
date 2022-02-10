package cc.cryptopunks.wrapdrive.api

const val Port = "warpdrive"

const val QueryPeers     = "$Port/peers"
const val QuerySend      = "$Port/send"
const val QueryAccept    = "$Port/accept"
const val QuerySubscribe = "$Port/subscribe"
const val QueryStatus    = "$Port/status"
const val QueryOffers    = "$Port/offers"
const val QueryUpdate    = "$Port/update"

const val FilterAll = "all"
const val FilterIn = "in"
const val FilterOut = "out"
