package cc.cryptopunks.astral.wrapdrive.offer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import cc.cryptopunks.astral.wrapdrive.api.FilterIn
import cc.cryptopunks.astral.wrapdrive.api.OffersFilter
import cc.cryptopunks.astral.wrapdrive.databinding.OfferListBinding

class OfferListFragment : Fragment() {

    private val model by activityViewModels<OfferModel>()
    private val offersAdapter = OfferListAdapter()
    private val filter get() = requireArguments().getString(FILTER)!!

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = OfferListBinding.inflate(inflater, container, false).apply {
        val manager = LinearLayoutManager(context)
        list.apply {
            layoutManager = manager
            adapter = offersAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        model.updates.getValue(filter).observe(viewLifecycleOwner) { change ->
            offersAdapter.apply {
                items = change.peersOffers()
                when (change.action) {
                    OfferModel.Update.Action.Init,
                    -> notifyDataSetChanged()
                    OfferModel.Update.Action.Changed,
                    -> notifyItemRangeChanged(change.position, change.value)
                    OfferModel.Update.Action.Inserted,
                    -> notifyItemInserted(change.position)
                }
            }
        }
        model.currentOffer.asLiveData().observe(viewLifecycleOwner) { offer ->
            if (filter == FilterIn == offer.`in`) {
                offersAdapter.items.indexOfFirst {
                    it.offer.id == offer.id
                }.takeIf { index ->
                    index > -1 && manager.run {
                        index !in findFirstCompletelyVisibleItemPosition()..findLastCompletelyVisibleItemPosition()
                    }
                }?.let { index ->
                    list.scrollToPosition(index)
                }
            }
        }
    }.root

    companion object {
        private const val FILTER = "filter"
        fun create(filter: OffersFilter) = OfferListFragment().apply {
            arguments = Bundle().apply {
                putString(FILTER, filter)
            }
        }
    }
}
