package cc.cryptopunks.astral.client

import cc.cryptopunks.astral.api.ConnectionRequest
import cc.cryptopunks.astral.api.Network
import cc.cryptopunks.astral.api.PortHandler
import cc.cryptopunks.astral.api.Stream
import cc.cryptopunks.astral.api.readMessage
import cc.cryptopunks.astral.coder.Decoder
import cc.cryptopunks.astral.coder.Encoder
import cc.cryptopunks.astral.coder.invoke
import cc.cryptopunks.astral.stream.AstralError
import cc.cryptopunks.astral.stream.Request
import cc.cryptopunks.astral.stream.Response
import java.io.Closeable
import java.net.ServerSocket
import java.net.Socket

fun astralTcpNetwork(
    identity: String,
    encode: Encoder,
    decode: Decoder,
): Network =
    TcpNetwork(identity, encode, decode)

private class TcpNetwork(
    private val identity: String,
    private val encode: Encoder,
    private val decode: Decoder,
) : Network {
    override fun identity(): String = identity

    override fun register(port: String): PortHandler =
        TcpPortHandler(ServerSocket(0), decode, encode).apply {
            TcpStream(astralSocket()).let { stream ->
                val request = Request(
                    type = Request.Type.register,
                    identity = identity,
                    port = port,
                    path = ":" + server.localPort
                )
                stream.write(encode(request).toByteArray())
                val message: String = stream.readMessage()
                    ?: throw AstralError("rejected by node")
                val response = decode<Response>(message)
                println("register response: $response")
                if (response.status != "ok")
                    throw AstralError(response.error)
            }
        }

    override fun connect(identity: String, port: String) =
        TcpStream(astralSocket()).let { stream ->
            val request = Request(
                type = Request.Type.connect,
                identity = identity,
                port = port,
                path = ":" + stream.socket.localPort,
            )
            stream.write(encode(request).toByteArray())
            val message = stream.readMessage()
                ?: throw AstralError("rejected by service")
            val response = decode<Response>(message)
            println("connect response: $response")
            if (response.status != "ok")
                throw AstralError(response.error)
            else stream
        }

}

private class TcpPortHandler(
    val server: ServerSocket,
    private val decode: Decoder,
    private val encode: Encoder,
) : PortHandler {
    override fun close() = server.close()
    override fun next(): TcpConnectionRequest {
        val stream = TcpStream(server.accept())
        return try {
            val message = stream.readMessage()
                ?: throw AstralError("rejected while reading request.")
            val request = decode<Request>(message)
            println("next request: $request")
            TcpConnectionRequest(
                stream = stream,
                encode = encode,
                caller = request.identity,
                query = request.port,
            )
        } catch (e: Throwable) {
            stream.close()
            throw e
        }
    }
}

private class TcpConnectionRequest(
    private val stream: TcpStream,
    private val encode: Encoder,
    private val caller: String,
    private val query: String,
) : ConnectionRequest {
    override fun accept() = stream.apply { write(encode(Response("ok")).toByteArray()) }
    override fun caller(): String = caller
    override fun query(): String = query
    override fun reject() = stream.close()
}

private class TcpStream(
    val socket: Socket,
) : Stream, Closeable {
    private val input by lazy { socket.getInputStream().buffered(4096) }
    private val output by lazy { socket.getOutputStream().buffered(4096) }
    override fun close() = socket.close()
    override fun read(buffer: ByteArray): Int = input.read(buffer)
    override fun write(buffer: ByteArray): Int = buffer
        .also(output::write)
        .also { output.flush() }
        .size
}

private fun astralSocket() = Socket("127.0.0.1", 8625)
