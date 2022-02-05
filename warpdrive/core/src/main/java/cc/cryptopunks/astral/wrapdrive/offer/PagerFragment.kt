package cc.cryptopunks.astral.wrapdrive.offer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.viewpager2.adapter.FragmentStateAdapter
import cc.cryptopunks.astral.wrapdrive.api.FilterIn
import cc.cryptopunks.astral.wrapdrive.api.FilterOut
import cc.cryptopunks.astral.wrapdrive.databinding.OfferPagerBinding
import com.google.android.material.tabs.TabLayoutMediator

class PagerFragment : Fragment() {

    private val model by activityViewModels<OfferModel>()
    private val adapter by lazy { Adapter() }
    private val config = TabLayoutMediator.TabConfigurationStrategy { tab, position ->
        tab.text = when (position) {
            0 -> "Received"
            1 -> "Sent"
            else -> throw IllegalArgumentException()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = OfferPagerBinding.inflate(inflater, container, false).apply {
        pager.adapter = adapter
        TabLayoutMediator(tabs, pager, config).attach()
        model.currentOffer.asLiveData().observe(viewLifecycleOwner) { offer ->
            pager.currentItem = if (offer.`in`) 0 else 1
        }
    }.root

    private inner class Adapter : FragmentStateAdapter(this) {

        private val received = OfferListFragment.create(FilterIn)
        private val sent = OfferListFragment.create(FilterOut)

        override fun getItemCount() = 2
        override fun createFragment(position: Int) = when (position) {
            0 -> received
            1 -> sent
            else -> throw IllegalArgumentException()
        }
    }
}
