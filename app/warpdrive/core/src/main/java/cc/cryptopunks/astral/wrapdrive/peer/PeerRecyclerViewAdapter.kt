package cc.cryptopunks.astral.wrapdrive.peer

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cc.cryptopunks.astral.wrapdrive.databinding.PeerItemBinding
import kotlin.properties.Delegates

@SuppressLint("NotifyDataSetChanged")
class PeerRecyclerViewAdapter : RecyclerView.Adapter<PeerRecyclerViewAdapter.ViewHolder>() {

    var values: List<PeerItem> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }

    var onItemClick: OnPeerSelected = {}

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ) = ViewHolder(PeerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item = values[position]
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: PeerItemBinding) : RecyclerView.ViewHolder(binding.root) {

        private val idView: TextView = binding.nodeId

        var item by Delegates.observable(PeerItem.Empty) { _, _, new ->
            idView.text = new.nodeId
        }

        init {
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }
}
