package cc.cryptopunks.wrapdrive.api.client

import cc.cryptopunks.astral.enc.EncNetwork
import cc.cryptopunks.astral.ext.bytesL8
import cc.cryptopunks.astral.ext.query
import cc.cryptopunks.astral.ext.read
import cc.cryptopunks.astral.ext.stringL8
import cc.cryptopunks.wrapdrive.api.PeerId

suspend fun EncNetwork.dial(
    network: String,
    address: ByteArray,
): PeerId =
    query("dial") {
        stringL8 = network
        bytesL8 = address
        // Read astral identity resolved from network address
        read(33).decodeToString()
    }
