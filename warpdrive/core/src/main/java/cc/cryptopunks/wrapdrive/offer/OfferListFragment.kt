package cc.cryptopunks.wrapdrive.offer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cc.cryptopunks.wrapdrive.api.FilterIn
import cc.cryptopunks.wrapdrive.api.FilterOut
import cc.cryptopunks.wrapdrive.api.Offer
import cc.cryptopunks.wrapdrive.api.OffersFilter
import cc.cryptopunks.wrapdrive.databinding.OfferListBinding
import cc.cryptopunks.wrapdrive.share.ShareActivity

class OfferListFragment : Fragment() {

    private val model by activityViewModels<OfferModel>()
    private val offersAdapter = OfferListAdapter()
    private val linearLayoutManager by lazy { LinearLayoutManager(context) }
    private val filter get() = requireArguments().getString(FILTER)!!

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = OfferListBinding.inflate(inflater, container, false).apply {
        noOffers.chooseAppButton.setOnClickListener {
            startActivity(ShareActivity.intent(requireContext()))
        }
        when (filter) {
            FilterIn -> noOffers.noReceived.isVisible = true
            FilterOut -> noOffers.noSent.isVisible = true
        }
        offers.apply {
            layoutManager = linearLayoutManager
            adapter = offersAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        model.updates(filter).observe(viewLifecycleOwner) { change ->
            val hasItems = change.offers.isNotEmpty()
            offers.isVisible = hasItems
            noOffers.root.isVisible = hasItems.not()
            offersAdapter update change
        }
        model.currentOffer.asLiveData().observe(viewLifecycleOwner) { offer ->
            offers tryScrollToViewedOffer offer
        }
    }.root

    @SuppressLint("NotifyDataSetChanged")
    private infix fun OfferListAdapter.update(change: OfferModel.Update) {
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

    private infix fun RecyclerView.tryScrollToViewedOffer(offer: Offer) {
        filter == FilterIn == offer.`in` || return
        val index = offersAdapter.items.indexOfFirst { it.offer.id == offer.id }
        index > -1 || return
        index !in linearLayoutManager.completelyVisibleItemPositions() || return
        scrollToPosition(index)
    }

    companion object {
        private const val FILTER = "filter"
        fun create(filter: OffersFilter) = OfferListFragment().apply {
            arguments = Bundle().apply {
                putString(FILTER, filter)
            }
        }
    }
}

private fun LinearLayoutManager.completelyVisibleItemPositions(): IntRange =
    findFirstCompletelyVisibleItemPosition()..findLastCompletelyVisibleItemPosition()
