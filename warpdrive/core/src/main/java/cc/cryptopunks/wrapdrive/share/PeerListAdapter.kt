package cc.cryptopunks.wrapdrive.share

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cc.cryptopunks.wrapdrive.proto.EmptyPeer
import cc.cryptopunks.wrapdrive.proto.Peer
import cc.cryptopunks.wrapdrive.databinding.PeerItemBinding
import kotlin.properties.Delegates

class PeerListAdapter(
    private val select: (String) -> Unit,
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
                select(item.id)
            }
        }

        var item by Delegates.observable(EmptyPeer) { _, _, new ->
            binding.apply {
                alias.text = new.alias.takeIf(String::isNotEmpty) ?: "no-name"
                nodeId.text = new.id.takeLast(8).chunked(4).joinToString("-")
            }
        }
    }
}
