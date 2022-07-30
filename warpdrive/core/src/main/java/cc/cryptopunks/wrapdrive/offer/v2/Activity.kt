package cc.cryptopunks.wrapdrive.offer.v2

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import cc.cryptopunks.wrapdrive.offer.OfferModel
import cc.cryptopunks.wrapdrive.offer.setOfferId
import cc.cryptopunks.wrapdrive.theme.AppTheme

class OfferActivity : ComponentActivity() {

    val model by viewModels<OfferModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                MainView(model)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent
        model.setOfferId(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        intent.data = null
//        model.launch {
//            delay(1500)
//            model.setOfferId(null)
//        }
    }
}
