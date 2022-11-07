package cc.cryptopunks.wrapdrive.proto

import cc.cryptopunks.astral.client.Stream
import cc.cryptopunks.astral.client.enc.StreamEncoder
import cc.cryptopunks.astral.client.ext.byte
import cc.cryptopunks.astral.client.ext.decodeList
import cc.cryptopunks.astral.client.ext.string8
import kotlin.system.measureNanoTime

fun StreamEncoder.offers(
    filter: Filter,
): List<Offer> {
    byte = CmdListOffers
    byte = filter
    return decodeList()
}

fun Stream.accept(
    offerId: OfferId,
): Byte {
    byte = CmdAcceptOffer
    string8 = offerId
    return byte
}

fun Stream.send(
    peerId: String,
    uri: String,
): Pair<OfferId, ResultCode> {
    byte = CmdCreateOffer
    string8 = peerId
    string8 = uri
    return string8 to byte
}

fun StreamEncoder.peers(): List<Peer> {
    byte = CmdListPeers
    return decodeList()
}

fun StreamEncoder.subscribeOffers(filter: Filter) =
    subscribe<Offer>(CmdListenOffers, filter)

fun StreamEncoder.subscribeStatus(filter: Filter) =
    subscribe<Status>(CmdListenStatus, filter)

inline fun <reified T> StreamEncoder.subscribe(
    cmd: Byte,
    filter: Filter,
): () -> T {
    byte = cmd
    byte = filter
    val reader = input.bufferedReader()
    val type = T::class.java
    return {
        val line = reader.readLine()
        encoder.decode(line, type)
    }
}

fun Stream.unsubscribe() {
    byte = CmdClose
}

fun Stream.ping(): (Byte) -> Long {
    byte = CmdPing
    return { code ->
        measureNanoTime {
            byte = code
            byte
        }
    }
}
