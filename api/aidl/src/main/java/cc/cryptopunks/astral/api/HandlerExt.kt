package cc.cryptopunks.astral.api

import kotlinx.coroutines.flow.flow

fun Handler.requests() = flow<Connection> {
    var connection: Connection?
    while (true) {
        connection = next()
        when {
            connection != null -> emit(connection)
            else -> break
        }
    }
}
