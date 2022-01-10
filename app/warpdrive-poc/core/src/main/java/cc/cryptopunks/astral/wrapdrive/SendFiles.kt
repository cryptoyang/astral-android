package cc.cryptopunks.astral.wrapdrive

import android.net.Uri
import cc.cryptopunks.astral.api.Network
import cc.cryptopunks.astral.wrapdrive.client.sendFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.io.InputStream


suspend fun Network.sendFiles(
    request: SendFilesRequest,
    notify: Notify.Manager,
) {
    notify.init(
        group = Notify.Group(request.nodeId),
        items = request.files.map {
            Notify.Item(
                id = it.id,
                name = it.name,
                size = it.size,
            )
        }
    )
    sendFiles(
        nodeId = request.nodeId,
        files = request.files,
        resolve = request.resolve
    ).buffer().collect { (info, progress) ->
        delay(50)
        when (progress) {
            -1L -> notify.start(
                id = info.id,
                indeterminate = info.size < 0
            )
            -2L -> notify.finish(info.id, rejected = false)
            -3L -> notify.finish(info.id, rejected = true)
            else -> notify.setProgress(info.id, progress)
        }
    }
    notify.finishGroup()
}

fun Network.sendFiles(
    nodeId: String,
    files: List<ContentInfo>,
    resolve: (Uri) -> InputStream,
) = channelFlow<Pair<ContentInfo, Long>> {
    val sample: Long = 300
    withContext(Dispatchers.IO) {
        files.forEach { info ->
            send(info to -1)
            var last = System.currentTimeMillis()
            var current: Long
            try {
                val final = sendFile(nodeId = nodeId,
                    fileName = info.name,
                    inputStream = resolve(info.uri)
                ).onEach { progress ->
                    current = System.currentTimeMillis()
                    if (current - last > sample) {
                        last = current
                        trySend(info to progress)
                    }
                }.last()
                send(info to final)
                send(info to -2)
            } catch (e: Throwable) {
                send(info to -3)
            }
        }
    }
}
