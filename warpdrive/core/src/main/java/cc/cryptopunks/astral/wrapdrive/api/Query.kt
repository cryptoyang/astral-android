package cc.cryptopunks.astral.wrapdrive.api

const val Port = "warpdrive"

const val SenPeers  = "$Port/sender/peers"
const val SenSend   = "$Port/sender/send"
const val SenStatus = "$Port/sender/status"
const val SenSent   = "$Port/sender/sent"
const val SenEvents = "$Port/sender/events"

const val RecIncoming = "$Port/recipient/incoming"
const val RecReceived = "$Port/recipient/received"
const val RecAccept   = "$Port/recipient/accept"
const val RecReject   = "$Port/recipient/reject"
const val RecUpdate   = "$Port/recipient/update"
const val RecEvents   = "$Port/recipient/events"

