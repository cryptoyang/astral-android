package cc.cryptopunks.wrapdrive.api.client

import cc.cryptopunks.astral.ext.byte
import cc.cryptopunks.astral.ext.query
import cc.cryptopunks.wrapdrive.api.Astral
import cc.cryptopunks.wrapdrive.api.CmdPing
import cc.cryptopunks.wrapdrive.api.Port
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withTimeout
import kotlin.system.measureNanoTime
import kotlin.time.Duration.Companion.seconds

fun Astral.ping() = channelFlow<Long> {
    runCatching {
        var code: Byte = 0
        query(Port) {
            byte = CmdPing
            while (true) {
                val time = measureNanoTime {
                    withTimeout(3.seconds) {
                        byte = code
                        code = byte
                    }
                }
                send(time)
                delay(1.seconds)
            }
        }
    }
}
