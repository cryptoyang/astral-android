
package cc.cryptopunks.astral.api

/**
 * Network provides access to core network APIs
 */
interface Network {
    fun connect(identity: String, port: String): Stream
    fun register(port: String): PortHandler
}

/**
 * PortHandler is a handler for a locally registered port
 */
interface PortHandler {
    fun close()
    operator fun next(): ConnectionRequest
}

/**
 * ConnectionRequest represents a connection request sent to a port
 */
interface ConnectionRequest {
    fun accept(): Stream
    fun caller(): String
    fun query(): String
    fun reject()
}

/**
 * Stream represents a bidirectional stream
 */
interface Stream {
    fun close()
    fun read(buffer: ByteArray): Int
    fun write(buffer: ByteArray): Int
}
