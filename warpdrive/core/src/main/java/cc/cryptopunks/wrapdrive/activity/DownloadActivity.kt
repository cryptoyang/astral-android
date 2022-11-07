package cc.cryptopunks.wrapdrive.activity

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.os.Bundle
import cc.cryptopunks.astral.intent.hasPermissions
import cc.cryptopunks.wrapdrive.app
import cc.cryptopunks.wrapdrive.proto.accept
import cc.cryptopunks.wrapdrive.proto.warpdrive
import cc.cryptopunks.wrapdrive.startWritePermissionActivity
import kotlinx.coroutines.launch

class DownloadActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasPermissions(WRITE_EXTERNAL_STORAGE)) startWritePermissionActivity()
        else try {
            val offerId = intent
                .data!!
                .lastPathSegment!!
            app.launch { warpdrive { accept(offerId) } }
        } catch (e: Throwable) {
            println("Cannot download files")
            e.printStackTrace()
        }
        finishAndRemoveTask()
    }
}
