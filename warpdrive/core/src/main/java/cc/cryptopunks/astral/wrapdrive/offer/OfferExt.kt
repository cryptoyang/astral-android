package cc.cryptopunks.astral.wrapdrive.offer

import android.net.Uri
import cc.cryptopunks.astral.wrapdrive.api.EmptyPeer
import cc.cryptopunks.astral.wrapdrive.api.Info
import cc.cryptopunks.astral.wrapdrive.api.Offer
import cc.cryptopunks.astral.wrapdrive.api.OfferId
import cc.cryptopunks.astral.wrapdrive.api.PeerId
import cc.cryptopunks.astral.wrapdrive.api.PeerOffer
import cc.cryptopunks.astral.wrapdrive.api.StatusAwaiting
import cc.cryptopunks.astral.wrapdrive.util.formatSize
import java.text.SimpleDateFormat

val Offer.formattedDateTime: String
    get() = SimpleDateFormat.getDateTimeInstance().format(update)

val Offer.formattedSize: String
    get() = "${summaryProgress.formatSize()} / ${summarySize.formatSize()}"

val Offer.formattedInfo: String?
    get() = when (files.size) {
        0 -> ""
        1 -> files.first().uri
        else -> when {
            !files.first().isDir -> ""
            else -> files.fold(files.first().uri) { dir, info ->
                if (info.uri.startsWith(dir)) dir
                else ""
            }
        }
    }.formattedUriFileName

val String.formattedUriFileName: String?
    get() = Uri.parse(this).run { lastPathSegment ?: host }

val Offer.formattedAmount: String
    get() = if (files.size > 1) "(${files.size})" else ""

val PeerOffer.formattedName: String
    get() = peer.takeIf { peer != EmptyPeer }
        ?.run { alias.takeIf(String::isNotEmpty) }
        ?: if (offer.peer.length == 8) shortPeerId(offer.peer)
        else offer.peer

val Offer.formattedStatus: String
    get() = when (status) {
        StatusAwaiting -> status.replaceFirstChar(Char::uppercase)
        else -> status.replaceFirstChar(Char::uppercase) + " at"
    }

val Offer.summaryProgress: Long
    get() = when (index) {
        -1 -> 0
        files.size -> files.sumOf(Info::size)
        else -> files.subList(0, index).sumOf(Info::size) + progress
    }

val Offer.summarySize: Long
    get() = files.sumOf(Info::size)


fun shortPeerId(id: PeerId): String =
    "UID-" + id.substring(0, 8).chunked(4).joinToString("-")

fun shortOfferId(id: OfferId): String =
    "OID-" + id.replace("-", "").substring(0, 12).chunked(4).joinToString("-")
