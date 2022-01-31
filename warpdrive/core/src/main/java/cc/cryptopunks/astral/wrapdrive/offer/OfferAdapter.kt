package cc.cryptopunks.astral.wrapdrive.offer

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cc.cryptopunks.astral.wrapdrive.api.EmptyOffer
import cc.cryptopunks.astral.wrapdrive.api.Info
import cc.cryptopunks.astral.wrapdrive.api.Offer
import cc.cryptopunks.astral.wrapdrive.api.OfferId
import cc.cryptopunks.astral.wrapdrive.databinding.OfferItemBinding
import cc.cryptopunks.astral.wrapdrive.util.formatSize
import kotlin.properties.Delegates

@SuppressLint("NotifyDataSetChanged")
class OfferAdapter : RecyclerView.Adapter<OfferAdapter.ViewHolder>() {

    inner class ViewHolder(
        private val binding: OfferItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        var item by Delegates.observable("" to EmptyOffer) { _, _, (id, new) ->
            binding.apply {
                peerId.text = new.peer
                offerId.text = id
                name.text = new.files.first().uri
                fileCount.text = new.files.size.toString()
                totalSize.text = new.files.sumOf(Info::size).formatSize()
                progress.text = new.files.sumOf(Info::progress).formatSize()
            }
        }

        init {
            binding.root.setOnClickListener {
                val intent = OfferActivity.intent(item.first)
                binding.root.context.startActivity(intent)
            }
        }
    }

    var items: List<Pair<OfferId, Offer>> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        OfferItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item = items[position]
    }
}
