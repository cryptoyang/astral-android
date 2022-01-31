package cc.cryptopunks.astral.wrapdrive.share

import cc.cryptopunks.astral.wrapdrive.api.Peer
import cc.cryptopunks.astral.wrapdrive.api.client.peers
import cc.cryptopunks.astral.wrapdrive.api.network
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withTimeout

val peersFetching = MutableStateFlow(false)
val peersResult = MutableSharedFlow<Result<Array<Peer>>>(
    extraBufferCapacity = Byte.MAX_VALUE.toInt())

suspend fun loadPeers() {
    try {
        peersFetching.value = true
        val peers = withTimeout(3000) {
            network.peers()
        }
        peersResult.emit(Result.success(peers))
    } catch (e: Throwable) {
        e.printStackTrace()
        peersResult.emit(Result.failure(e))
    } finally {
        peersFetching.value = false
    }
}
