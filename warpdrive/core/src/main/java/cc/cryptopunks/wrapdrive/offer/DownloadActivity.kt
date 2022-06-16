package cc.cryptopunks.wrapdrive.offer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cc.cryptopunks.wrapdrive.api.client.accept
import cc.cryptopunks.wrapdrive.api.network
import cc.cryptopunks.wrapdrive.util.hasWriteStoragePermissions
import cc.cryptopunks.wrapdrive.util.requestWriteStoragePermission
import cc.cryptopunks.wrapdrive.warpdrive
import kotlinx.coroutines.launch

class DownloadActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasWriteStoragePermissions) requestWriteStoragePermission()
        else try {
            val offerId = intent
                .data!!
                .lastPathSegment!!
            warpdrive.launch {
                network.accept(offerId)
            }
        } catch (e: Throwable) {
            println("Cannot download files")
            e.printStackTrace()
        }
        finishAndRemoveTask()
    }
}
