package cc.cryptopunks.wrapdrive.share

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.asLiveData
import cc.cryptopunks.wrapdrive.R
import cc.cryptopunks.wrapdrive.util.DisconnectionFragment
import cc.cryptopunks.wrapdrive.util.items
import cc.cryptopunks.wrapdrive.warpdrive
import com.google.android.material.snackbar.Snackbar

class ShareActivity : AppCompatActivity() {

    private val model by viewModels<ShareModel>()
    private val selectUri = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) finish()
        else model.uri.value = uri
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.share_activity)
        setSupportActionBar(findViewById(R.id.toolbar))
        setUri(intent)
        warpdrive.isConnected.asLiveData().observe(this) { isConnected ->
            val fragment = if (isConnected) {
                model.subscribePeers()
                PeerListFragment()
            } else {
                model.peersJob.cancel()
                DisconnectionFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
        }
        Snackbar.make(
            findViewById(R.id.coordinator),
            "Cannot obtain file uri, please select file once again",
            Snackbar.LENGTH_INDEFINITE
        ).let { info ->
            model.uri.asLiveData().observe(this) { uri ->
                when (uri) {
                    Uri.EMPTY -> info.show()
                    else -> {
                        val astralPackage = getString(R.string.astral_package)
                        grantUriPermission(
                            astralPackage, uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                        info.dismiss()
                    }
                }
            }
        }
        findViewById<ProgressBar>(R.id.toolbarProgress).let { progress ->
            isSharing.asLiveData().observe(this) { value ->
                progress.isVisible = value
            }
        }
        Snackbar.make(
            findViewById(R.id.coordinator),
            "",
            Snackbar.LENGTH_LONG
        ).also { info ->
            sharingStatus.asLiveData().observe(this) { result ->
                result.onSuccess { (_, code) ->
                    info.setText(when (code.toInt()) {
                        1 -> "Share accepted, the files are sending in background"
                        else -> "Share delivered and waiting for approval"
                    })
                }
                result.onFailure {
                    info.setText("Cannot share files: " + it.localizedMessage)
                }
                info.show()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent
        setUri(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.share, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var result = true
        when (item.itemId) {
            R.id.share -> selectUri.launch(MIME)
            R.id.bt -> startService(Intent("cc.cryptopunks.astral.service.BT_DISCOVERY").apply {
                `package` = getString(R.string.astral_package)
            })
            else -> result = super.onOptionsItemSelected(item)
        }
        return result
    }

    private fun setUri(intent: Intent) {
        val uri = intent.clipData?.items()?.firstOrNull()?.uri
        if (uri != null) {
            model.uri.value = uri
        } else {
            selectUri.launch(MIME)
        }
    }

    companion object {
        fun intent(context: Context) = Intent(context, ShareActivity::class.java)
        private const val MIME = "*/*"
    }
}
