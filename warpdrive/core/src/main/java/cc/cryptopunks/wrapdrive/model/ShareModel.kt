package cc.cryptopunks.wrapdrive.model

import android.net.Uri
import cc.cryptopunks.wrapdrive.app
import cc.cryptopunks.wrapdrive.proto.OfferId
import cc.cryptopunks.wrapdrive.proto.ResultCode
import cc.cryptopunks.wrapdrive.proto.network
import cc.cryptopunks.wrapdrive.proto.send
import cc.cryptopunks.wrapdrive.util.CoroutineViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class ShareModel : CoroutineViewModel() {
    val uri = MutableStateFlow(Uri.EMPTY to 0L)
    val isSharing = MutableStateFlow(false)
    val results = MutableSharedFlow<Result<Pair<OfferId, ResultCode>>>(
        extraBufferCapacity = 255
    )
}
fun ShareModel.setUri(uri: Uri) {
    this.uri.value = uri to System.currentTimeMillis()
}

fun ShareModel.share(peerId: String, uri: Uri) = app.launch {
    try {
        isSharing.value = true
        val result = withTimeout(10000) {
            network.send(
                peerId = peerId,
                uri = uri.toString()
            )
        }
        results.emit(Result.success(result))
    } catch (e: Throwable) {
        results.emit(Result.failure(e))
    } finally {
        isSharing.value = false
    }
}
