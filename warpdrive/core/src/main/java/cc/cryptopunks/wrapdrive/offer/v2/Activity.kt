package cc.cryptopunks.wrapdrive.offer.v2

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import cc.cryptopunks.wrapdrive.offer.OfferModel
import cc.cryptopunks.astral.ext.hasWriteStoragePermissions
import cc.cryptopunks.wrapdrive.offer.setCurrent
import cc.cryptopunks.wrapdrive.offer.setOfferId
import cc.cryptopunks.wrapdrive.theme.AppTheme

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
        model.hasWritePermission = hasWriteStoragePermissions()
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
