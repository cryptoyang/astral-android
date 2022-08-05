package cc.cryptopunks.wrapdrive.offer

import cc.cryptopunks.astral.err.AstralLocalConnectionException
import cc.cryptopunks.wrapdrive.api.EmptyPeer
import cc.cryptopunks.wrapdrive.api.FilterIn
import cc.cryptopunks.wrapdrive.api.FilterOut
import cc.cryptopunks.wrapdrive.api.Offer
import cc.cryptopunks.wrapdrive.api.Peer
import cc.cryptopunks.wrapdrive.api.PeerOffer
import cc.cryptopunks.wrapdrive.api.Peers
import cc.cryptopunks.wrapdrive.api.Status
import cc.cryptopunks.wrapdrive.api.client.offers
import cc.cryptopunks.wrapdrive.api.client.peers
import cc.cryptopunks.wrapdrive.api.client.status
import cc.cryptopunks.wrapdrive.api.client.subscribe
import cc.cryptopunks.wrapdrive.api.network
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch

fun OfferModel.subscribeChanges() {
    job.cancel()
    val init = mutableMapOf(
        FilterIn to MutableStateFlow(OfferModel.Update()),
        FilterOut to MutableStateFlow(OfferModel.Update()),
    )
    val contacts = suspend {
        network.peers().associateBy(Peer::id)
    }
    val refresh = suspend {
        val peers = contacts()
        init.forEach { (filter, channel) ->
            val offers = network
                .offers(filter)
                .sortedByDescending(Offer::create)

            val change = OfferModel.Update(
                offers = offers,
                peers = peers,
                value = offers.size,
                action = OfferModel.Update.Action.Init,
            )
            channel.emit(change)
        }
    }
    val subscribe = {
        init.map { (filter, channel) ->
            val items = updates.getValue(filter)
            offerChanges(
                channel,
                network.subscribe(filter),
                network.status(filter),
                contacts,
            ).onCompletion {
                println("Completed")
            }.onEach { change ->
                items.value = change
                currentId.value?.let { id ->
                    val index = change.offers.indexOfFirst { offer ->
                        offer.id == id
                    }
                    val indexes = change.run {
                        position until position + value
                    }
                    if (index in indexes) {
                        val offer = change.offers[index]
                        val peer = change.peers[offer.peer] ?: EmptyPeer
                        val current = PeerOffer(
                            offer = offer,
                            peer = peer
                        )
                        this.current.value = current
                    }
                }
            }
        }.asFlow().flattenMerge()
    }

    job = launch {
        try {
            refresh()
            subscribe().collect()
        } catch (e: Throwable) {
            val cause = when (e) {
                is CancellationException -> e.cause
                else -> e
            }
            if (cause != null && cause !is AstralLocalConnectionException) {
                e.printStackTrace()
                error.value = OfferModel.Error("Cannot subscribe for offers", cause)
            }
        }
    }
}

private fun offerChanges(
    changes: Flow<OfferModel.Update>,
    offers: Flow<Offer>,
    status: Flow<Status>,
    peers: suspend () -> Peers,
): Flow<OfferModel.Update> = merge(
    changes,
    offers,
    status,
).scan(
    OfferModel.Update()
) { state, value ->
    when (value) {
        is Offer -> state.copy(
            action = OfferModel.Update.Action.Inserted,
            peers = peers(),
            offers = listOf(value) + state.offers,
            position = 0,
            value = 1,
        )
        is Status -> {
            var position = 0
            val updated = state.offers.mapIndexed { index, offer ->
                if (offer.id != value.id) offer
                else offer.copy(
                    status = value.status,
                    update = value.update,
                    index = value.index,
                    progress = value.progress
                ).also {
                    position = index
                }
            }
            state.copy(
                action = OfferModel.Update.Action.Changed,
                offers = updated,
                position = position,
                value = 1
            )
        }
        is OfferModel.Update -> value
        else -> state
    }
}
