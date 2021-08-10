package cc.cryptopunks.astral.test

import android.util.Log
import cc.cryptopunks.astral.api.Request
import cc.cryptopunks.astral.api.readWriteSocket
import cc.cryptopunks.astral.api.readJsonRequest
import cc.cryptopunks.astral.api.writeJsonRequest
import cc.cryptopunks.astral.api.writeOk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ServerSocket
import java.net.Socket
import java.nio.CharBuffer

private const val identity = "02212c8beca38c02f09beb4cf9701504b53dccee55f88d02259527c31312c18819"

private const val localhost = "127.0.0.1"
private const val astralTcpPort = 8625

private fun astralSocket() = Socket(localhost, astralTcpPort)

private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

fun startTcpTest() {
    scope.launch { startService() }
    scope.launch { delay(3000); startClient() }
}

fun stopTcpTest() {
    scope.cancel()
}

private fun startService() {
    val tag = "service"
    val server = ServerSocket(0)
    println("Registering service $server")
    scope.launch(Dispatchers.IO) {
        while (true) {
            val socket = server.accept()
            scope.launch(Dispatchers.IO) {
                Log.d(tag, "New connection")

                socket.readWriteSocket().apply {
                    val request = readJsonRequest()
                    Log.d(tag, request.toString())

                    writeOk()

                    val buffer = CharArray(4096)
                    var len: Int
                    while (true) {
                        len = reader.read(buffer)
                        if (len > 0) println(String(buffer.copyOf(len)))
                        else break
                    }
                }

                Log.d(tag, "Closing connection")
            }
        }
    }


    astralSocket().readWriteSocket().apply {
        writeJsonRequest(
            type = Request.Type.register,
            identity = identity,
            port = "test2",
            path = ":" + server.localPort
        )
        val buffer = CharBuffer.allocate(128)
        while (true) {
            val len = reader.read(buffer)
            if (len > 0) print(buffer.take(len))
            else break
        }
        Log.d(tag, "Registered")
    }
}

private suspend fun startClient() {
    val tag = "client"
    println("Connecting client")
    astralSocket().readWriteSocket().apply {
        writeJsonRequest(
            type = Request.Type.connect,
            identity = identity,
            port = "test2",
        )
        Log.d(tag, "Start sending")

        withContext(Dispatchers.IO) {
            writer.use { writer ->
                while (true) {
                    delay(1000)
                    writer.write("Hello!!!\n")
                    writer.flush()
                }
            }

        }
    }
    println("Disconnect client")
}
