package cc.cryptopunks.wrapdrive.offer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cc.cryptopunks.astral.ext.hasWriteStoragePermissions
import cc.cryptopunks.astral.ext.startWritePermissionActivity
import cc.cryptopunks.wrapdrive.api.client.accept
import cc.cryptopunks.wrapdrive.api.network
import cc.cryptopunks.wrapdrive.app
import kotlinx.coroutines.launch

class DownloadActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasWriteStoragePermissions()) startWritePermissionActivity()
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
