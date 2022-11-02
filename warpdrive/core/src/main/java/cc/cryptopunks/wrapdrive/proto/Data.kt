package cc.cryptopunks.wrapdrive.proto

typealias OfferId = String
typealias ResultCode = Byte

data class Offer(
    val peer: PeerId = "",
    val files: List<Info> = emptyList(),
    val create: Long = 0,

    val id: OfferId = "",
    val `in`: Boolean = true,
    val status: String = "",
    val update: Long = 0,
    val index: Int = -1,
    val progress: Long = 0,
) {
    val isIncoming get() = `in`
}

data class Status(
    val id: OfferId = "",
    val `in`: Boolean = true,
    val status: String = "",
    val update: Long = 0,
    val index: Int = -1,
    val progress: Long = 0,
) {
    val isIncoming get() = `in`
}

typealias Peers = Map<PeerId, Peer>
typealias PeerId = String

data class Peer(
    val id: PeerId = "",
    val alias: String = "",
    val mod: String = "",
)

data class PeerOffer(
    val peer: Peer = EmptyPeer,
    val offer: Offer = EmptyOffer,
)

data class Info(
    val uri: String = "",
    val size: Long = 0,
    val isDir: Boolean = false,
    val perm: Int = 0x755,
    val mime: String = "",
    val name: String = "",
)

const val StatusAwaiting = "awaiting"
const val StatusAccepted = "accepted"
const val StatusRejected = "rejected"
const val StatusProgress = "progress"
const val StatusCompleted = "completed"
const val StatusFailed = "failed"

const val PeerModAsk = ""
const val PeerModTrust = "trust"
const val PeerModBlock = "block"

val EmptyStatus = Status()
val EmptyOffer = Offer()
val EmptyPeer = Peer()
val EmptyInfo = Info()
val EmptyPeerOffer = PeerOffer()
