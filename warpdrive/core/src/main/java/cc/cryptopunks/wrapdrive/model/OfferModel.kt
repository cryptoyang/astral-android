package cc.cryptopunks.wrapdrive.model

import android.content.Intent
import cc.cryptopunks.wrapdrive.proto.EmptyOffer
import cc.cryptopunks.wrapdrive.proto.EmptyPeer
import cc.cryptopunks.wrapdrive.proto.EmptyPeerOffer
import cc.cryptopunks.wrapdrive.proto.FilterIn
import cc.cryptopunks.wrapdrive.proto.FilterOut
import cc.cryptopunks.wrapdrive.proto.Offer
import cc.cryptopunks.wrapdrive.proto.OfferId
import cc.cryptopunks.wrapdrive.proto.Peer
import cc.cryptopunks.wrapdrive.proto.PeerId
import cc.cryptopunks.wrapdrive.proto.PeerOffer
import cc.cryptopunks.wrapdrive.proto.accept
import cc.cryptopunks.wrapdrive.proto.network
import cc.cryptopunks.wrapdrive.util.CoroutineViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class OfferModel : CoroutineViewModel() {

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

fun OfferModel.setOfferId(intent: Intent?) {
    val id = intent?.data?.lastPathSegment
    setCurrent(id)
}

fun OfferModel.setCurrent(id: OfferId?) {
    if (id == null)
        setCurrent(EmptyPeerOffer)
    else {
        currentId.value = id
        val offer = updates
            .flatMap { (_, state) -> state.value.offers }
            .find { offer -> offer.id == id } ?: EmptyOffer
        val peer = updates.values
            .first().value.peers[offer.peer] ?: EmptyPeer
        val peerOffer = PeerOffer(peer, offer)
        println(peerOffer)
        if (peerOffer != EmptyPeerOffer) {
            setCurrent(peerOffer)
        }
    }
}

fun OfferModel.setCurrent(peerOffer: PeerOffer) {
    currentId.value = peerOffer.offer.id.takeIf(OfferId::isNotBlank)
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
        PeerOffer(
            offer = offer,
            peer = peers[offer.peer]
                ?: EmptyPeer,
        )
    }
