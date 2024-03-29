package cc.cryptopunks.wrapdrive.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import cc.cryptopunks.wrapdrive.compose.AppTheme
import cc.cryptopunks.wrapdrive.compose.ShareView
import cc.cryptopunks.wrapdrive.model.ShareModel
import cc.cryptopunks.wrapdrive.model.setUri
import cc.cryptopunks.wrapdrive.startFileChooser
import cc.cryptopunks.wrapdrive.util.items

class ShareActivity : ComponentActivity() {

    val model by viewModels<ShareModel>()

    private val selectUri = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) finish()
        else model.setUri(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUri(intent)
        setContent {
            AppTheme {
                ShareView(
                    shareModel = model,
                    selectUri = { selectUri.startFileChooser() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent
        setUri(intent)
    }

    private fun setUri(intent: Intent) {
        val uri = intent.clipData?.items()?.firstOrNull()?.uri
        if (uri != null) {
            model.setUri(uri)
        } else {
            selectUri.startFileChooser()
        }
    }
}
