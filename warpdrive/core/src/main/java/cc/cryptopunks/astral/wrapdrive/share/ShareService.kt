package cc.cryptopunks.astral.wrapdrive.share

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import cc.cryptopunks.astral.wrapdrive.api.OfferId
import cc.cryptopunks.astral.wrapdrive.api.ProvideAction
import cc.cryptopunks.astral.wrapdrive.api.ResultCode
import cc.cryptopunks.astral.wrapdrive.api.client.send
import cc.cryptopunks.astral.wrapdrive.api.network
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withTimeout

val isSharing = MutableStateFlow(false)
val sharingStatus =
    MutableSharedFlow<Result<Pair<OfferId, ResultCode>>>(extraBufferCapacity = Byte.MAX_VALUE.toInt())

fun Activity.shareService() = fun(
    id: String,
): ProvideAction = {
    val items: List<ClipData.Item> = intent.clipData?.items()
        ?: throw IllegalStateException("No clip data")
    val uri = items.first().uri
    grantUriPermission(
        "cc.cryptopunks.astral.node", uri,
        Intent.FLAG_GRANT_READ_URI_PERMISSION
    )
    suspend {
        try {
            isSharing.value = true
            val result = withTimeout(5000) {
                network.send(
                    peerId = id,
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
}

fun ClipData.items(): List<ClipData.Item> = List(itemCount, this::getItemAt)
