package cc.cryptopunks.astral.wrapdrive

import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import cc.cryptopunks.astral.wrapdrive.client.sendFile
import cc.cryptopunks.astral.wrapdrive.peer.PeerItem
import cc.cryptopunks.astral.wrapdrive.peer.SelectPeerFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ShareFileActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private var clips: List<ClipData.Item> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.send_file_activity)
        supportFragmentManager.fragments
            .filterIsInstance<SelectPeerFragment>()
            .first().onPeerSelected = this::sendFiles
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        clips = intent.clipData?.items() ?: emptyList()
        clips.forEach { Log.d(this.javaClass.simpleName, it.toString()) }
    }

    private fun sendFiles(selected: PeerItem) {
        launch(Dispatchers.IO) {
            clips.mapNotNull { item -> item.uri }.forEach { uri ->
                val filePath = uri.path!!.toString().split("/").last()
                val inputStream = contentResolver.openInputStream(uri)!!
                network.sendFile(selected.nodeId, filePath, inputStream)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}

private fun ClipData.items() = List(itemCount, this::getItemAt)
