package cc.cryptopunks.wrapdrive.activity

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cc.cryptopunks.astral.ext.hasPermissions
import cc.cryptopunks.wrapdrive.app
import cc.cryptopunks.wrapdrive.proto.accept
import cc.cryptopunks.wrapdrive.proto.network
import cc.cryptopunks.wrapdrive.startWritePermissionActivity
import kotlinx.coroutines.launch

class DownloadActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasPermissions(WRITE_EXTERNAL_STORAGE)) startWritePermissionActivity()
        else try {
            val offerId = intent
                .data!!
                .lastPathSegment!!
            app.launch {
                network.accept(offerId)
            }
        } catch (e: Throwable) {
            println("Cannot download files")
            e.printStackTrace()
        }
        finishAndRemoveTask()
    }
}