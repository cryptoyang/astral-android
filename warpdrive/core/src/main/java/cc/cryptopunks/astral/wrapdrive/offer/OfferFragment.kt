package cc.cryptopunks.astral.wrapdrive.offer

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
import cc.cryptopunks.astral.wrapdrive.api.client.accept
import cc.cryptopunks.astral.wrapdrive.api.network
import cc.cryptopunks.astral.wrapdrive.databinding.OfferViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class OfferFragment : Fragment(), CoroutineScope by MainScope() {

    private val fileAdapter = OfferFilesAdapter()
    private val model: OfferModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = OfferViewBinding.inflate(inflater, container, false).apply {
        list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = fileAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        download.setOnClickListener {
            model.currentId.value?.let { offerId ->
                launch {
                    try {
                        network.accept(offerId)
                    } catch (e: Throwable) {
                        model.error.value = OfferModel.Error("Cannot share files", e)
                    }
                }
            }
        }
        val dateTime = SimpleDateFormat.getDateTimeInstance()
        model.current.filter {
            it != OfferModel.EmptyCurrent
        }.asLiveData().observe(viewLifecycleOwner) { data ->
            offerId.text = shortOfferId(data.offer.id)
            peer.text = data.peer.formattedName
            status.text = data.offer.formattedStatus
            createdAt.text = dateTime.format(data.offer.create)
            if (data.offer.create != data.offer.update)
                updatedAt.text = dateTime.format(data.offer.update)
            download.isVisible = data.offer.`in`
            fileAdapter.apply {
                val prevIndex = index
                index = data.offer.index
                progress = data.offer.progress
                items = data.offer.files
                when {
                    prevIndex != index -> notifyDataSetChanged()
                    data.offer.index > 0 -> notifyItemChanged(data.offer.index)
                    else -> notifyItemRangeChanged(0, data.offer.files.size)
                }
            }
        }
    }.root
}
