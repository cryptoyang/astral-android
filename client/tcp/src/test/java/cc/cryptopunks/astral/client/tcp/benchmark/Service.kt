package cc.cryptopunks.astral.client.tcp.benchmark

import cc.cryptopunks.astral.api.readMessage
import cc.cryptopunks.astral.api.register
import cc.cryptopunks.astral.client.astralTcpNetwork
import cc.cryptopunks.astral.coder.GsonDecoder
import cc.cryptopunks.astral.coder.GsonEncoder
import kotlinx.coroutines.runBlocking
import java.lang.System.currentTimeMillis


fun main() {
    runBlocking {
        astralTcpNetwork(
            identity = "033c352b239deb28292d48f36e742e8b84ba60ad1abdcc29c669883836203f6b3a",
            decode = GsonDecoder(),
            encode = GsonEncoder(),
        ).register(
            port = "tcp-test",
        ) {
            val start = currentTimeMillis()
            while (readMessage {  });
            val stop = currentTimeMillis()
            println(start)
            println(stop)
            println(stop - start)
        }
    }
}
