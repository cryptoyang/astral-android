package cc.cryptopunks.wrapdrive.model

import android.net.Uri
import cc.cryptopunks.astral.client.err.AstralLocalConnectionException
import cc.cryptopunks.wrapdrive.app
import cc.cryptopunks.wrapdrive.proto.OfferId
import cc.cryptopunks.wrapdrive.proto.Peer
import cc.cryptopunks.wrapdrive.proto.ResultCode
import cc.cryptopunks.wrapdrive.proto.network
import cc.cryptopunks.wrapdrive.proto.peers
import cc.cryptopunks.wrapdrive.proto.send
import cc.cryptopunks.wrapdrive.util.CoroutineViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class ShareModel : CoroutineViewModel() {

    // state
    val error = MutableStateFlow(null as Throwable?)
    val uri = MutableStateFlow(Uri.EMPTY to 0L)
    val peers = MutableStateFlow(emptyList<Peer>())
    val isRefreshing = MutableStateFlow(false)
    var peersJob = Job() as Job

    // actions
    val refresh = MutableSharedFlow<Unit>()
    val share = MutableSharedFlow<String>(extraBufferCapacity = 1)

    init {
        subscribeShare()
    }
}

fun ShareModel.setUri(uri: Uri) {
    this.uri.value = uri to System.currentTimeMillis()
}

val isSharing = MutableStateFlow(false)
val sharingStatus = MutableSharedFlow<Result<Pair<OfferId, ResultCode>>>(
    extraBufferCapacity = Byte.MAX_VALUE.toInt()
)

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

fun share(peerId: String, uri: Uri) = app.launch {
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
