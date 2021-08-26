package cc.cryptopunks.astral.node.core

import cc.cryptopunks.astral.api.ConnectionRequest
import cc.cryptopunks.astral.api.PortHandler
import cc.cryptopunks.astral.api.Network
import cc.cryptopunks.astral.api.Stream

class NetworkAdapter(
    private val binding: astralApi.Network,
) : Network {
    override fun connect(
        identity: String,
        port: String,
    ): Stream = StreamAdapter(binding.connect(identity, port))

    override fun register(
        port: String,
    ): PortHandler = PortHandlerAdapter(binding.register(port))

    override fun identity(): String = binding.identity()
}


class StreamAdapter(
    private val binding: astralApi.Stream,
) : Stream {
    override fun read(buffer: ByteArray): Int = binding.read(buffer).toInt()
    override fun write(buffer: ByteArray): Int = binding.write(buffer).toInt()
    override fun close() = binding.close()
}

class PortHandlerAdapter(
    private val binding: astralApi.PortHandler,
) : PortHandler {
    override fun close() = binding.close()
    override fun next(): ConnectionRequest = ConnectionRequestAdapter(binding.next())
}

class ConnectionRequestAdapter(
    private val binding: astralApi.ConnectionRequest,
) : ConnectionRequest {
    override fun accept(): Stream = StreamAdapter(binding.accept())
    override fun caller(): String = binding.caller()
    override fun query(): String = binding.query()
    override fun reject() = binding.reject()
}
