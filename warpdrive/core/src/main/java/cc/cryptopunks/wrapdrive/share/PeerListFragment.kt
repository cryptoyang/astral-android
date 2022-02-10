package cc.cryptopunks.wrapdrive.share

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import cc.cryptopunks.wrapdrive.databinding.PeerListBinding

class PeerListFragment : Fragment() {

    private val model by activityViewModels<ShareModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = PeerListBinding.inflate(inflater, container, false).root

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) = PeerListBinding.bind(view).run {
        val peerAdapter = PeerListAdapter { selectedPeer ->
            model.share.tryEmit(selectedPeer)
        }
        list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = peerAdapter
        }
        swipeRefresh.setOnRefreshListener {
            model.refresh()
        }
        model.isRefreshing.asLiveData().observe(viewLifecycleOwner) { value ->
            swipeRefresh.isRefreshing = value
        }
        model.peers.asLiveData().observe(viewLifecycleOwner) { peers ->
            peerAdapter.items = peers
            peerAdapter.notifyItemRangeChanged(0, peers.size)
        }
    }
}
