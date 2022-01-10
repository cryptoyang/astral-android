package cc.cryptopunks.astral.wrapdrive

import android.app.IntentService
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import cc.cryptopunks.astral.wrapdrive.util.getContentSize
import cc.cryptopunks.astral.wrapdrive.util.items
import kotlinx.coroutines.runBlocking

class SendFileService : IntentService("SendFileService") {

    override fun onHandleIntent(intent: Intent) {
        val request = getSendFilesRequest(intent)
        val manager = notifyManager()
        runBlocking {
            network.sendFiles(request, manager)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        println("destroy service")
    }
}

const val EXTRA_NODE_ID = "nodeId"

fun Context.sendFileIntent(
    clip: ClipData,
    nodeId: String,
) = Intent(this, SendFileService::class.java).apply {
    clipData = clip
    putExtra(EXTRA_NODE_ID, nodeId)
}

fun Context.getSendFilesRequest(intent: Intent) = SendFilesRequest(
    nodeId = intent.getStringExtra(EXTRA_NODE_ID),
    files = intent.clipData!!.items().map { clip -> parseContentInfo(clip.uri) },
    resolve = { contentResolver.openInputStream(it)!! }
)


private fun Context.parseContentInfo(uri: Uri) = ContentInfo(
    uri = uri,
    name = uri.path!!.toString().split("/").last(),
    size = contentResolver.getContentSize(uri),
)
