package cc.cryptopunks.wrapdrive.api

const val Port = "warpdrive"

const val CmdClose: Byte = 0
const val CmdPeers: Byte = 1
const val CmdSend: Byte = 2
const val CmdAccept: Byte = 3
const val CmdUpdate: Byte = 4
const val CmdSubscribe: Byte = 5
const val CmdStatus: Byte = 6
const val CmdOffers: Byte = 7

const val FilterAll = "all"
const val FilterIn = "in"
const val FilterOut = "out"
