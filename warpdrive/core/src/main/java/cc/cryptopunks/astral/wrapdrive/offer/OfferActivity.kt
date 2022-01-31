package cc.cryptopunks.astral.wrapdrive.offer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cc.cryptopunks.astral.wrapdrive.R
import cc.cryptopunks.astral.wrapdrive.api.client.received
import cc.cryptopunks.astral.wrapdrive.api.client.sent
import cc.cryptopunks.astral.wrapdrive.api.network
import cc.cryptopunks.astral.wrapdrive.util.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class OfferActivity :
    AppCompatActivity(),
    CoroutineScope by MainScope() {

    private val loadRequest = Channel<Unit>(Channel.CONFLATED)
    private val offersFragment = OffersFragment()
    private val offerFragment = OfferFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.offer_activity)
        launch { loadRequest.consumeEach { loadOffers() } }
        loadRequest.trySend(Unit)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, offersFragment)
            .commit()
        handle(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        loadRequest.trySend(Unit)
        this.intent = intent
        handle(intent)
    }

    override fun onResume() {
        super.onResume()
        loadRequest.trySend(Unit)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (supportFragmentManager.backStackEntryCount == 0) {
            intent.data = null
            intent = intent
        }
    }

    private fun handle(intent: Intent) {
        intent.data ?: return
        if (supportFragmentManager.fragments.last() != offerFragment)
            supportFragmentManager.beginTransaction()
                .add(R.id.container, offerFragment)
                .addToBackStack(null)
                .commit()
    }

    private suspend fun loadOffers() {
        try {
            val items = withTimeout(3000) {
                network.run { sent() + received() }
            }
            offersFragment.offerAdapter.items = items.toList()
            intent.data?.run {
                val id = host!!
                val item = items[id]!!
                offerFragment.offer = item
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            toast(e)
        }
    }

    companion object {
        fun intent(offerId: String): Intent {
            val uri = Uri.parse("warpdrive://$offerId")
            return Intent(Intent.ACTION_VIEW, uri)
        }
    }
}
