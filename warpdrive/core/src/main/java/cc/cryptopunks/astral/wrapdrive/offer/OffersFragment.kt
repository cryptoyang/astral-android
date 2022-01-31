package cc.cryptopunks.astral.wrapdrive.offer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import cc.cryptopunks.astral.wrapdrive.databinding.OfferListBinding

class OffersFragment : Fragment() {

    val offerAdapter = OfferAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = OfferListBinding.inflate(inflater, container, false).apply {
        list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = offerAdapter
        }
    }.root
}

