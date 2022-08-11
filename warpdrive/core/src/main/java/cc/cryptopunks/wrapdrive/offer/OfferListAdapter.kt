package cc.cryptopunks.wrapdrive.offer

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cc.cryptopunks.wrapdrive.api.EmptyPeerOffer
import cc.cryptopunks.wrapdrive.api.PeerOffer
import cc.cryptopunks.wrapdrive.databinding.OfferItemBinding
import cc.cryptopunks.wrapdrive.offerIntent
import kotlin.properties.Delegates

@SuppressLint("NotifyDataSetChanged")
class OfferListAdapter : RecyclerView.Adapter<OfferListAdapter.ViewHolder>() {

    var items = emptyList<PeerOffer>()

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        OfferItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item = items[position]
    }

    inner class ViewHolder(
        private val binding: OfferItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.apply {
                setOnClickListener {
                    context.startActivity(offerIntent(item.offer.id))
                }
            }
        }

        var item by Delegates.observable(EmptyPeerOffer) { _, _, new ->
            binding.apply {
                peer.text = new.formattedName
                amount.text = new.offer.formattedAmount
                info.text = new.offer.formattedInfo
                size.text = new.offer.formattedSize
                offerId.text = shortOfferId(new.offer.id)
                datetime.text = new.offer.formattedDateTime
            }
        }
    }
}
