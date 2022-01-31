package cc.cryptopunks.ui.service.astral

import cc.cryptopunks.astral.ext.bytesL32
import cc.cryptopunks.astral.ext.stringL8
import cc.cryptopunks.astral.io.reader
import cc.cryptopunks.astral.net.Network
import cc.cryptopunks.ui.mapper.Jackson
import cc.cryptopunks.ui.model.Service
import cc.cryptopunks.ui.service.schema.OpenRpc
import cc.cryptopunks.ui.service.schema.toSchema
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

class AstralUIService(
    private val network: Network,
) :
    Service.Interface,
    CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.IO) {

    private val port = "ui"
    private val nodeId = "024d47047667312be7cd0a14033323b716030f5fc9dd7ae774eb96527a76fa55f9"

    private object Method {
        val Schemas = "schema"
        val Request = "request"
        val Subscribe = "subscribe"
    }

    override fun execute(request: Service.Request): Job = launch {
        network.run {
            query(nodeId, port).run {
                stringL8 = Method.Request
                query(nodeId, stringL8)
            }.run {
                val (destPort, method) = request.method.split("/")
                stringL8 = destPort
                write(
                    Jackson.jsonMapper.writeValueAsBytes(
                        RpcJsonRequest(
                            method = method,
                            params = request.args.values.toList()
                        )
                    )
                )
            }
        }
    }

    override fun subscribe(request: Service.Request): Flow<Any> = channelFlow {
        val stream = withContext(Dispatchers.IO) {
            network.run {
                query(nodeId, port).run {
                    stringL8 = Method.Subscribe
                    query(nodeId, stringL8)
                }
            }
        }
        launch(Dispatchers.IO) {
            stream.run {
                val (destPort, method) = request.method.split("/")
                stringL8 = destPort
                val rpcRequest = RpcJsonRequest(
                    method = method,
                    params = request.args.values.toList(),
                    id = 1
                )
                println("sending rpc request")
                println(Jackson.jsonPrettyWriter.writeValueAsString(rpcRequest))
                write(Jackson.jsonMapper.writeValueAsString(rpcRequest).toByteArray())
                println("reading subscribed elements")
                reader().runCatching {
                    forEachLine { line ->
                        println(line)
                        val data = Jackson.jsonSlimMapper.readValue<Map<String, Any>>(line)
                        val result = data.getValue("result")
                        trySendBlocking(result)
                    }
                }

                println("finish reading responses of $request")
            }
        }
        awaitClose {
            stream.close()
        }
    }

    override fun schemas(
        known: Set<Service.Schema.Version>,
    ): Flow<Service.Schema> = channelFlow {
        withContext(Dispatchers.IO) {
            network.run {
                query(nodeId, port).run {
                    stringL8 = Method.Schemas
                    query(nodeId, stringL8)
                }
            }.run {
                while (true) {
                    val bytes = bytesL32
                    val doc = Jackson.jsonMapper.readValue<OpenRpc.Document>(bytes)
                    val schema = doc.toSchema()
                    send(schema)
                }
            }
        }
    }

    override fun status(): Flow<Service.Status> {
        TODO("Not yet implemented")
    }
}

data class RpcJsonRequest(
    val method: String,
    val params: List<Any>,
    val id: Long = 0,
)
