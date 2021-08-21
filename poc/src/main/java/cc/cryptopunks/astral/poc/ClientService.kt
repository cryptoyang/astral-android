package cc.cryptopunks.astral.poc

import astralApi.Network
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

private const val port = "test"

private val message = "Hello!!!\n".toByteArray()

suspend fun Network.service() = coroutineScope {
    val handler = register(port)
    try {
        launch {
            handler.requests().collect { request ->
                println("Obtained request: $request")
                val stream = request.accept()
                launch {
                    val buffer = ByteArray(message.size)
                    while (true) {
                        stream.read(buffer)
                        print(String(buffer))
                    }
                }
            }
        }.join()
    } finally {
        handler.close()
        println("Service closed")
    }
}

suspend fun Network.client() {
    delay(3000)
    val stream = connect(identity(), port)
    try {
        flow {
            while (true) emit(message)
        }.collect { message ->
            stream.write(message)
            delay(1000)
        }
    } finally {
        stream.close()
        println("Client closed")
    }
}
