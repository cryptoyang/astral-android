package cc.cryptopunks.astral.wrapdrive.api


typealias Offers = Map<OfferId, Offer>
typealias OfferId = String
typealias ResultCode = Byte

data class Offer(
    val id: OfferId,
    val status: String,
    val peer: PeerId,
    val files: List<Info>,
)


data class Status(
    val id: OfferId,
    val status: String,
)

typealias Peers = Map<PeerId, Peer>
typealias PeerId = String

data class Peer(
    val id: PeerId,
    val alias: String,
    val mod: String,
)

data class Info(
    val uri: String,
    val size: Long,
    val isDir: Boolean = false,
    val perm: Int = 0x755,
    val mime: String = "",
    val progress: Long = 0,
)

const val StatusAdded = ""

const val StatusAccepted = "accepted"

const val StatusRejected = "rejected"
const val StatusProgress = "progress"
const val StatusFailed = "failed"
const val StatusCompleted = "completed"
const val StatusAborted = "aborted"
const val PeerModAsk = ""
const val PeerModTrust = "trust"

const val PeerModBlock = "block"

val EmptyStatus = Status("", "")
val EmptyOffer = Offer("", "", "", emptyList())
val EmptyPeer = Peer("", "", "")
val EmptyInfo = Info("", 0)
