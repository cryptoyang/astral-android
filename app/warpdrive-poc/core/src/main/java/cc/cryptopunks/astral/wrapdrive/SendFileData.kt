package cc.cryptopunks.astral.wrapdrive

import android.net.Uri
import java.io.InputStream

data class ContentInfo(
    val uri: Uri,
    val name: String,
    val size: Long,
) {
    val id = lastId++
    private companion object {
        var lastId = 100
    }
}

class SendFilesRequest(
    val nodeId: String,
    val files: List<ContentInfo>,
    val resolve: (Uri) -> InputStream,
)
