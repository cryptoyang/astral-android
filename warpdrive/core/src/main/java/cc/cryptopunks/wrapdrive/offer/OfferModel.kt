package cc.cryptopunks.wrapdrive.offer

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import cc.cryptopunks.wrapdrive.api.EmptyOffer
import cc.cryptopunks.wrapdrive.api.EmptyPeer
import cc.cryptopunks.wrapdrive.api.EmptyPeerOffer
import cc.cryptopunks.wrapdrive.api.FilterIn
import cc.cryptopunks.wrapdrive.api.FilterOut
import cc.cryptopunks.wrapdrive.api.Offer
import cc.cryptopunks.wrapdrive.api.OfferId
import cc.cryptopunks.wrapdrive.api.Peer
import cc.cryptopunks.wrapdrive.api.PeerId
import cc.cryptopunks.wrapdrive.api.PeerOffer
import cc.cryptopunks.wrapdrive.api.client.accept
import cc.cryptopunks.wrapdrive.api.network
import cc.cryptopunks.wrapdrive.util.CoroutineViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class OfferModel : CoroutineViewModel() {

    val dispatch = MutableSharedFlow<Action>(extraBufferCapacity = 1)
    var hasWritePermission: Boolean = false
    val currentId = MutableStateFlow(null as OfferId?)
    val current = MutableStateFlow(PeerOffer())
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
        val EmptyUpdate = Update()
    }
}

sealed interface Action {
    object Share : Action
}

infix fun OfferModel.dispatch(action: Action) {
    dispatch.tryEmit(action)
}

fun OfferModel.updates(filter: String): Flow<OfferModel.Update> =
    updates.getValue(filter).filterNot { it == OfferModel.EmptyUpdate }

fun OfferModel.setOfferId(intent: Intent?) {
    val id = intent?.data?.lastPathSegment
    setCurrent(id)
}

fun OfferModel.setCurrent(id: OfferId?) {
    val current = if (id == null)
        EmptyPeerOffer
    else {
        val offer = updates
            .flatMap { (_, state) -> state.value.offers }
            .find { offer -> offer.id == id } ?: EmptyOffer
        val peer = updates.values
            .first().value.peers[offer.peer] ?: EmptyPeer
        PeerOffer(peer, offer)
    }
    setCurrent(current)
}

fun OfferModel.setCurrent(peerOffer: PeerOffer) {
    currentId.value = peerOffer.offer.id
    current.value = peerOffer
}

fun OfferModel.download() {
    val offerId = currentId.value ?: return
    launch {
        try {
            network.accept(offerId)
        } catch (e: Throwable) {
            error.value = OfferModel.Error("Cannot download files", e)
        }
    }
}

val OfferModel.Update.peersOffers: List<PeerOffer>
    get() = offers.map { offer ->
        val peer = peers[offer.peer] ?: EmptyPeer
        PeerOffer(peer, offer)
    }

val OfferModel.currentOffer
    get() = current.filter { current ->
        current != EmptyPeerOffer
    }.map {
        it.offer
    }
