package cc.cryptopunks.astral.wrapdrive.peer

data class PeerItem(
    val nodeId: String = ""
) {
    companion object {
        var Empty = PeerItem()
    }
}

typealias OnPeerSelected = (PeerItem) -> Unit
