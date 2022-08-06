package cc.cryptopunks.wrapdrive.offer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.asLiveData
import cc.cryptopunks.astral.err.AstralLocalConnectionException
import cc.cryptopunks.wrapdrive.R
import cc.cryptopunks.wrapdrive.share.ShareActivity
import cc.cryptopunks.wrapdrive.util.DisconnectionFragment
import cc.cryptopunks.wrapdrive.app
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach

class OfferActivity : AppCompatActivity() {

    private val model by viewModels<OfferModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.offer_activity)
        setOfferId(intent)
        app.isConnected.onEach { isConnected ->
            if (isConnected) model.subscribeChanges()
            else model.job.cancel()
        }.combine(model.currentId) { isConnected, currentId ->
            isConnected to currentId
        }.asLiveData().observe(this) { (isConnected, currentId) ->
            val fragments = supportFragmentManager.fragments
            when {
                !isConnected -> supportFragmentManager.apply {
                    popBackStack()
                    commit {
                        replace(R.id.container, DisconnectionFragment())
                    }
                }
                else -> {
                    if (fragments.firstOrNull() !is PagerFragment) supportFragmentManager.commit {
                        replace(R.id.container, PagerFragment())
                    }
                    if (currentId != null && fragments.lastOrNull() !is OfferFragment) supportFragmentManager.commit {
                        add(R.id.container, OfferFragment())
                        addToBackStack(null)
                    }
                }
            }
        }
        val errorSnack = Snackbar.make(
            findViewById(R.id.coordinator),
            "", Snackbar.LENGTH_LONG
        ).setAction("open") {
            val intent = packageManager.getLaunchIntentForPackage("cc.cryptopunks.astral.node")
            startActivity(intent)
        }
        model.error
            .filterNotNull()
            .filterNot { it.throwable is AstralLocalConnectionException }
            .asLiveData().observe(this) { error ->
                model.error.value = null
                errorSnack.setText("${error.message}: ${error.throwable.localizedMessage}").show()
            }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent
        setOfferId(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (model.currentId.value != null) {
            model.setCurrent(null)
            intent.data = null
            intent = intent
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.share, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var result = true
        when (item.itemId) {
            R.id.share -> startActivity(ShareActivity.intent(this))
            else -> result = super.onOptionsItemSelected(item)
        }
        return result
    }

    private fun setOfferId(intent: Intent) {
        val id = intent.data?.lastPathSegment ?: return
        model.setCurrent(id)
    }

    companion object {
        fun intent(offerId: String): Intent {
            val uri = Uri.parse("warpdrive://offer/$offerId")
            return Intent(Intent.ACTION_VIEW, uri)
        }
    }
}
