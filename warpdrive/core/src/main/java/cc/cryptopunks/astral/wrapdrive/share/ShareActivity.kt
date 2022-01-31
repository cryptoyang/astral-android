package cc.cryptopunks.astral.wrapdrive.share

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cc.cryptopunks.astral.wrapdrive.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ShareActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val peerAdapter = PeerAdapter(shareService())

    private val refreshLayout get() = findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)

    private val toolbarProgress by lazy { findViewById<ProgressBar>(R.id.toolbarProgress) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.share_activity)
        setSupportActionBar(findViewById(R.id.toolbar))
        findViewById<RecyclerView>(R.id.list).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = peerAdapter
        }
        refreshLayout.apply {
            setOnRefreshListener {
                launch {
                    loadPeers()
                }
            }
        }
        launch {
            peersFetching.collect { value ->
                refreshLayout.isRefreshing = value
            }
        }
        launch {
            peersResult.collect { result ->
                result.onSuccess {
                    peerAdapter.items = it
                }
                result.onFailure {
                    Snackbar.make(
                        findViewById(R.id.coordinator),
                        "Cannot load peers: " + it.localizedMessage,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
        launch {
            isSharing.collect { value ->
                toolbarProgress.isVisible = value
            }
        }
        launch {
            sharingStatus.collect { result ->
                result.onSuccess { (_, code) ->
                    Snackbar.make(
                        findViewById(R.id.coordinator),
                        when (code.toInt()) {
                            1 -> "Share accepted, the files are sending in background"
                            else -> "Share delivered and waiting for approval"
                        },
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                result.onFailure {
                    Snackbar.make(
                        findViewById(R.id.coordinator),
                        "Cannot share files: " + it.localizedMessage,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        launch {
            loadPeers()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}
