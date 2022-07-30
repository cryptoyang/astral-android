package cc.cryptopunks.wrapdrive.share

import android.net.Uri
import cc.cryptopunks.wrapdrive.api.OfferId
import cc.cryptopunks.wrapdrive.api.Peer
import cc.cryptopunks.wrapdrive.api.ResultCode
import cc.cryptopunks.wrapdrive.util.CoroutineViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

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
