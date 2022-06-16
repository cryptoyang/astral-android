package cc.cryptopunks.wrapdrive.share

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cc.cryptopunks.wrapdrive.api.EmptyPeer
import cc.cryptopunks.wrapdrive.api.Peer
import cc.cryptopunks.wrapdrive.api.PeerId
import cc.cryptopunks.wrapdrive.databinding.PeerItemBinding
import kotlin.properties.Delegates

class PeerListAdapter(
    private val select: (Peer) -> Unit,
) : RecyclerView.Adapter<PeerListAdapter.ViewHolder>() {

    var items: List<Peer> = emptyList()

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        PeerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item = items[position]
    }

    inner class ViewHolder(
        private val binding: PeerItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                select(item)
            }
        }

        var item by Delegates.observable(EmptyPeer) { _, _, new ->
            binding.apply {
                alias.text = new.alias.takeIf(String::isNotEmpty) ?: "no-name"
                nodeId.text = when(new.network) {
                    "astral" -> new.id.shortId
                    else -> new.id
                }
            }
        }
    }
}

private val PeerId.shortId get() = takeLast(8).chunked(4).joinToString("-")
