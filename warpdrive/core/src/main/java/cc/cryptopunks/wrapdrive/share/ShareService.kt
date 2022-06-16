package cc.cryptopunks.wrapdrive.share

import android.net.Uri
import cc.cryptopunks.astral.err.AstralLocalConnectionException
import cc.cryptopunks.astral.service.bluetooth.parseHex
import cc.cryptopunks.wrapdrive.api.Peer
import cc.cryptopunks.wrapdrive.api.client.bluetoothPeers
import cc.cryptopunks.wrapdrive.api.client.dial
import cc.cryptopunks.wrapdrive.api.client.peers
import cc.cryptopunks.wrapdrive.api.client.send
import cc.cryptopunks.wrapdrive.api.network
import cc.cryptopunks.wrapdrive.warpdrive
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

fun ShareModel.refresh() = launch {
    isRefreshing.emit(true)
    refresh.emit(Unit)
}

fun ShareModel.subscribeShare() = launch {
    share.collect { peer ->
        val uri = uri.value
        warpdrive.launch {
            share(peer, uri)
        }
    }
}

private suspend fun share(peer: Peer, uri: Uri) {
    try {
        val peerId = when (peer.network) {
            "astral" -> peer.id
            "bt" -> network.dial(peer.network, peer.id.parseHex())
            else -> throw IllegalArgumentException("Unsupported network type ${peer.network}")
        }

        isSharing.value = true
        val result = withTimeout(5000) {
            network.send(
                peerId = peerId,
                uri = uri.toString()
            )
        }
        sharingStatus.emit(Result.success(result))
    } catch (e: Throwable) {
        sharingStatus.emit(Result.failure(e))
    } finally {
        isSharing.value = false
    }
}

fun ShareModel.subscribePeers() {
    peersJob.cancel()
    peersJob = launch {
        merge(
            subscribeAstralPeers(),
            bluetoothPeers().map(::listOf),
        ).scan(emptyMap<String, Peer>()) { acc, peers ->
            acc + peers.associateBy { it.id }
        }.collect {
            peers.value = it.values.toList()
        }
    }
}

private fun ShareModel.subscribeAstralPeers() =
    merge(
        refresh.debounce(500),
        trigger(),
//            ticker()
    ).mapNotNull {
        try {
            val list = network.peers()
            isRefreshing.emit(false)
            list.sortedBy(Peer::id)
        } catch (e: Throwable) {
            if (e is AstralLocalConnectionException) {
                throw e
            } else {
                error.value = e
                null
            }
        }
    }

fun trigger() = flowOf(Unit)

private fun ticker(
    delay: Long = 3000,
) = flow {
    while (true) {
        delay(delay)
        emit(Unit)
    }
}
