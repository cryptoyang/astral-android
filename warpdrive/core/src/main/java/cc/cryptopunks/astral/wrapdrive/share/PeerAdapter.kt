package cc.cryptopunks.astral.wrapdrive.share

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cc.cryptopunks.astral.wrapdrive.api.ProvideAction
import cc.cryptopunks.astral.wrapdrive.api.EmptyPeer
import cc.cryptopunks.astral.wrapdrive.api.Peer
import cc.cryptopunks.astral.wrapdrive.api.dispatch
import cc.cryptopunks.astral.wrapdrive.application
import cc.cryptopunks.astral.wrapdrive.databinding.PeerItemBinding
import kotlin.properties.Delegates

@SuppressLint("NotifyDataSetChanged")
class PeerAdapter(
    val select: (String) -> ProvideAction,
) : RecyclerView.Adapter<PeerAdapter.ViewHolder>() {

    inner class ViewHolder(
        private val binding: PeerItemBinding,
    ) :
        RecyclerView.ViewHolder(binding.root) {

        var item by Delegates.observable(EmptyPeer) { _, _, new ->
            binding.apply {
                alias.text = new.alias.takeIf(String::isNotEmpty) ?: "no-name"
                nodeId.text = new.id.takeLast(8).chunked(4).joinToString("-")
            }
        }

        init {
            binding.root.setOnClickListener {
                val action = select(item.id)
                application dispatch action()
            }
        }
    }

    var items: Array<Peer> by Delegates.observable(emptyArray()) { _, _, _ ->
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        PeerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item = items[position]
    }
}
