package cc.cryptopunks.astral.wrapdrive

import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cc.cryptopunks.astral.wrapdrive.peer.PeerItem
import cc.cryptopunks.astral.wrapdrive.peer.SelectPeerFragment

class ShareFileActivity : AppCompatActivity() {

    private var clips: ClipData? = null

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
        clips = intent.clipData
    }

    private fun sendFiles(selected: PeerItem) {
        val clips = clips ?: return
        startService(sendFileIntent(clips, selected.nodeId))
    }
}
