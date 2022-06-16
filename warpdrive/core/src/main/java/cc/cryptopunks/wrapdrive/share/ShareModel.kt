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
    val error = MutableStateFlow(null as Throwable?)
    val uri = MutableStateFlow(Uri.EMPTY)
    val peers = MutableStateFlow(emptyList<Peer>())
    val refresh = MutableSharedFlow<Unit>()
    val isRefreshing = MutableStateFlow(false)
    val share = MutableSharedFlow<Peer>(extraBufferCapacity = 1)
    var peersJob = Job() as Job

    init {
        subscribeShare()
    }
}

val isSharing = MutableStateFlow(false)
val sharingStatus = MutableSharedFlow<Result<Pair<OfferId, ResultCode>>>(
    extraBufferCapacity = Byte.MAX_VALUE.toInt()
)
