package cc.cryptopunks.astral.wrapdrive.offer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import cc.cryptopunks.astral.wrapdrive.api.EmptyOffer
import cc.cryptopunks.astral.wrapdrive.api.client.accept
import cc.cryptopunks.astral.wrapdrive.api.network
import cc.cryptopunks.astral.wrapdrive.application
import cc.cryptopunks.astral.wrapdrive.databinding.OfferViewBinding
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class OfferFragment : Fragment() {

    private var binding: OfferViewBinding? = null
    private val fileAdapter = FileAdapter()

    var offer by Delegates.observable(EmptyOffer) { _, _, new ->
        fileAdapter.items = new.files
        binding?.apply {
            peerId.text = new.peer
            offerId.text = new.id
            status.text = new.status
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = OfferViewBinding.inflate(inflater, container, false).apply {
        list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = fileAdapter

        }
        download.setOnClickListener {
            application.launch {
                network.accept(offer.id)
            }
        }
    }.also {
        binding = it
        offer = offer
    }.root

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}
