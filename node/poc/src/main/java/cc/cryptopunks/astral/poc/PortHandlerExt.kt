package cc.cryptopunks.astral.poc

import astralApi.ConnectionRequest
import astralApi.PortHandler
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
