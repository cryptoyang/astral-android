package cc.cryptopunks.wrapdrive.offer

import androidx.lifecycle.asLiveData
import cc.cryptopunks.wrapdrive.api.EmptyOffer
import cc.cryptopunks.wrapdrive.api.EmptyPeer
import cc.cryptopunks.wrapdrive.api.FilterIn
import cc.cryptopunks.wrapdrive.api.FilterOut
import cc.cryptopunks.wrapdrive.api.Offer
import cc.cryptopunks.wrapdrive.api.OfferId
import cc.cryptopunks.wrapdrive.api.Peer
import cc.cryptopunks.wrapdrive.api.PeerId
import cc.cryptopunks.wrapdrive.api.PeerOffer
import cc.cryptopunks.wrapdrive.util.CoroutineViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map

class OfferModel : CoroutineViewModel() {

    var hasWritePermission: Boolean = false
    val currentId = MutableStateFlow(null as OfferId?)
    val current = MutableStateFlow(Current())
    val error = MutableStateFlow(null as Error?)
    val updates = mapOf(
        FilterIn to MutableStateFlow(EmptyUpdate),
        FilterOut to MutableStateFlow(EmptyUpdate),
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
        val EmptyUpdate = Update()
    }
}

fun OfferModel.Update.peersOffers() = offers.map { offer ->
    val peer = peers[offer.peer] ?: EmptyPeer
    PeerOffer(peer, offer)
}

fun OfferModel.updates(filter: String) =
    updates.getValue(filter).filterNot { it == OfferModel.EmptyUpdate }.asLiveData()

fun OfferModel.setCurrent(id: OfferId?) {
    currentId.value = id
    current.value = if (id == null)
        OfferModel.EmptyCurrent
    else {
        val offer = updates
            .flatMap { (_, state) -> state.value.offers }
            .find { offer -> offer.id == id } ?: EmptyOffer
        val peer = updates.values
            .first().value.peers[offer.peer] ?: EmptyPeer
        OfferModel.Current(peer, offer)
    }

}

val OfferModel.currentOffer
    get() = current.filter { current ->
        current != OfferModel.EmptyCurrent
    }.map {
        it.offer
    }
