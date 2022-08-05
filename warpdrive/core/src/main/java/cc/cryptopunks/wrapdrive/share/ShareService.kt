package cc.cryptopunks.wrapdrive.share

import android.net.Uri
import androidx.compose.material.SnackbarHostState
import cc.cryptopunks.astral.contacts.ContactsModel
import cc.cryptopunks.astral.err.AstralLocalConnectionException
import cc.cryptopunks.wrapdrive.api.Peer
import cc.cryptopunks.wrapdrive.api.client.peers
import cc.cryptopunks.wrapdrive.api.client.send
import cc.cryptopunks.wrapdrive.api.network
import cc.cryptopunks.wrapdrive.warpdrive
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

fun ShareModel.refresh() = launch {
    isRefreshing.emit(true)
    refresh.emit(Unit)
}

fun ShareModel.subscribeShare() = launch {
    share.collect { peerId ->
        val (uri) = uri.value
        share(peerId, uri)
    }
}

fun share(peerId: String, uri: Uri) = warpdrive.launch {
    try {
        isSharing.value = true
        val result = withTimeout(10000) {
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
            refresh.debounce(500),
            flowOf(Unit),
//            ticker()
        ).collect {
            try {
                val list = network.peers()
                peers.value = list.sortedBy(Peer::id)
                isRefreshing.emit(false)
            } catch (e: Throwable) {
                if (e is AstralLocalConnectionException) {
                    throw e
                } else {
                    error.value = e
                }
            }
        }
    }
}

private fun ticker(
    delay: Long = 3000,
) = flow {
    while (true) {
        delay(delay)
        emit(Unit)
    }
}
