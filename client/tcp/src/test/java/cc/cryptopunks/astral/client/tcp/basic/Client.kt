package cc.cryptopunks.astral.client.tcp.basic

import cc.cryptopunks.astral.api.connect
import cc.cryptopunks.astral.client.astralTcpNetwork
import cc.cryptopunks.astral.coder.GsonDecoder
import cc.cryptopunks.astral.coder.GsonEncoder
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        astralTcpNetwork(
            decode = GsonDecoder(),
            encode = GsonEncoder(),
        ).connect(
            identity = "033c352b239deb28292d48f36e742e8b84ba60ad1abdcc29c669883836203f6b3a",
            port = "tcp-test",
        ) {
            write("hello!!!\n".toByteArray())
        }
    }
}
