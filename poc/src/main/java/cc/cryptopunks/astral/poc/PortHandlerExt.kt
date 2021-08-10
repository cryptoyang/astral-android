package cc.cryptopunks.astral.poc

import astraljava.ConnectionRequest
import astraljava.PortHandler
import kotlinx.coroutines.flow.flow

fun PortHandler.requests() = flow<ConnectionRequest> {
    var request: ConnectionRequest?
    while (true) {
        request = next()
        when {
            request != null -> emit(request)
            else -> break
        }
    }
}
