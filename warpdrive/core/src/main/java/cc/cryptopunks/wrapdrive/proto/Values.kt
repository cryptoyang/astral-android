package cc.cryptopunks.wrapdrive.proto

const val WarpdrivePort = "warpdrive"

const val CmdClose: Byte = 0xff.toByte()
const val CmdListPeers: Byte = 1
const val CmdCreateOffer: Byte = 2
const val CmdAcceptOffer: Byte = 3
const val CmdListOffers: Byte = 4
const val CmdListenOffers: Byte = 5
const val CmdListenStatus: Byte = 6
const val CmdUpdate: Byte = 7
const val CmdPing: Byte = 200.toByte()

typealias Filter = Byte

const val FilterAll: Filter = 0
const val FilterIn: Filter = 1
const val FilterOut: Filter = 2
