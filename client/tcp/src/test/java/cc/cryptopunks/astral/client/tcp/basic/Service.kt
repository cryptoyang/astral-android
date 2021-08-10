package cc.cryptopunks.astral.client.tcp.basic

import cc.cryptopunks.astral.api.readMessage
import cc.cryptopunks.astral.api.register
import cc.cryptopunks.astral.client.astralTcpNetwork
import cc.cryptopunks.astral.coder.GsonDecoder
import cc.cryptopunks.astral.coder.GsonEncoder
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        astralTcpNetwork(
            identity = "033c352b239deb28292d48f36e742e8b84ba60ad1abdcc29c669883836203f6b3a",
            decode = GsonDecoder(),
            encode = GsonEncoder(),
        ).register(
            port = "tcp-test",
        ) {
            while (readMessage(::print));
        }
    }
}
