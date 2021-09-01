package cc.cryptopunks.astral.wrapdrive.peer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cc.cryptopunks.astral.wrapdrive.R
import cc.cryptopunks.astral.wrapdrive.client.listPeers
import cc.cryptopunks.astral.wrapdrive.network
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectPeerFragment :
    Fragment(),
    CoroutineScope by MainScope() {

    var onPeerSelected: OnPeerSelected = {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = inflater
        .inflate(R.layout.fragment_item_list, container, false)
        .let { view -> view as RecyclerView }
        .apply {
            layoutManager = LinearLayoutManager(context)
            adapter = PeerRecyclerViewAdapter().apply {
                onItemClick = { onPeerSelected(it) }
                launch {
                    try {
                        val peers = network.listPeers().map { PeerItem(it) }
                        withContext(Dispatchers.Main) { values = peers }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancelChildren()
        onPeerSelected = {}
    }
}
