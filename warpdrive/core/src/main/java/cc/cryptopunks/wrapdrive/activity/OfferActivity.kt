package cc.cryptopunks.wrapdrive.activity

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import cc.cryptopunks.astral.ext.hasPermissions
import cc.cryptopunks.wrapdrive.compose.AppTheme
import cc.cryptopunks.wrapdrive.compose.MainView
import cc.cryptopunks.wrapdrive.model.OfferModel
import cc.cryptopunks.wrapdrive.model.setCurrent
import cc.cryptopunks.wrapdrive.model.setOfferId

class OfferActivity : ComponentActivity() {

    val model by viewModels<OfferModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model.setOfferId(intent)
        setContent {
            AppTheme {
                MainView(model)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        model.hasWritePermission = hasPermissions(WRITE_EXTERNAL_STORAGE)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent
        model.setOfferId(intent)
    }

    override fun onBackPressed() {
        when (model.currentId.value) {
            null -> super.onBackPressed()
            else -> {
                intent = null
                model.setCurrent(null)
            }
        }
    }
}
