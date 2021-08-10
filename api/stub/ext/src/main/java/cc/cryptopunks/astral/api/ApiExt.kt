package cc.cryptopunks.astral.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

suspend fun Network.register(
    port: String,
    handle: suspend Stream.() -> Unit,
) = coroutineScope {
    val handler = register(port)
    while (true) {
        val connection = handler.next()
        println("next connection")
        launch(Dispatchers.IO) {
            val stream = connection.accept()
            println("accepted")
            try {
                stream.handle()
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                stream.close()
                println("closed")
            }
        }
    }
}

suspend fun Network.connect(
    identity: String,
    port: String,
    handle: suspend Stream.() -> Unit,
) = withContext(Dispatchers.IO) {
    val stream = connect(identity, port)
    try {
        stream.handle()
    } catch (e: Throwable) {
        e.printStackTrace()
    } finally {
        stream.close()
    }
}

fun Stream.readMessage(): String? {
    val result = StringBuilder()
    val buffer = ByteArray(4096)
    var len: Int
    do {
        len = read(buffer)
        if (len > 0) result.append(String(buffer.copyOf(len)))
    } while (len == buffer.size)
    return when {
        len == -1 && result.isEmpty() -> null
        else -> result.toString()
    }
}

fun Stream.readMessage(handle: (String) -> Unit): Boolean =
    readMessage()?.let(handle) != null
