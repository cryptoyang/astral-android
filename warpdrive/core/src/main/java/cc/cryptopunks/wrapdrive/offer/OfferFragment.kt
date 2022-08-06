package cc.cryptopunks.wrapdrive.offer

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
import cc.cryptopunks.astral.ext.hasWriteStoragePermissions
import cc.cryptopunks.astral.ext.startWritePermissionActivity
import cc.cryptopunks.wrapdrive.api.EmptyPeerOffer
import cc.cryptopunks.wrapdrive.api.PeerOffer
import cc.cryptopunks.wrapdrive.databinding.OfferViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.filter
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
            if (model.hasWritePermission) model.download()
            else requireContext().startWritePermissionActivity()
        }
        val dateTime = SimpleDateFormat.getDateTimeInstance()
        model.current.filter {
            it != EmptyPeerOffer
        }.asLiveData().observe(viewLifecycleOwner) { data ->
            offerId.text = shortOfferId(data.offer.id)
            peer.text = PeerOffer(data.peer, data.offer).formattedName
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
                    offerId != data.offer.id -> notifyDataSetChanged()
                    prevIndex != index -> notifyDataSetChanged()
                    data.offer.index > 0 -> notifyItemChanged(data.offer.index)
                    else -> notifyItemRangeChanged(0, data.offer.files.size)
                }
            }
        }
    }.root

    override fun onResume() {
        super.onResume()
        model.hasWritePermission = requireContext().hasWriteStoragePermissions()
    }
}
