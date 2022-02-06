package cc.cryptopunks.astral.wrapdrive.offer

import androidx.lifecycle.MutableLiveData
import cc.cryptopunks.astral.wrapdrive.api.EmptyOffer
import cc.cryptopunks.astral.wrapdrive.api.EmptyPeer
import cc.cryptopunks.astral.wrapdrive.api.FilterIn
import cc.cryptopunks.astral.wrapdrive.api.FilterOut
import cc.cryptopunks.astral.wrapdrive.api.Offer
import cc.cryptopunks.astral.wrapdrive.api.OfferId
import cc.cryptopunks.astral.wrapdrive.api.Peer
import cc.cryptopunks.astral.wrapdrive.api.PeerId
import cc.cryptopunks.astral.wrapdrive.api.PeerOffer
import cc.cryptopunks.astral.wrapdrive.util.CoroutineViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class OfferModel : CoroutineViewModel() {

    val currentId = MutableStateFlow(null as OfferId?)
    val current = MutableStateFlow(Current())
    val error = MutableStateFlow(null as Error?)
    val updates = mapOf(
        FilterIn to MutableLiveData(Update()),
        FilterOut to MutableLiveData(Update()),
    )
    var job = Job() as Job

    data class Error(
        val message: String,
        val throwable: Throwable,
    )

    data class Current(
        val peer: Peer = EmptyPeer,
        val offer: Offer = EmptyOffer,
    )

    data class Update(
        val offers: List<Offer> = emptyList(),
        val peers: Map<PeerId, Peer> = emptyMap(),
        val action: Action = Action.Init,
        val position: Int = 0,
        val value: Int = 0,
    ) {
        enum class Action { Init, Inserted, Changed }
    }

    companion object {
        val EmptyCurrent = Current()
    }
}

fun OfferModel.Update.peersOffers() = offers.map { offer ->
    val peer = peers[offer.peer] ?: EmptyPeer
    PeerOffer(peer, offer)
}

fun OfferModel.setCurrent(id: OfferId?) {
    currentId.value = id
    current.value = if (id == null)
        OfferModel.EmptyCurrent
    else {
        val offer = updates.values
            .mapNotNull { it.value }
            .flatMap(OfferModel.Update::offers)
            .find { offer -> offer.id == id }
            ?: EmptyOffer
        val peer = updates.values.first().value
            ?.peers
            ?.get(offer.peer)
            ?: EmptyPeer
        OfferModel.Current(peer, offer)
    }

}

val OfferModel.currentOffer
    get() = current.filter { current ->
        current != OfferModel.EmptyCurrent
    }.map {
        it.offer
    }
